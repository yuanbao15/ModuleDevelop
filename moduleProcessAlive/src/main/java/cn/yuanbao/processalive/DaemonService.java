package cn.yuanbao.processalive;

import android.app.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * @fileName DaemonService
 * @description 远程进程-即守护进程，启动本地服务后会启动远程进程的服务并绑定远程服务，
 * 同时远程服务也会绑定本地进程的服务，任何一个服务停止都会得到另一个进程的Binder 通知，
 * 即刻被拉起，实现进程保活的一种方式。即为双进程保活。
 * Created by yuanbao on 2019/7/17 
 */
public class DaemonService extends Service {
    private final static String TAG = "DaemonService";
    public static final int NOTIFICATION_ID = 1;
    private String packageName = "";

    @Override
    public void onCreate() {
        super.onCreate();

        if(mBinder == null){
            mBinder = new MyBinder();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //提高进程优先级 让此服务在前台运行，在此状态下向用户提供正在进行的通知
        if (intent == null) {
            return START_STICKY;
        }
        packageName = intent.getStringExtra("packageName"); // 记录是哪个应用进程启动的服务


        // 解决android 8.0以上开启Notification闪退问题
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0)) // 设置PendingIntent
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("守护进程...") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        String CHANNEL_ONE_ID = "Channel One";
        String CHANNEL_ONE_NAME = "Channel One";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ONE_ID);
        }
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(NOTIFICATION_ID, notification);

        bindService(new Intent(this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.w(TAG,"DaemonService 服务启动");
            YbAidlInterface iMyAidlInterface = YbAidlInterface.Stub.asInterface(service);
            try {
                Log.w(TAG, "DaemonService 完成连接至" + iMyAidlInterface.getServiceName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 当与服务的连接丢失时重新开启服务
            Log.w(TAG,"DaemonService 挂掉了，正在重新启用");
            startService(new Intent(DaemonService.this, WorkService.class));
            bindService(new Intent(DaemonService.this, WorkService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    private MyBinder mBinder;
    private class MyBinder extends YbAidlInterface.Stub{
        @Override
        public String getServiceName() throws RemoteException {
            return DaemonService.class.getName();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        Log.w(TAG,"DaemonService onDestroy");
    }
}
