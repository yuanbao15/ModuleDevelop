package com.epichust.modulepdf;


import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.ybUtils.JsParamsUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 该类映射至Javascript中moduleDemo对象
 * <strong>Js Example:</strong>
 * var module = api.require('modulePdf');
 * module.xxx();
 * Created by yuanbao on 2018/4/17.
 * @author yuanbao
 *
 */

public class PdfDemo extends UZModule {

    public static UZModuleContext mModuleContext;
    public static JsParamsUtil mJsParamsUtil;
    public PdfDemo(UZWebView webView) {
        super(webView);
    }
    //jsmethod_方法名，可被api调用。这里的方法名就是在html中的方法可传递msg
    //tips:引擎默认在UI线程中操作该函数,不能作耗时操作
    public void jsmethod_test(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        String msg = moduleContext.optString("msg");
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();

    }

    /**
     * <strong>函数</strong><br><br>
     * 该函数映射至Javascript中moduleDemo对象的startActivity函数<br><br>
     * <strong>JS Example：</strong><br>
     * modulePdf.startActivity(argument);
     *
     * @param moduleContext  (Required)
     */
    public void jsmethod_startActivity(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
//        Toast.makeText(mContext, "开启pdf读写模块", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), PdfActivity.class);
//        decode(moduleContext);
//        intent.putExtra("appParam", moduleContext.optString("appParam"));     //原来的，现在传递文件路径
        intent.putExtra("filePath", moduleContext.optString("filePath"));
        startActivity(intent);
    }

    /*private void decode(UZModuleContext moduleContext) {
        String filePath = mJsParamsUtil.decodePath(moduleContext);
        if (TextUtils.isEmpty(filePath)) {
            selectImgFromSystem();
        } else {
            decodeImg(filePath);
        }
    }

    private void encodeCallBack(UZModuleContext moduleContext, String savePath,
                                String albumPath) {
        JSONObject ret = new JSONObject();
        try {
            ret.put("imgPath", savePath);
            if (albumPath != null)
                ret.put("albumPath", albumPath);
            ret.put("status", true);
            moduleContext.success(ret, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void decodeImg(String imgPath) {
        imgPath = UZUtility.makeRealPath(imgPath, getWidgetInfo());
        final String path = makeRealPath(imgPath);
        if (!TextUtils.isEmpty(path)) {
            new Thread(new Runnable() {
                public void run() {
                    Result result = ScannerDecoder.decodeBar(path);
                    mBeepUtil.playBeepSoundAndVibrate();
                    if (result == null) {
                        decodeCallBack(false, null);
                    } else {
                        decodeCallBack(true, result.toString());
                    }
                }
            }).start();
        }
    }*/


}
