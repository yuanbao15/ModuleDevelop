package com.apicloud.moduleapi;

import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

/**
 * Created by yuanbao on 2018/4/16.
 */

public class ApiDemo extends UZModule{

    public ApiDemo(UZWebView webView) {
        super(webView);
    }

    //jsmethod_方法名，可被api调用。这里的方法名就是在html中的方法可传递msg
    //tips:引擎默认在UI线程中操作该函数,不能作耗时操作
    public void jsmethod_testApi(UZModuleContext moduleContext){
        String msg = moduleContext.optString("msg");
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


}
