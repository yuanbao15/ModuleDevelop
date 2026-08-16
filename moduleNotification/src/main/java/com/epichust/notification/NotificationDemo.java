package com.epichust.notification;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import org.json.JSONObject;

/**
 * 消息通知模块入口（UZModule），接收 JS 调用并委托 YbNotificationManager 发送通知
 *   （最早版本 2019/7/18，后经多次迭代升级）
 * @author yuanbao
 * @date 2026-08-16
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

        // 可选参数：渠道、铃声开关、震动开关、震动时长
        String channelId = moduleContext.optString("channelId");
        String channelName = moduleContext.optString("channelName");
        boolean soundEnabled = moduleContext.optBoolean("soundEnabled", true);
        boolean vibrateEnabled = moduleContext.optBoolean("vibrateEnabled", true);
        // 闪光灯模式：0=不闪（默认），1=闪两下停2s再闪两下结束，2=闪两下停2s循环持续60s
        int flashMode = moduleContext.optInt("flashMode", 0);
        int vibrateDuration = moduleContext.optInt("vibrateDuration", 0);

        YbNotificationManager ybNotificationManager = YbNotificationManager.getInstance();
        String title = titleStr!=null? titleStr:"元宝哥哥";
        String content = contentStr!=null? contentStr:"元宝哥哥爆红网络";

        // 组装配置
        YbNotificationManager.Config config = new YbNotificationManager.Config();
        config.channelId = channelId != null && channelId.length() > 0 ? channelId : null;
        config.channelName = channelName != null && channelName.length() > 0 ? channelName : null;
        config.soundEnabled = soundEnabled;
        config.vibrateEnabled = vibrateEnabled;
        config.flashMode = flashMode;
        config.vibrateDurationSec = vibrateDuration;

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            // UZModule.getContext()：获取当前模块运行所在的Activity的上下文
            // 第二个参为本activity的class，用于跳转打开这个activity
            ybNotificationManager.showNotification(mModuleContext.getContext(), getContext().getClass(), title, content, config);

            ret.put("status", true);
        } catch (Exception e) {
            try {
                ret.put("status", false);
                ret.put("errmsg", "模块调用失败："+e.getMessage());
            }catch (Exception e2){
            }
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

    /**
     * 终止正在循环的震动和闪光灯闪烁
     * <p>若之前推送的通知还在循环震动或循环闪烁闪光灯（如模式2未到时长），调用此方法立即停止
     * <p>调用方式：moduleNotification.closeVibrateFlash(callback)
     */
    public void jsmethod_closeVibrateFlash(UZModuleContext moduleContext){
        mModuleContext = moduleContext;

        JSONObject ret = new JSONObject();
        try {
            YbNotificationManager.getInstance().closeVibrateFlash(getContext());
            ret.put("status", true);
        } catch (Exception e) {
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
