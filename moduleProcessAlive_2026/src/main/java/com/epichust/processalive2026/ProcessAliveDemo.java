package com.epichust.processalive2026;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 进程守护模块入口（2026 版）
 * 方法名与旧模块 moduleProcessAlive 保持一致：
 * jsmethod_startAlive / jsmethod_stopAlive
 *
 * @author yuanbao
 * @date 2026-08-16
 */
public class ProcessAliveDemo extends UZModule {
    public static UZModuleContext mModuleContext;

    public ProcessAliveDemo(UZWebView webView) {
        super(webView);
    }

    /**
     * 开启进程常驻（前台服务常驻 + 被杀自动恢复）
     */
    public void jsmethod_startAlive(UZModuleContext moduleContext) {
        mModuleContext = moduleContext;
        ProcessManager manager = ProcessManager.getInstance();
        manager.startAlive(this.getContext());
        callbackSuccess(moduleContext);
    }

    /**
     * 关闭进程常驻
     */
    public void jsmethod_stopAlive(UZModuleContext moduleContext) {
        mModuleContext = moduleContext;
        ProcessManager manager = ProcessManager.getInstance();
        manager.stopAlive(this.getContext());
        callbackSuccess(moduleContext);
    }

    private void callbackSuccess(UZModuleContext moduleContext) {
        try {
            JSONObject ret = new JSONObject();
            ret.put("status", "success");
            moduleContext.success(ret, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
