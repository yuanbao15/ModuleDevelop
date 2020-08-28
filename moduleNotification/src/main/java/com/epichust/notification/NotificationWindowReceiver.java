package com.epichust.notification;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @name: NotificationWindowReceiver
 * @description: 接收到广播后判断是否为锁屏状态再去打开通知窗口activity
 *  实践时：能推通知，但声音提示不一定有，且存在通知后进程挂掉，原因是未写注销广播，不知道如何注销
 *  后废弃此广播，直接打开activity
 * @author yuanbao
 * @date 2020-08-13 16:02:47
 */
public class NotificationWindowReceiver extends BroadcastReceiver
{
    private static final String TAG = "TAG";
    private int notifyId = 100;

    @Override public void onReceive(Context context, Intent intent)
    {

        Log.w(TAG, intent.getAction());
        //拿到传来过来数据
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        Log.w(TAG, "onReceive: 收到广播。" + title + "|" + content);

        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); // 锁屏管理对象
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  // 电源管理器对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            // boolean isScreenOn = mPowerManager.isScreenOn();    // km.isKeyguardLocked() // 后去除这个判断，不管怎样都开activity
            //启动Activity
            Intent alarmIntent = new Intent(context, NotificationWindowActivity.class);
            //携带数据
            alarmIntent.putExtra("title", title);
            alarmIntent.putExtra("content", content);
            //activity需要新的任务栈
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);


            FileLogUtils.init(context);
            // 获取系统通知服务
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 创建 PendingIntent
            int requestCode = 0;
            int flags = PendingIntent.FLAG_ONE_SHOT; // FLAG_UPDATE_CURRENT 消息对象是共用一个；FLAG_ONE_SHOT 各自分配一条,另要保证每个通知对象的id不同

            Intent ni = new Intent();
            ni.setClass(context, context.getClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, ni, flags); // getActivity跳转到一个activity组件

            // 创建通知
            Notification notification = new NotificationCompat.Builder(context).setContentTitle(title)
                    .setContentText(content).setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent).setAutoCancel(true) // 设置跳转和自动取消
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // 设置消息声音
                    .setVibrate(new long[]{100, 200, 100, 200}) // 设置震动no
                    .setLights(Color.rgb(0,0,255),5000,5000) // 设置呼吸灯
                    .build();
            manager.notify(notifyId++, notification);
            Log.w(TAG, "推送通知："+title+"|"+content);
            FileLogUtils.write("推送通知："+title+"|"+content);
        }
    }

}
