package com.epichust.process;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import cn.yuanbao.processalive.DaemonService;
import cn.yuanbao.processalive.JobWakeUpService;
import cn.yuanbao.processalive.WorkService;

/**
 * Created by yuanbao on 2019/7/17
 */
public class ProcessManager {
    // 弄个单例模式
    private static ProcessManager singleton = null;
    private ProcessManager() {
    }
    public static ProcessManager getInstance() {
        if (singleton == null) {
            synchronized (ProcessManager.class) {
                if (singleton == null) {
                    singleton = new ProcessManager();
                }
            }
        }
        return singleton;
    }

    private Context mContext;
    private Intent mLocalIntent; // 启动本地服务的intent
    private Intent mRemoteIntent; // 启动远程服务的intent
    private Intent mJobIntent; // job的intent
    /**
     * 开启双进程相互守护
     * @param context
     */
    public void startAlive(Context context) {
        this.mContext = context;

        mLocalIntent = new Intent(mContext, WorkService.class);
        String packageName =  mContext.getPackageName();
        mLocalIntent.putExtra("packageName", packageName); // 把应用名称传过去给服务
        mContext.startService(mLocalIntent); // 开启本地进程
        mRemoteIntent = new Intent(mContext, DaemonService.class);
        mContext.startService(mRemoteIntent); // 开启守护进程

        mJobIntent = new Intent(mContext, JobWakeUpService.class);
        // 定时任务保证服务常驻进程
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // api版本不低于21
            mContext.startService(mJobIntent);
        }
    }

    /**
     * 关闭双进程相互守护
     * @param context
     */
    public void stopAlive(Context context) {
        this.mContext = context;
        // 解除服务的绑定，停止服务
        // 绑定后没有解绑，无法使用stopService()将其停止，所以必须先解绑，再停止

        mContext.stopService(new Intent(mContext, WorkService.class));
        mContext.stopService(new Intent(mContext, DaemonService.class));
        mContext.stopService(new Intent(mContext, JobWakeUpService.class));

    }
}
