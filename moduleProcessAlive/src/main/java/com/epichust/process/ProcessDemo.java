package com.epichust.process;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

/**
 * Created by yuanbao on 2019/7/17
 */
public class ProcessDemo extends UZModule {
    public static UZModuleContext mModuleContext;

    public ProcessDemo(UZWebView webView) {
        super(webView);
    }

    /**
     * @method    开启进程常驻
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/7/17 
     */
    public void jsmethod_startAlive(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        ProcessManager manager = ProcessManager.getInstance();
        manager.startAlive(this.getContext());
    }
    /**
     * @method    关闭线程常驻，未成功
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/7/17 
     */
    public void jsmethod_stopAlive(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        ProcessManager manager = ProcessManager.getInstance();
        manager.stopAlive(this.getContext());
    }
}
