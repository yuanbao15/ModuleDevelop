package com.epichust.processalive2026;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

/**
 * 进程保活管理器（2026 版）
 * 单前台服务方案：常驻 + 被杀自动恢复(START_STICKY) + 干净停止(全局停止开关)
 * 兼容 Android 8.0 (API 26) ~ Android 16 (API 36)
 * 已砍掉旧方案的双进程 Binder 守护与 JobScheduler 轮询
 */
public class ProcessManager {
    private static ProcessManager singleton = null;

    /**
     * 全局停止开关：置 true 后所有复活通道（系统自动重建/重复启动）立即自毁，不再拉起。
     * 服务运行在主进程，静态变量可正常共享。
     */
    public static volatile boolean sStopFlag = false;
    /** 服务运行标记（用于日志观测与幂等判断） */
    public static volatile boolean sServiceRunning = false;

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

    /**
     * 开启进程常驻
     * 前台服务 + START_STICKY（被系统杀死后自动重建恢复）
     */
    public void startAlive(Context context) {
        this.mContext = context;
        sStopFlag = false; // 再次开启时重置停止开关（支持 停止后立刻重新开启）

        Intent intent = new Intent(mContext, WorkService.class);
        intent.putExtra("packageName", mContext.getPackageName());

        // Android 8.0+ 必须使用 startForegroundService，服务需在 5 秒内 startForeground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }

        // 引导用户关闭电池优化，提升存活率
        addAppToWhiteList();
    }

    /**
     * 关闭进程常驻
     * 置停止开关 + stopService；WorkService 检测到开关后自毁并返回 START_NOT_STICKY，
     * 系统不会再重建该服务，实现"能停干净"。
     */
    public void stopAlive(Context context) {
        this.mContext = context;
        sStopFlag = true; // 1. 打开停止开关，阻断一切复活路径

        // 2. 停止服务（本模块不使用 bindService，stopService 可直接生效；
        //    若服务正处于 onStartCommand，检测到开关后会走 stopSelf 自毁）
        try {
            mContext.stopService(new Intent(mContext, WorkService.class));
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 引导加入电池优化白名单
     * 注意：ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 直接拉起授权页，
     * 在 Android 10+ 后台启动 Activity 会被拦截；改为跳转系统设置页，
     * 由用户手动选择应用，成功率更高。
     */
    public void addAppToWhiteList() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || mContext == null) {
            return;
        }
        try {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(mContext.getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } catch (Exception e) {
            // 部分 ROM 没有此设置页，忽略
        }
    }
}
