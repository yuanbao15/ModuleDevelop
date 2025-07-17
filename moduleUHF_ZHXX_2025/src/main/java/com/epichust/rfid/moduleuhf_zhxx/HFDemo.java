package com.epichust.rfid.moduleuhf_zhxx;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.magicrf.uhfreader.MainActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @ClassName: HFDemo
 * @Description: Apicloud的接口提供类：重汽项目APP 高瓶NFC读写模块封装 <br>
 *      专用于中航信息的PDA，需要通过串口调用NFC模块进行读写. 协议为ISO-14443-3A.
 *      读写块固定为扇区1块2，即块号06。单块内包含16字节/32位16进制。
 * @Author: yuanbao
 * @Date: 2025/7/11
 **/
public class HFDemo extends UZModule
{
    public static UZModuleContext mModuleContext;
    public HFDemo(UZWebView webView) {
        super(webView);
    }

    //jsmethod_方法名，可被api调用。这里的方法名就是在html中的方法可传递msg
    //tips:引擎默认在UI线程中操作该函数,不能作耗时操作
    public void jsmethod_test(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        String msg = moduleContext.optString("msg");
        Toast.makeText(mContext, "模块调用联通：" + msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * @MethodName: jsmethod_startActivity
     * @Description: 跳转打开页面
     * @param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void jsmethod_startActivity(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        Log.w("onCreate","自定义模块接收的参数："+moduleContext.optString("name"));

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("name", moduleContext.optString("name"));
        startActivity(intent);
    }

    /**
     * @MethodName: jsmethod_initUHF
     * @Description: 初始化模块，包含串口初始化
     * @param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void jsmethod_initUHFModule(UZModuleContext moduleContext){
        mModuleContext = moduleContext;

        // --回调结果
        JSONObject ret = new JSONObject();
        try {

            Log.w("initUHF","-------初始化1");
            HFReadWrite uhf = new HFReadWrite();
            uhf.initUHFModule(this.getContext());
            Log.w("initUHF","-------初始化2");
            Toast.makeText(mContext, "模块初始化成功", Toast.LENGTH_SHORT).show();

            ret.put("status", "");
            moduleContext.success(ret, true);
        } catch (JSONException e) {
            Log.e("initUHF", "error:" + e.getMessage());
            moduleContext.error(ret, true);
        }
    }

    /**
     * readUHF:读取UHF标签的信息，先初始化，再根据传参读取
     * @param moduleContext
     */
    public void jsmethod_readUHF(UZModuleContext moduleContext){
        mModuleContext = moduleContext;

        // 前端传参值类型：UTF8或16进制
        String dataTypeStr = moduleContext.optString("dataType");
        int dataType = dataTypeStr == "" ? 0 : Integer.parseInt(dataTypeStr); // 默认数据类型0为16进制，1为utf8

        Log.w("readUHF","-------读取初始化1");
        HFReadWrite uhf = new HFReadWrite();
        uhf.initUHFModule(this.getContext());
        Log.w("readUHF","-------读取初始化2");

        // 异步回调方式
        uhf.readBlock(dataType, new HFReadWrite.OnReadBlockListener() {
            @Override
            public void onReadBlockResult(Map<String, String> map) {
                String flag = map.get("flag");
                String epc = map.get("epc");
                String info = map.get("info");
                Log.w("readUHF","-------读取后flag:" + flag);
                Log.w("readUHF","-------读取后epc:" + epc);
                Log.w("readUHF","-------读取后info:" + info);
//                Toast.makeText(mContext, "flag:"+flag+"\nepc:"+epc+"\ninfo:"+info, Toast.LENGTH_SHORT).show();

                // --回调结果
                JSONObject ret = new JSONObject();
                try {
                    ret.put("flag", flag);
                    ret.put("epc", epc);
                    ret.put("info", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                moduleContext.success(ret, true);
            }
        });
    }

    /**
     * writeUHF:写入到电子标签
     * @param moduleContext
     */
    public void jsmethod_writeUHF(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：写入的信息
        String str = moduleContext.optString("str");
        // 前端传参值类型：UTF8或16进制
        String dataTypeStr = moduleContext.optString("dataType");
        int dataType = dataTypeStr == "" ? 0 : Integer.parseInt(dataTypeStr); // 默认数据类型0为16进制，1为utf8

        Log.w("writeUHF","-------写入初始化1");
        HFReadWrite uhf = new HFReadWrite();
        uhf.initUHFModule(this.getContext());
        Log.w("writeUHF","-------写入初始化2");

        // 异步回调方式
        uhf.writeData(str, dataType, new HFReadWrite.OnWriteBlockListener() {
            @Override
            public void onWriteBlockResult(Map<String, String> map) {
                String flag = map.get("flag");
                String epc = map.get("epc");
                String info = map.get("info");
                Log.w("writeUHF","-------写入后flag:" + flag);
                Log.w("writeUHF","-------写入后epc:" + epc);
                Log.w("writeUHF","-------写入后info:" + info);
//                Toast.makeText(mContext, "flag:"+flag+"\nepc:"+epc+"\ninfo:"+info, Toast.LENGTH_SHORT).show();

                // --回调结果
                JSONObject ret = new JSONObject();
                try {
                    ret.put("flag", flag);
                    ret.put("epc", epc);
                    ret.put("info", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                moduleContext.success(ret, true);
            }
        });
    }

    /**
     * @MethodName: jsmethod_readEPC
     * @Description: 获取EPC标签编码的信息
     * @param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/16
     **/
    public void jsmethod_readEPC(UZModuleContext moduleContext){
        mModuleContext = moduleContext;

        Log.w("readEPC","-------读取初始化1");
        HFReadWrite uhf = new HFReadWrite();
        uhf.initUHFModule(this.getContext());
        Log.w("readEPC","-------读取初始化2");

        // 异步回调方式
        uhf.readEPC( new HFReadWrite.OnReadEPCListener() {
            @Override
            public void onReadEPCResult(Map<String, String> map) {
                String flag = map.get("flag");
                String epc = map.get("epc");
                String info = map.get("info");
                Log.w("readEPC","-------读取后flag:" + flag);
                Log.w("readEPC","-------读取后epc:" + epc);
                Log.w("readEPC","-------读取后info:" + info);
                //                Toast.makeText(mContext, "flag:"+flag+"\nepc:"+epc+"\ninfo:"+info, Toast.LENGTH_SHORT).show();

                // --回调结果
                JSONObject ret = new JSONObject();
                try {
                    ret.put("flag", flag);
                    ret.put("epc", epc);
                    ret.put("info", info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                moduleContext.success(ret, true);
            }
        });
    }
}