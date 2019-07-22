package cn.yuanbao.processalive;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @fileName WorkService
 * @description 本地进程-后台运行的服务
 * Created by yuanbao on 2019/7/17
 */
public class WorkService extends Service {
    private String packageName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        // 开启子线程每隔几秒执行一次
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    ToastUtils.showShort("保持心跳...");
                    // toast只能在主线程下，否则需要先Looper.prepare()后Looper.loop()，而且只能有一个Looper
                    // Toast.makeText(getApplicationContext(),"保持心跳...", Toast.LENGTH_SHORT).show();
                    Log.w("TAG", packageName + "_保持心跳..." + i++);
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        packageName = intent.getStringExtra("packageName"); // 记录是哪个应用进程启动的服务
        startForeground(1, new Notification()); // 给前端一个信息展示
        bindService(new Intent(this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IProcessConnection.Stub() {
        };
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //在建立到服务的连接时调用，该连接使用到服务的通信通道的IBinder。
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当与服务的连接丢失时重新开启服务
            Log.w("TAG","WorkService挂掉了，正在重新启用");
            startService(new Intent(WorkService.this, DaemonService.class));
            bindService(new Intent(WorkService.this, DaemonService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("TAG","WorkService onDestroy");
    }
}
