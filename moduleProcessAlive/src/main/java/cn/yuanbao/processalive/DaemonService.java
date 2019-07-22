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
 * @fileName DaemonService
 * @description 远程进程-即守护进程，启动本地服务后会启动远程进程的服务并绑定远程服务，
 * 同时远程服务也会绑定本地进程的服务，任何一个服务停止都会得到另一个进程的Binder 通知，
 * 即刻被拉起，实现进程保活的一种方式。即为双进程保活。
 * Created by yuanbao on 2019/7/17 
 */
public class DaemonService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //提高进程优先级 让此服务在前台运行，在此状态下向用户提供正在进行的通知
        startForeground(1, new Notification());
        bindService(new Intent(this, WorkService.class), mServiceConnection, Context.BIND_IMPORTANT);
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

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 当与服务的连接丢失时重新开启服务
            Log.w("TAG","DaemonService挂掉了，正在重新启用");
            startService(new Intent(DaemonService.this, WorkService.class));
            bindService(new Intent(DaemonService.this, WorkService.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("TAG","DaemonService onDestroy");
    }
}
