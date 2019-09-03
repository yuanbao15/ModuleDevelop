package com.epichust.notification;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuanbao on 2019/7/18
 */
public class NotificationDemo extends UZModule {
    public static UZModuleContext mModuleContext;

    public NotificationDemo(UZWebView webView) {
        super(webView);
    }

    public void jsmethod_showNotification(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：标题、文本内容
        String titleStr = moduleContext.optString("title");
        String contentStr = moduleContext.optString("content");

        YbNotificationManager ybNotificationManager = YbNotificationManager.getInstance();
        String title = titleStr!=null? titleStr:"元宝哥哥";
        String content = contentStr!=null? contentStr:"元宝哥哥爆红网络";

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            // UZModule.getContext()：获取当前模块运行所在的Activity的上下文
            // 第二个参为本activity的class，用于跳转打开这个activity
            ybNotificationManager.showNotification(mModuleContext.getContext(), getContext().getClass(), title, content);

            ret.put("status", true);
        } catch (JSONException e) {
            try {
                ret.put("status", false);
                ret.put("errmsg", "模块调用失败："+e.getMessage());
            }catch (Exception e2){
            }
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }
}
