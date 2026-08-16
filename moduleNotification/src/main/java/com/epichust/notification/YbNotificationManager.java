package com.epichust.notification;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import static java.lang.Thread.sleep;

/**
 * 通知管理器（单例）：兼容 Android 4.1 ~ Android 16+，
 * 支持通知渠道、铃声/震动开关、动态震动时长、闪光灯三档闪烁模式、手动停止震动/闪烁
 *      （最早版本 2019/7/18，后经多次迭代升级）
 * @author yuanbao
 * @date 2026-08-16
 */
public class YbNotificationManager {
    private static final String TAG = "YbNotification";
    // 默认渠道信息（不同 App 可调用 setChannelInfo() 自定义）
    private static final String DEFAULT_CHANNEL_ID_PREFIX = "yb-notification";
    private static final String DEFAULT_CHANNEL_NAME_PREFIX = "YB-任务通知";
    private String mChannelIdPrefix = DEFAULT_CHANNEL_ID_PREFIX;
    private String mChannelNamePrefix = DEFAULT_CHANNEL_NAME_PREFIX;
    // 默认短震动模式：开始→振100ms→停200ms→振100ms→停200ms
    private static final long[] DEFAULT_VIBRATE_PATTERN = new long[]{100, 200, 100, 200};
    // 弄个单例模式
    private static YbNotificationManager singleton = null;

    // 点亮屏幕+解锁
    PowerManager mPowerManager; // 电源管理器对象
    KeyguardManager mKeyguardManager; // 键盘锁管理器对象
    KeyguardManager.KeyguardLock mKeyguardLock;
    DevicePolicyManager mDPM; // 获取设备策略服务

    private Context mContext;
    private int notifyId = 100;

    /**
     * 通知配置（可选参数，不设则走默认值）
     */
    public static class Config {
        public String channelId;           // 渠道ID前缀，null=使用全局默认。实际会生成 {prefix}-sound / {prefix}-silent 两个渠道
        public String channelName;         // 渠道名称前缀（用户可见），null=使用全局默认
        public boolean soundEnabled = true;     // 是否响铃
        public boolean vibrateEnabled = true;   // 是否震动
        /** 闪光灯闪烁模式：0=不闪（默认），1=闪两下停2s再闪两下结束，2=闪两下停2s循环持续60s */
        public int flashMode = 0;
        public int vibrateDurationSec;     // 震动持续秒数，0=使用默认短震动；同时复用为闪光灯模式2的总时长（秒）
    }

    private YbNotificationManager() {
    }
    public static synchronized YbNotificationManager getInstance() {
        if (singleton == null) {
            singleton = new YbNotificationManager();
        }
        return singleton;
    }

    /**
     * 自定义渠道信息（可选，不调用则用默认值）
     * 不同 App 建议设置不同的渠道前缀，推荐格式："包名.类别"，如 "com.xxx.app.task"
     *
     * @param channelIdPrefix   渠道ID前缀，实际生成 {prefix}-sound 和 {prefix}-silent
     * @param channelNamePrefix 渠道名称前缀，实际显示 "{prefix}" 和 "{prefix}-静音"
     */
    public void setChannelInfo(String channelIdPrefix, String channelNamePrefix) {
        this.mChannelIdPrefix = channelIdPrefix;
        this.mChannelNamePrefix = channelNamePrefix;
    }

    /**
     * 根据持续秒数生成间歇式震动模式
     * 模式：振2000ms → 停2000ms → 振2000ms → ... 循环至目标时长
     * @param durationSec 持续秒数
     * @return 震动模式数组，格式 {延迟, 振, 停, 振, 停, ...}
     */
    private static long[] buildVibratePattern(int durationSec) {
        if (durationSec <= 0) {
            return DEFAULT_VIBRATE_PATTERN;
        }
        // 每4秒 = 振2s + 停2s = 2个元素，每个元素 2000ms
        int elementCount = durationSec / 2;
        if (elementCount < 2) {
            elementCount = 2; // 最少保证一次完整的 振2s+停2s
        }
        long[] pattern = new long[elementCount + 1]; // +1 给开头的 delay
        pattern[0] = 0; // 立即开始，无延迟
        for (int i = 0; i < elementCount; i++) {
            pattern[i + 1] = 2000;
        }
        return pattern;
    }

    /**
     * 推送消息通知（兼容旧版调用，使用默认配置）
     */
    public void showNotification(Context context, Class clazz, String title, String content) throws Exception {
        showNotification(context, clazz, title, content, null);
    }

    /**
     * @method    推送消息通知（支持自定义配置）
     *      20260809 升级：兼容 Android 4.1 ~ Android 16+，并适配 Android 8.0+ 的通知渠道，支持铃声和震动
     * @param     config 可选配置，传 null 则全部使用默认值
     *
     * @author  yuanbao
     * @date    2019/7/18
     */
    public void showNotification(Context context, Class clazz, String title, String content, Config config) throws Exception
    {
        this.mContext = context;
        FileLogUtils.init(this.mContext);

        // 配置为 null 则使用默认
        if (config == null) {
            config = new Config();
        }
        long[] vibratePattern = buildVibratePattern(config.vibrateDurationSec);

        // 电源组件初始化
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mKeyguardManager = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock"); // 只能禁用滑动锁，不能操作指纹、密码
        mDPM = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);



        // 锁屏时点亮屏幕：但发现亮屏后无法响通知铃声和震动了，未解决。 -废弃，已通过 NotificationChannel 方式解决
        /*mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = mPowerManager.isScreenOn();
        if (!isScreenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
            mWakeLock.acquire(10*1000); // 点亮屏幕
            mWakeLock.release(); // 释放

            // 线程等待片刻，让机器彻底从休眠中退出，然后推通知才有声音和震动 -- 无效
            sleep(100);
        }*/


        // 获取系统通知服务
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建 PendingIntent
        int requestCode = 0;
        int flags = PendingIntent.FLAG_ONE_SHOT; // FLAG_UPDATE_CURRENT 消息对象是共用一个；FLAG_ONE_SHOT 各自分配一条,另要保证每个通知对象的id不同

        Intent intent = new Intent();
        intent.setClass(mContext, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, requestCode, intent, flags); // getActivity跳转到一个activity组件


        // ============================================================
        // 创建通知（兼容 Android 4.1 ~ Android 16+）
        // ============================================================
        //
        // 【铃声 & 震动的适配策略】
        //
        // 铃声：
        //   → 预建两个渠道：有声渠道（带铃声）+ 无声渠道（无铃声）
        //   → 根据 config.soundEnabled 选择对应渠道
        //   → MIUI 无法动态改渠道，但可以切换渠道，所以二选一
        //
        // 震动：
        //   → 渠道层面关闭震动（enableVibration=false）
        //   → 改用系统 Vibrator 服务独立控制，完全绕过通知系统的限制
        //   → 好处：震动时长、模式可逐条动态控制，不受渠道束缚
        // ============================================================

        // 构建通知-已废弃的旧方案（NotificationCompat.Builder，8.0+ 无 Channel 会闪退）
//        Notification notification = new NotificationCompat.Builder(mContext).setContentTitle(title)
//                .setContentText(content).setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
//                .setContentIntent(pendingIntent).setAutoCancel(true) // 设置跳转和自动取消
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // 设置消息声音
//                .setVibrate(new long[]{100, 200, 100, 200}) // 设置震动
//                .setLights(Color.rgb(0,0,255),5000,5000) // 设置呼吸灯
//                .build();
//        manager.notify(notifyId++, notification);


        // Android 8.0+：预建有声/无声两个渠道（是否铃声需通过APP通知渠道权限里分别控制两个渠道，是否震动则不走渠道控制），使用时根据配置选择其中一个
        String selectedChannelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 渠道ID/名称前缀：优先用单条通知指定的，否则用全局设置的
            String idPrefix = config.channelId != null ? config.channelId : mChannelIdPrefix;
            String namePrefix = config.channelName != null ? config.channelName : mChannelNamePrefix;
            String soundChannelId = idPrefix + "-sound";
            String silentChannelId = idPrefix + "-silent";

            // 创建有声渠道
            NotificationChannel soundChannel = new NotificationChannel(soundChannelId, namePrefix, NotificationManager.IMPORTANCE_HIGH);
            soundChannel.enableLights(true);
            soundChannel.enableVibration(true);
            soundChannel.setVibrationPattern(new long[]{0}); // 骗过 MIUI：允许震但震0秒，避免系统默认短震打断 Vibrator
            android.net.Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (soundUri != null) {
                soundChannel.setSound(soundUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            }
            soundChannel.setShowBadge(true);
            soundChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager.createNotificationChannel(soundChannel);

            // 创建无声渠道
            NotificationChannel silentChannel = new NotificationChannel(silentChannelId, namePrefix + "-静音", NotificationManager.IMPORTANCE_HIGH);
            silentChannel.enableLights(true);
            silentChannel.enableVibration(true);
            silentChannel.setVibrationPattern(new long[]{0});
            silentChannel.setSound(null, null); // 无声
            silentChannel.setShowBadge(true);
            silentChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager.createNotificationChannel(silentChannel);

            // 根据配置选择渠道
            selectedChannelId = config.soundEnabled ? soundChannelId : silentChannelId;
        } else {
            // Android < 8.0 无渠道概念，只用 Builder
            selectedChannelId = null;
        }

        // 构建通知
        Notification.Builder builder = new Notification.Builder(mContext.getApplicationContext())
                .setContentTitle(title)
                .setContentText(content).setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent).setAutoCancel(true) // 设置跳转和自动取消
                .setLights(Color.rgb(0,0,255),5000,5000) // 设置呼吸灯
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(selectedChannelId);
        } else {
            // Android < 8.0：Builder 直接控音震
            builder.setSound(config.soundEnabled
                    ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : null);
            builder.setVibrate(config.vibrateEnabled ? vibratePattern : null);
        }
        Notification notification = builder.build(); // 获取构建好的Notification
        manager.notify(notifyId++, notification);

        // 震动：使用 Vibrator 服务独立控制，不依赖通知渠道
        if (config.vibrateEnabled) {
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, -1));
                } else {
                    vibrator.vibrate(vibratePattern, -1);
                }
            }
        }

        NotificationChannel created = manager.getNotificationChannel(selectedChannelId);
        Log.w(TAG, "渠道重要性: " + created.getImportance() + " 震动: " + created.shouldVibrate());
        Log.w(TAG, "推送广播通知："+title+"|"+content);
        FileLogUtils.write("推送广播通知："+title+"|"+content);

        // 20200813 增加锁屏亮屏+弹窗提醒
        /*// 推送广播
        Intent it = new Intent();
        it.setAction("com.epichust.notification.WAKE");
        it.putExtra("title", title);
        it.putExtra("content", content);
        this.mContext.sendBroadcast(it);*/

        // 20200815  直接启动activity方式，在深度休眠后能亮屏弹窗，但没有声音和震动了
        /*Intent it = new Intent(mContext, NotificationWindowActivity.class);
        it.putExtra("title", title);
        it.putExtra("content", content);
        this.mContext.startActivity(it);*/

        // 20260815 推送通知时闪光灯按模式闪烁；模式2的总时长复用 vibrateDurationSec 参数控制
        flashByMode(mContext, config.flashMode, config.vibrateDurationSec);
    }

    /** 当前正在执行的闪光灯线程（新通知到来时打断旧的，避免叠加错乱） */
    private static volatile Thread sFlashThread = null;

    /**
     * 终止正在进行的震动和闪光灯循环闪烁（手动停止接口）
     * <p>震动：直接调用 Vibrator.cancel() 停止循环震动
     * <p>闪光灯：打断当前闪烁线程，并立即关灯
     * @param context 上下文
     */
    public void closeVibrateFlash(Context context) {
        // 1. 停止循环震动
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
                Log.w(TAG, "已手动停止震动");
            }
        } catch (Exception e) {
            Log.w(TAG, "停止震动失败: " + e.getMessage());
        }

        // 2. 打断闪光灯闪烁线程
        Thread flashThread = sFlashThread;
        if (flashThread != null && flashThread.isAlive()) {
            flashThread.interrupt();
        }

        // 3. 立即关灯，防止线程响应 interrupt 前灯还亮着
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                if (cameraManager != null) {
                    for (String cameraId : cameraManager.getCameraIdList()) {
                        cameraManager.setTorchMode(cameraId, false);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "关闭闪光灯失败: " + e.getMessage());
            }
        }
        FileLogUtils.init(context);
        FileLogUtils.write("已手动终止震动与闪光灯");
    }

    /**
     * 闪光灯按模式闪烁，在后台线程中执行，不阻塞主线程
     * <p>通过 CameraManager.setTorchMode 控制闪光灯，需要 Android 5.0+ 及 CAMERA 权限
     * @param mode        0=不闪（默认），1=闪两下停2s再闪两下结束，2=闪两下停2s循环闪烁
     * @param durationSec 模式2的总时长（秒），复用震动时长参数；&lt;=0 时默认 60s
     */
    private void flashByMode(final Context context, final int mode, final int durationSec) {
        if (mode <= 0) {
            return; // 0=不开闪光灯
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.w(TAG, "Android 5.0 以下不支持闪光灯闪烁");
            FileLogUtils.write("闪光灯闪烁失败：Android 5.0 以下不支持");
            return;
        }
        // Android 6.0+ 需要运行时 CAMERA 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && context.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "无 CAMERA 权限，跳过闪光灯闪烁");
            FileLogUtils.write("闪光灯闪烁失败：无 CAMERA 权限，请在系统设置中授予相机权限");
            return;
        }
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CameraManager cameraManager = null;
                String torchCameraId = null;
                try {
                    cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
                    if (cameraManager == null) {
                        FileLogUtils.write("闪光灯闪烁失败：CameraManager 为 null");
                        return;
                    }
                    // 找带有闪光灯的摄像头
                    for (String cameraId : cameraManager.getCameraIdList()) {
                        CameraCharacteristics chars = cameraManager.getCameraCharacteristics(cameraId);
                        Boolean flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (flashAvailable != null && flashAvailable) {
                            torchCameraId = cameraId;
                            break;
                        }
                    }
                    if (torchCameraId == null) {
                        Log.w(TAG, "设备无闪光灯");
                        FileLogUtils.write("闪光灯闪烁失败：设备无闪光灯");
                        return;
                    }

                    long startTime = System.currentTimeMillis();
                    if (mode == 1) {
                        // 模式1：闪两下 → 停2s → 闪两下 → 结束
                        flashTwiceOnce(cameraManager, torchCameraId);
                        sleep(2000);
                        flashTwiceOnce(cameraManager, torchCameraId);
                    } else {
                        // 模式2：闪两下 → 停2s，循环闪烁；总时长由 durationSec 控制，默认60s
                        long totalMillis = (durationSec > 0) ? durationSec * 1000L : 60_000L;
                        while (System.currentTimeMillis() - startTime < totalMillis) {
                            flashTwiceOnce(cameraManager, torchCameraId);
                            sleep(2000);
                        }
                    }
                    FileLogUtils.write("闪光灯闪烁完成，mode=" + mode);
                } catch (InterruptedException e) {
                    // 被新通知打断，属正常现象
                    Log.i(TAG, "闪光灯闪烁被新通知打断");
                } catch (Exception e) {
                    Log.w(TAG, "闪光灯闪烁失败: " + e.getMessage());
                    FileLogUtils.write("闪光灯闪烁失败：" + e.toString());
                } finally {
                    // 无论哪种方式结束，都要确保关灯
                    if (cameraManager != null && torchCameraId != null) {
                        try {
                            cameraManager.setTorchMode(torchCameraId, false);
                        } catch (Exception ignored) {
                        }
                    }
                    sFlashThread = null;
                }
            }
        }, "yb-notification-flash");
        // 若上一次闪烁还在进行，先打断它，避免两个线程同时控制闪光灯
        Thread oldThread = sFlashThread;
        if (oldThread != null && oldThread.isAlive()) {
            oldThread.interrupt();
        }
        sFlashThread = thread;
        thread.start();
    }

    /**
     * 闪烁两下：开200ms → 关200ms → 开200ms → 关200ms
     */
    private void flashTwiceOnce(CameraManager cameraManager, String torchCameraId) throws Exception {
        for (int i = 0; i < 2; i++) {
            cameraManager.setTorchMode(torchCameraId, true);
            sleep(200);
            cameraManager.setTorchMode(torchCameraId, false);
            sleep(200);
        }
    }
}
