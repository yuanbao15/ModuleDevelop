package com.epichust.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
    /**
     * @method    推送消息通知
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/7/18 
     */
    public void showNotification(Context context, Class clazz, String title, String content) {
        this.mContext = context;
        // 获取系统通知服务
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建 PendingIntent
        int requestCode = 0;
        int flags = PendingIntent.FLAG_ONE_SHOT; // FLAG_UPDATE_CURRENT 消息对象是共用一个；FLAG_ONE_SHOT 各自分配一条,另要保证每个通知对象的id不同

//        Intent intent = new Intent(mContext, clazz);// 启动 NotificationActivity 活动
        Intent intent = new Intent();
        intent.setClass(mContext, clazz);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, requestCode, intent, flags); // getActivity跳转到一个activity组件

        // 创建通知
        Notification notification = new NotificationCompat.Builder(mContext).setContentTitle(title)
                .setContentText(content).setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent).setAutoCancel(true) // 设置跳转和自动取消
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // 设置消息声音
                .setVibrate(new long[]{100, 200, 100, 200}) // 设置震动no
                .setLights(Color.rgb(0,0,255),5000,5000) // 设置呼吸灯
                .build();
        manager.notify(notifyId++, notification);
        Log.w(TAG, "推送通知："+title+"|"+content);
    }
}
