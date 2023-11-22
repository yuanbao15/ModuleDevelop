package cn.yuanbao.processalive;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * @fileName WorkService
 * @description 本地进程-后台运行的服务
 * Created by yuanbao on 2019/7/17
 */
public class WorkService extends Service {
    private final static String TAG = "WorkService";
    private String packageName = "";
    public static final int NOTIFICATION_ID=0x11;

    @Override
    public void onCreate() {
        super.onCreate();
        if(mBinder == null){
            mBinder = new MyBinder();
        }

        // 这儿写主进程要做的事情
        // 开启子线程每隔几秒执行一次
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    ToastUtils.showShort("保持心跳...");
                    // toast只能在主线程下，否则需要先Looper.prepare()后Looper.loop()，而且只能有一个Looper
                    // Toast.makeText(getApplicationContext(),"保持心跳...", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, packageName + "_保持心跳..." + i++);
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        packageName = intent.getStringExtra("packageName"); // 记录是哪个应用进程启动的服务

        startForeground(1, new Notification());
        bindService(new Intent(this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);

        /*PendingIntent contentIntent = PendingIntent.getService(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker("XXX")
                .setContentIntent(contentIntent)
                .setContentTitle("我是XXX，我怕谁!")
                .setAutoCancel(false)
                .setContentText("哈哈")
                .setWhen( System.currentTimeMilis());
        //把service设置为前台运行，避免手机系统自动杀掉改服务。
        startForeground(startId, builder.build());
        bindService(new Intent(this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);*/

        /*利用Android前台service的漏洞：
            对于 API level < 18 ：调用startForeground(ID， new Notification())，发送空的Notification ，图标则不会显示。 
            对于 API level >= 18：在需要提优先级的service A启动一个InnerService，两个服务同时startForeground，且绑定同样的 ID。Stop 掉InnerService ，这样通知栏图标即被移除。
        */
        /*//API 18以下，直接发送Notification并将其置为前台
        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.JELLY_BEAN_MR2) {
            startForeground(NOTIFICATION_ID, new Notification());
        } else {
            //API 18以上，发送Notification并将其置为前台后，启动InnerService
            Notification.Builder builder = new Notification.Builder(this);
            startForeground(NOTIFICATION_ID, builder.build());
            startService(new Intent(this, InnerService.class));
        }*/

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
            //在建立到服务的连接时调用，该连接使用到服务的通信通道的IBinder。
//            Log.w(TAG,"WorkService 服务启动");
            YbAidlInterface iMyAidlInterface = YbAidlInterface.Stub.asInterface(service);
            try {
                Log.w(TAG, "WorkService 完成连接至" + iMyAidlInterface.getServiceName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当与服务的连接丢失时重新开启服务
            Log.w(TAG,"WorkService 挂掉了，正在重新启用");
            startService(new Intent(WorkService.this, DaemonService.class));
            bindService(new Intent(WorkService.this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    private MyBinder mBinder;
    private class MyBinder extends YbAidlInterface.Stub{
        @Override
        public String getServiceName() throws RemoteException {
            return WorkService.class.getName();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG,"WorkService onDestroy");
    }

    /*// 用于实际控制service是否保留
    public class InnerService extends Service{
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onCreate() {
            super.onCreate();
            //发送与KeepLiveService中ID相同的Notification，然后将其取消并取消自己的前台显示
            Notification.Builder builder = new Notification.Builder(this);
            startForeground(NOTIFICATION_ID, builder.build());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopForeground(true);
                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(NOTIFICATION_ID);
                    stopSelf();
                }
            },100);
        }
    }*/

}
