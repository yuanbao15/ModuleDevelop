package com.epichust.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;

/**
 * Created by yuanbao on 2019/7/18
 */
public class YbNotificationManager {
    private static final String TAG = "TAG";
    // 弄个单例模式
    private static YbNotificationManager singleton = null;
    private YbNotificationManager() {
    }
    public static synchronized YbNotificationManager getInstance() {
        if (singleton == null) {
            singleton = new YbNotificationManager();
        }
        return singleton;
    }

    private Context mContext;
    private int notifyId = 100;
    PowerManager mPowerManager; // 电源管理器对象

    /**
     * @method    推送消息通知
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/7/18 
     */
    public void showNotification(Context context, Class clazz, String title, String content) throws Exception
    {
        this.mContext = context;
        FileLogUtils.init(this.mContext);


        // 先亮屏
//        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
//        boolean isScreenOn = mPowerManager.isScreenOn();
//        if (!isScreenOn) {
//            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
//            PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
//                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
//                            PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
//            mWakeLock.acquire(10*1000); // 点亮屏幕
//            mWakeLock.release(); // 释放
//
//            // 线程等待片刻，让机器彻底从休眠中退出，然后推通知才有声音和震动
//            sleep(50);
//        }


        // 获取系统通知服务
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建 PendingIntent
        int requestCode = 0;
        int flags = PendingIntent.FLAG_ONE_SHOT; // FLAG_UPDATE_CURRENT 消息对象是共用一个；FLAG_ONE_SHOT 各自分配一条,另要保证每个通知对象的id不同

        Intent intent = new Intent();
        intent.setClass(mContext, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, requestCode, intent, flags); // getActivity跳转到一个activity组件

        // 创建通知
        Notification notification = new NotificationCompat.Builder(mContext).setContentTitle(title)
                .setContentText(content).setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent).setAutoCancel(true) // 设置跳转和自动取消
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // 设置消息声音
                .setVibrate(new long[]{100, 200, 100, 200}) // 设置震动
                .setLights(Color.rgb(0,0,255),5000,5000) // 设置呼吸灯
                .build();
        manager.notify(notifyId++, notification);

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
    }
}
