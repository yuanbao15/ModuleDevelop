package com.epichust.nfc;

import android.content.Intent;
import android.util.Log;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuanbao on 2019/3/21
 */
public class NFC15693Demo extends UZModule {
    public static UZModuleContext mModuleContext;
    public static JSONObject ret = null; // 调用js方法返回的结果

    public NFC15693Demo(UZWebView webView) {
        super(webView);
    }

    /**
     * @methodName    读取NFC标签，传参为读单个模块的模块位置
     *
     * @author  yuanbao
     * @date    2019/3/22
     */
    public void jsmethod_readNFC(final UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        ret = null; // 撤掉销毁，并清空内存，防止第二次读取时被第一次数据干扰
        // 接收到参：起始位置、长度
        String blockStr = moduleContext.optString("blockIndex");
        String blockNumStr = moduleContext.optString("blockNum");
        int blockIndex = blockStr==""? 0:Integer.parseInt(blockStr); // 默认起始位置0
        int blockNum = blockNumStr==""? 1:Integer.parseInt(blockNumStr); // 默认读块数量1

        // --回调结果
        Log.w("readNFC","-------读取初始化1");
        Intent intent = new Intent(getContext(), NFCReadActivity.class);
        intent.putExtra("blockIndex", blockIndex);
        intent.putExtra("blockNum", blockNum);
        startActivityForResult(intent,1);

        new Thread(){
            @Override
            public void run() {
                //需要在子线程中处理的逻辑
                while (true){
                    try {
                        if (ret != null && ret.getBoolean("readFlag")){
                            ret.put("readFlag", false);
                            moduleContext.success(ret, true);
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
        }.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 2) {
            if (requestCode == 1) {
                //                Tag tag = intent.getParcelableExtra("tag");

                String uid = intent.getStringExtra("uid");
                String tech = intent.getStringExtra("tech");
                String info = intent.getStringExtra("info");
                String data = intent.getStringExtra("data");
                Log.w("readNFC", "----------uid:" + uid + ",tech:" + tech + ",info:" + info + ",data:" + data);

                // --回调结果
                ret = new JSONObject();
                try {
                    ret.put("uid", uid);
                    ret.put("tech", tech);
                    ret.put("info", info);
                    ret.put("data", data);
                    ret.put("readFlag", true); // 作为是否新解析到的标志位
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
