package com.epichust.rfid;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.cus.maguhf.ReadWriteActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 该类映射至Javascript中moduleDemo对象
 *
 * var module = api.require('moduleUHF');
 * module.xxx();
 * Created by yuanbao on 2019/3/17.
 */
public class UHFDemo extends UZModule {
    public static UZModuleContext mModuleContext;
    public UHFDemo(UZWebView webView) {
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
        Log.w("onCreate","自定义模块接收的参数："+moduleContext.optString("name"));

        Intent intent = new Intent(getContext(), ReadWriteActivity.class);
        intent.putExtra("name", moduleContext.optString("name"));
        startActivity(intent);
    }

    /**
     * readUHF:读取UHF标签的信息，先初始化，再根据传参读取
     * @param moduleContext
     */
    public void jsmethod_readUHF(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：起始位置、长度
        String startStr = moduleContext.optString("startIndex");
        String lengthStr = moduleContext.optString("length");
        int startIndex = startStr==""? 0:Integer.parseInt(startStr); // 默认起始位置0
        int length = lengthStr==""? 6:Integer.parseInt(lengthStr); // 默认长度为6

        Log.w("readUHF","-------读取初始化1");
        UHFReadWrite uhf = new UHFReadWrite();
        uhf.initUHFModule(this.getContext());
        Log.w("readUHF","-------读取初始化2");
        Map<String, String> map = uhf.readUHF(startIndex, length);
        String flag = map.get("flag");
        String epc = map.get("epc");
        String info = map.get("info");
        Log.w("readUHF","-------读取后flag:" + flag);
        Log.w("readUHF","-------读取后epc:" + epc);
        Log.w("readUHF","-------读取后info:" + info);
        Toast.makeText(mContext, "flag:"+flag+"\nepc:"+epc+"\ninfo:"+info, Toast.LENGTH_SHORT).show();

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            ret.put("data", map);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

    /**
     * writeUHF:写入到电子标签
     * @param moduleContext
     */
    public void jsmethod_writeUHF(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：写入的信息
        String str = moduleContext.optString("str");
        String startStr = moduleContext.optString("startIndex");
        String lengthStr = moduleContext.optString("length");
        int startIndex = startStr==""? 0:Integer.parseInt(startStr); // 默认起始位置0
        int length = lengthStr==""? 6:Integer.parseInt(lengthStr); // 默认长度为6

        Log.w("writeUHF","-------写入初始化1");
        UHFReadWrite uhf = new UHFReadWrite();
        uhf.initUHFModule(this.getContext());
        Log.w("writeUHF","-------写入初始化2");
        Map<String, String> map = uhf.writeUHF(startIndex, length, str);
        String flag = map.get("flag");
        String epc = map.get("epc");
        String info = map.get("info");
        Log.w("writeUHF","-------写入后flag:" + flag);
        Log.w("writeUHF","-------写入后epc:" + epc);
        Log.w("writeUHF","-------写入后info:" + info);
        Toast.makeText(mContext, "flag:"+flag+"\nepc:"+epc+"\ninfo:"+info, Toast.LENGTH_SHORT).show();

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            ret.put("data", map);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }


    /*-------------------写的非正式的方法为了匹配app上原有的rfid模块接口------------------------*/
    public void jsmethod_readFrom6C(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：起始位置、长度
        String startStr = moduleContext.optString("startAddr");
        String lengthStr = moduleContext.optString("length");
        int startIndex = startStr==""? 0:Integer.parseInt(startStr); // 默认起始位置0
        int length = lengthStr==""? 20:Integer.parseInt(lengthStr); // 默认长度为20

        UHFReadWrite uhf = new UHFReadWrite();
        uhf.initUHFModule(this.getContext());
        Map<String, String> map = uhf.readUHF(startIndex, length);
        String flag = map.get("flag");
        String epc = map.get("epc");
        String info = map.get("info");

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            if (flag.equals("yes"))
                ret.put("status",true);
            else
                ret.put("status",false);
            ret.put("data", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

    public void jsmethod_writeTo6C(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // 接收到参：起始位置、长度
        String startStr = moduleContext.optString("startAddr");
        String lengthStr = moduleContext.optString("length");
        int startIndex = startStr==""? 0:Integer.parseInt(startStr); // 默认起始位置0
        int length = lengthStr==""? 20:Integer.parseInt(lengthStr); // 默认长度为20
        String str = moduleContext.optString("data");

        UHFReadWrite uhf = new UHFReadWrite();
        uhf.initUHFModule(this.getContext());
        Map<String, String> map = uhf.writeUHF(startIndex, length, str);
        String flag = map.get("flag");
        String epc = map.get("epc");
        String info = map.get("info");

        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            if (flag.equals("yes"))
                ret.put("status",true);
            else
                ret.put("status",false);
            ret.put("data", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }

    public void jsmethod_setOutputPower(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            ret.put("status", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }
    public void jsmethod_getInstance(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            ret.put("status", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }
    public void jsmethod_powerOn(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        // --回调结果
        JSONObject ret = new JSONObject();
        try {
            ret.put("status", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
    }
}
