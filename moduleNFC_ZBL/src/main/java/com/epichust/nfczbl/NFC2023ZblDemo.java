package com.epichust.nfczbl;

import android.content.Intent;
import android.util.Log;
import com.epichust.nfczbl.NFCReadActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import org.json.JSONException;
import org.json.JSONObject;

/***
 * @ClassName: NFC2023ZblDemo
 * @Description: NFC自定义模块的调用方法类：对内存块进行读写操作，协议为ISO-15693
 * @Author: yuanbao
 * @Date: 2023/12/21
 **/
public class NFC2023ZblDemo extends UZModule
{
    public static UZModuleContext mModuleContext;
    public static JSONObject ret = null; // 调用js方法返回的结果

    public NFC2023ZblDemo(UZWebView webView)
    {
        super(webView);
    }

    /**
     * @MethodName: jsmethod_readNFC
     * @Description: 读取NFC标签-15693，可读取任意数量的块，不仅限单块，设置读取起始块位置、数据类型为16进制或UTF。返回值中会额外携带UID地址码。
     * @Param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/12/21
     **/
    public void jsmethod_readNFC(final UZModuleContext moduleContext)
    {
        mModuleContext = moduleContext;
        ret = null; // 撤掉销毁，并清空内存，防止第二次读取时被第一次数据干扰
        // 接收到参：起始位置、长度
        String blockStr = moduleContext.optString("blockIndex");
        String blockNumStr = moduleContext.optString("blockNum");
        String dataTypeStr = moduleContext.optString("dataType");
        int blockIndex = blockStr == "" ? 0 : Integer.parseInt(blockStr); // 默认起始位置0
        int blockNum = blockNumStr == "" ? 1 : Integer.parseInt(blockNumStr); // 默认读块数量1
        int dataType = dataTypeStr == "" ? 0 : Integer.parseInt(dataTypeStr); // 默认数据类型0为16进制，1为utf8

        // --回调结果
        Log.w("readNFC", "-------读取初始化1");
        Intent intent = new Intent(getContext(), NFCReadActivity.class);
        intent.putExtra("blockIndex", blockIndex);
        intent.putExtra("blockNum", blockNum);
        intent.putExtra("dataType", dataType);
        startActivityForResult(intent, 1);

        new Thread()
        {
            @Override
            public void run()
            {
                //需要在子线程中处理的逻辑
                while (true)
                {
                    try
                    {
                        if (ret != null && ret.has("readFlag") && ret.getBoolean("readFlag"))
                        {
                            ret.put("readFlag", false);
                            moduleContext.success(ret, true);
                            break;
                        }
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
        }.start();

    }

    /**
     * @MethodName: jsmethod_writeNFC
     * @Description: 写值到NFC标签-15693，设置写入起始块位置、块数、待写入的数据、数据类型为16进制或UTF
     * @Param moduleContext
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/12/21
     **/
    public void jsmethod_writeNFC(final UZModuleContext moduleContext)
    {
        mModuleContext = moduleContext;
        ret = null; // 撤掉销毁，并清空内存，防止第二次读取时被第一次数据干扰
        // 接收到参：起始位置、长度
        String blockStr = moduleContext.optString("blockIndex");
        String blockNumStr = moduleContext.optString("blockNum"); // 设置写入的块数。 写入字节数/4 不超过块数时，按实际数据的长度进行写入；超出块数时，进行截断只取块数内的长度的数值
        String blockData = moduleContext.optString("blockData"); // 待写入的数据
        String dataTypeStr = moduleContext.optString("dataType");
        int blockIndex = blockStr == "" ? 0 : Integer.parseInt(blockStr); // 默认起始位置0
        int blockNum = blockNumStr == "" ? 1 : Integer.parseInt(blockNumStr); // 默认写块数量1
        int dataType = dataTypeStr == "" ? 0 : Integer.parseInt(dataTypeStr); // 默认数据类型0为16进制，1为utf8

        // --回调结果
        Log.w("writeNFC", "-------写入初始化1");
        Intent intent = new Intent(getContext(), NFCWriteActivity.class);
        intent.putExtra("blockIndex", blockIndex);
        intent.putExtra("blockNum", blockNum);
        intent.putExtra("blockData", blockData);
        intent.putExtra("dataType", dataType);
        startActivityForResult(intent, 1);

        new Thread()
        {
            @Override
            public void run()
            {
                //需要在子线程中处理的逻辑
                while (true)
                {
                    try
                    {
                        if (ret != null && ret.has("readFlag") && ret.getBoolean("readFlag"))
                        {
                            ret.put("readFlag", false);
                            moduleContext.success(ret, true);
                            break;
                        }
                    } catch (JSONException e)
                    {
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
     * @Date: 2023/12/21
     **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 2)
        {
            if (requestCode == 1)
            {
                //                Tag tag = intent.getParcelableExtra("tag");

                String uid = intent.getStringExtra("uid");
                String tech = intent.getStringExtra("tech");
                String info = intent.getStringExtra("info");
                String data = intent.getStringExtra("data");
                Boolean operateStatus = intent.getBooleanExtra("operateStatus", false); // 记录操作是否成功，布尔值
                String operateMsg = intent.getStringExtra("operateMsg"); // 操作失败时给出失败信息
                Log.w("readNFC", "----------uid:" + uid + ",tech:" + tech + ",info:" + info + ",data:" + data
                        + ",operateStatus:" + operateStatus + ",operateMsg:" + operateMsg);

                // --回调结果
                ret = new JSONObject();
                try
                {
                    ret.put("uid", uid);
                    ret.put("tech", tech);
                    ret.put("info", info);
                    ret.put("data", data);
                    ret.put("operateStatus", operateStatus);
                    ret.put("operateMsg", operateMsg);
                    ret.put("readFlag", true); // 作为是否新解析到的标志位-无实际意义，仅用于正确返回时机。
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
