package com.epichust.processalive2026;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * 保活前台服务（2026 版）
 * - Android 8.0+：由 startForegroundService 启动，onStartCommand 内必须尽快 startForeground，否则崩溃
 * - START_STICKY：被系统杀死后自动重建，此时 intent==null，同样要 startForeground 兜底
 * - 停止开关：ProcessManager.sStopFlag == true 时自毁，返回 START_NOT_STICKY，杜绝复活
 *
 * @author yuanbao
 * @date 2026-08-16
 */
public class WorkService extends Service {
    private static final String TAG = "WorkService2026";
    public static final int NOTIFICATION_ID = 2026;
    private static final String CHANNEL_ID = "process_alive_2026";
    private static final String CHANNEL_NAME = "进程守护2026";
    /** 心跳间隔：周期性重新挂载前台通知，防被划掉后长期缺失；兼顾耗电取 15s */
    private static final long HEARTBEAT_INTERVAL = 15_000L;

    private String packageName = "";
    private final Handler mHeartbeatHandler = new Handler();
    private final Runnable mHeartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (ProcessManager.sStopFlag) {
                // 已要求停止：不再续期心跳，通知由 stopForeground 移除
                return;
            }
            Log.i(TAG, packageName + " 心跳/刷新常驻通知...");
            // 重新挂载前台通知：被用户划掉（MIUI 等 ROM 允许划 NO_CLEAR 通知）后自动恢复，
            // 实现"通知不可被划掉"的实效
            startForegroundCompat();
            mHeartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessManager.sServiceRunning = true;
        Log.i(TAG, "WorkService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 停止开关：立即自毁，且不再被系统重建
        if (ProcessManager.sStopFlag) {
            stopForegroundCompat();
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            packageName = intent.getStringExtra("packageName");
        }

        // 无论 intent 是否为空（系统 START_STICKY 自动重建时 intent 为 null）都必须 startForeground，
        // 否则 Android 8.0+ 会抛 ForegroundServiceDidNotStartInTimeException 崩溃
        startForegroundCompat();

        // 启动轻量心跳，用于观测服务存活
        mHeartbeatHandler.removeCallbacks(mHeartbeatRunnable);
        mHeartbeatHandler.postDelayed(mHeartbeatRunnable, HEARTBEAT_INTERVAL);

        // START_STICKY：被杀后由系统自动重建（intent 会为 null，由上面的 startForegroundCompat 兜底）
        return START_STICKY;
    }

    /**
     * 按 API 分档启动前台服务
     * - API 30+：三参数版本，显式传 connectedDevice 类型（与 manifest 声明一致）
     * - API 29-：单参数版本，类型由 manifest 的 foregroundServiceType 提供
     */
    private void startForegroundCompat() {
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("进程守护运行中...")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                // 前台服务通知应不可清除；标准 Android 上禁止滑动/全部清除，
                // MIUI 等 ROM 会放宽，由心跳周期刷新兜底恢复
                .setFlag(Notification.FLAG_NO_CLEAR, true)
                .setFlag(Notification.FLAG_ONGOING_EVENT, true);

        // Android 8.0+ 通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            builder.setChannelId(CHANNEL_ID);
        }

        // Android 12+ 要求 PendingIntent 显式声明可变性，否则抛 IllegalArgumentException。
        // 点击通知打开宿主 App 主页（若宿主无 launcher Activity 则兜底指向本服务）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent contentIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (contentIntent == null) {
                contentIntent = new Intent(this, WorkService.class);
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);
        }

        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHeartbeatHandler.removeCallbacks(mHeartbeatRunnable);
        // 关闭守护时同步移除常驻通知，实现"关而关"
        stopForegroundCompat();
        ProcessManager.sServiceRunning = false;
        Log.i(TAG, "WorkService onDestroy, 停止开关=" + ProcessManager.sStopFlag);
    }

    /** 按 API 分档停止前台服务并移除通知 */
    private void stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }
}
