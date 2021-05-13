package com.szyd.moduleuhf_50;


import android.content.Context;
import android.os.SystemClock;
import android.util.Log;


import java.util.HashMap;
import java.util.Map;

import realid.rfidlib.MyLib;

/**
 * UHFReadWrite:作为调用方法的管理类，提供方法去读写
 * <p>
 * Created by yuanbao on 2019/5/20.
 */
public class UHFReadWrite {
    private MyLib mb;

    /**
     * 初始化方法，由于作为library没有使用application，需要传入上下文
     *
     * @param context 上下文
     */
    public void initUHFModule(Context context) throws Exception {
        mb = new MyLib(context);
        System.out.println("poweron--------" + mb.powerOn());
        System.out.println("powerSet--------" + mb.powerSet(30));
        System.out.println("readTagModeGet--------" + mb.readTagModeGet());
    }

    // 读标签接口
    public Map<String, String> readUHF() {
        Map<String, String> resultMap = new HashMap<>();
        System.out.println("start-------" + mb.startInventoryTag());
        String[] tagData = null;
        int maxWhileQty = 0;
        while (tagData == null && maxWhileQty < 10) {
            SystemClock.sleep(100);
            tagData = mb.readTagFromBuffer();
            maxWhileQty++;
        }
        Log.w("idata50", "读取EPC");
        System.out.println("end-------" + mb.stopInventory());
        if (tagData != null && tagData.length > 0) {
            DevBeep.PlayOK();
            resultMap.put("flag", "yes");
            resultMap.put("epc", tagData[1]);
            resultMap.put("info", tagData[2]);
        } else {
            DevBeep.PlayErr();
            resultMap.put("flag", "no");
            resultMap.put("epc", "EPC读取失败");
        }
        System.out.println("result--------" + resultMap.get("epc"));
        return resultMap;
    }

    public Map<String, String> readUHFData(Context context, int startIndex, int length, int memIndex) {
        String pwd = "00000000";
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> epcMap = this.readUHF();
        if (epcMap.get("flag").equals("yes")) {
            String data = mb.readTag(pwd, 0, 0, 0, "0", memIndex, startIndex, length);
            SystemClock.sleep(100);
            System.out.println("readTagFromBuffer--------" + mb.readTagFromBuffer());
            Log.w("idata50", "读取数据" + data);
            if (data != null) {
                DevBeep.PlayOK();
                resultMap.put("flag", "yes");
                resultMap.put("epc", epcMap.get("epc"));
                resultMap.put("info", StringUtility.hexStringToString(data));
            } else {
                DevBeep.PlayErr();
                resultMap.put("flag", "no");
                resultMap.put("info", "读取失败");
            }
        }
        return resultMap;
    }

    // 写标签接口
    public Map<String, String> writeUHFData(Context context, int memIndex, int startIndex, int length, String str) {
        String pwd = "00000000";
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> epcMap = this.readUHF();
        if (epcMap.get("flag").equals("yes")) {
            boolean flag = mb.writeTag(pwd, 1, 32, 96, epcMap.get("epc"), memIndex, startIndex, length, StringUtility.stringToHexString(str));
            System.out.println("resultdata--------" + flag);
            if (flag) {
                DevBeep.PlayOK();
                resultMap.put("flag", "yes");
                resultMap.put("epc", epcMap.get("epc"));
                resultMap.put("info", "写入成功");
            } else {
                DevBeep.PlayErr();
                resultMap.put("flag", "no");
                resultMap.put("epc", epcMap.get("epc"));
                resultMap.put("info", "写入失败");
            }
        }
        return resultMap;
    }

    public void recyleUHFModule() {
        System.out.println("endInventory-------" + mb.stopInventory());
        System.out.println("poweroff--------" + mb.powerOff());
    }
}
