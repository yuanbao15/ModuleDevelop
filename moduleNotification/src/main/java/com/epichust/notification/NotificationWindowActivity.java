package com.epichust.notification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class NotificationWindowActivity extends Activity
{
    private static final String TAG = "TAG";

    PowerManager mPowerManager; // 电源管理器对象
    KeyguardManager mKeyguardManager; // 键盘锁管理器对象
    KeyguardManager.KeyguardLock mKeyguardLock;
    DevicePolicyManager mDPM; // 获取设备策略服务

    TextView tv_title ;
    TextView tv_content ;

    // 报错解决：Failed resolution of: Landroid/support/graphics/drawable/VectorDrawableCompat;
    static{
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "NotificationWindowActivity.onCreate");

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED // 锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD // 解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  // 保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); // 打开屏幕
        setContentView(R.layout.activity_notification_window);

        // 电源管理组件初始化
        initKeyguardAndPower();

        // 组件初始化
        tv_title = (TextView) findViewById(R.id.text_title);
        tv_content = (TextView) findViewById(R.id.text_content);
    }

    @Override protected void onResume()
    {
        super.onResume();

        // 拿到传过来的数据展示
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        tv_title.setText(title);
        tv_content.setText(content);
        //点亮屏幕
        // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
        mWakeLock.acquire(5*1000); // 点亮屏幕
        mWakeLock.release(); // 释放

        // 过10秒 自动销毁
        new Thread(){
            @Override
            public void run() {
                try
                {
                    sleep((10*1000));
                    finish();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 这个方法是当这个activity没有销毁的时候，人为的按下锁屏键，然后再启动这个Activity的时候会去调用
     * 栈内唯一，singleTask
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 此处有坑，必须设置新的intent，否则一直为第一次的intent

        // 电源管理组件初始化
        initKeyguardAndPower();
        Log.w(TAG, "NotificationWindowActivity.onNewIntent");

        boolean isScreenOn = mPowerManager.isScreenOn();
        if (!isScreenOn) {
            // 拿到传过来的数据
            String title = getIntent().getStringExtra("title");
            String content = getIntent().getStringExtra("content");
            tv_title.setText(title);
            tv_content.setText(content);
            // 点亮屏幕
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
            mWakeLock.acquire(5*1000); // 点亮屏幕
            mWakeLock.release(); // 释放

            // 过10秒 自动销毁
            new Thread(){
                @Override
                public void run() {
                    try
                    {
                        sleep((10*1000));
                        finish();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    @Override protected void onDestroy()
    {
        super.onDestroy();
    }

    // 电源管理模块init
    private void initKeyguardAndPower()
    {
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock"); // 只能禁用滑动锁，不能操作指纹、密码
        mDPM = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    // 让activity回到后台，而不是销毁
    @Override public void finish()
    {
        moveTaskToBack(true);
    }
}
