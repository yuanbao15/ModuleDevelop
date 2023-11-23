package com.epichust.nfc_2023;

import android.content.Intent;
import android.util.Log;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import org.json.JSONException;
import org.json.JSONObject;

/***
 * @ClassName: NFC2023Demo
 * @Description: NFC自定义模块的调用方法类：此支持标签文本的读写，协议为ISO-14443B
 * @Author: yuanbao
 * @Date: 2023/11/22
 **/
public class NFC2023Demo extends UZModule {
    public static UZModuleContext mModuleContext;
    public static JSONObject ret = null; // 调用js方法返回的结果

    public NFC2023Demo(UZWebView webView) {
        super(webView);
    }


    /**
     * @MethodName: jsmethod_readNFC
     * @Description: 读取NFC标签，不需要传参直接读标签上的文本信息
     * @Param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/11/17
     **/
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
                        if (ret != null && ret.has("readFlag") && ret.getBoolean("readFlag")){
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

    /***
     * @MethodName: jsmethod_writeNFC
     * @Description: 写入NFC标签，会将原始文本信息覆盖。（原有的删掉，写入新的，不受长度约束）
     * @Param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/11/17
     **/
    public void jsmethod_writeNFC(final UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        ret = null; // 撤掉销毁，并清空内存，防止第二次读取时被第一次数据干扰
        // 接收到参：待写入的文本
        String text = moduleContext.optString("text");

        // --回调结果
        Log.w("writeNFC","-------写入初始化1");
        Intent intent = new Intent(getContext(), NFCWriteActivity.class);
        intent.putExtra("text", text);
        startActivityForResult(intent,1);

        new Thread(){
            @Override
            public void run() {
                //需要在子线程中处理的逻辑
                while (true){
                    try {
                        if (ret != null && ret.has("readFlag") && ret.getBoolean("readFlag")){
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

    /**
     * @MethodName: onActivityResult
     * @Description: 重写intent回调方法，将读取标签的activity里读到的内容带回来
     * @Param requestCode
     * @Param resultCode
     * @Param intent
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/11/22
     **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 2) {
            if (requestCode == 1) {
//                Tag tag = intent.getParcelableExtra("tag");

                Boolean operateStatus = intent.getBooleanExtra("operateStatus", false);
                String operateMsg = intent.getStringExtra("operateMsg");
                String info = intent.getStringExtra("info");
                String data = intent.getStringExtra("data");
                Log.w("readNFC-result", "-------operateStatus:" + operateStatus + ",operateMsg:" + operateMsg + ",info:" + info + ",data:" + data);

                // --回调结果
                ret = new JSONObject();
                try {
                    ret.put("operateStatus", operateStatus);
                    ret.put("operateMsg", operateMsg);
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
