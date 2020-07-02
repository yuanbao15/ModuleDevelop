package com.yuanbao.rfid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;
import com.handheld.uhfdemo1.EPC;
import com.handheld.uhfdemo1.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pda.serialport.Tools;

/**
 * UHFReadWrite:作为调用方法的管理类，提供方法去读写
 *
 * Created by yuanbao on 2019/3/18.
 */
public class UHFReadWrite {
    Context mContext;
    private ArrayList<EPC> listEPC;
    private ArrayList<String> listepc;
    private boolean runFlag = true;
    private boolean startFlag = false;
    private UhfReader manager;

    // RESERVE EPC TID USER:0,1,2,3
    private final String[] strMemBank = { "RESERVE", "EPC", "TID", "USER" };
    /************************************/
    private ArrayAdapter<String> adatpterMemBank;

    private String[] powers = {"26dbm","24dbm","20dbm","18dbm","17dbm","16dbm"};
    //private String[] powers = {"26dbm","25dm","24dbm","23dbm","22dbm","21dbm","20dbm","19dbm","18dbm","17dbm","16dbm"};
    private String[] sensitives = null;

    private String[] lockMemArrays = {"Kill Password", "Access password", "EPC", "TID", "USER"} ;
    private int power = 0 ;//rate of work
    private int area = 0;

    private SharedPreferences shared;
    private SharedPreferences.Editor editor;


    // *******************************************************
    byte btMemBank = 0x03;
    // *******************************************************
    String epcCode = ""; // 标签编码
    String readResult = ""; // 读取结果
    // *******************************************************

    /**
     * 初始化UHF模块方法，由于作为library没有使用application，需要传入上下文
     * @param context 上下文
     */
    public void initUHFModule(Context context){
        this.mContext = context;
        listEPC = new ArrayList<EPC>(); // 存放epc对象
        listepc = new ArrayList<String>(); // 存放epcCode
        // init sound pool
        Util.initSoundPool(context);
        SystemClock.sleep(200);
//        Util.play(1, 0); // 音频test

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // Get the Rf power, and set
        shared = context.getSharedPreferences("UhfRfPower", 0);
        editor = shared.edit();
        power = shared.getInt("power", 26);
        area = shared.getInt("area", 2);

        manager = UhfReader.getInstance();
        if (manager == null) {
            String text = "模块初始化失败！";
            showToast(text);
            return;
        }
//		Log.e("", "value" + power);
        manager.setOutputPower(power);
        manager.setWorkArea(area);


        DevBeep.init(context);
        Log.w("onCreate", "--------模块初始化--yes");
    };

    public Map<String, String> readUHF(int startIndex, int length, int memIndex){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 先获取到标签信息
            // 由于是开启多线程去读的，等读到后再放回结果
            Thread thread = new InventoryThread();
            thread.start();

            thread.join();
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.w("readUHF","读的标签"+ listepc.toString());
        // 进行读操作
        int mresult = readLable(startIndex, length, memIndex);
        if(mresult==0){
            DevBeep.PlayOK();
            resultMap.put("flag","yes");
            resultMap.put("epc", epcCode);
            resultMap.put("info", readResult);
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("epc", epcCode==""? "无标签":epcCode);
            resultMap.put("info", readResult);
        }

        return resultMap;
    }

    public Map<String, String> writeUHF(int startIndex, int length, String str, int memIndex){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 先获取到标签信息
            // 由于是开启多线程去读的，等读到后再放回结果
            Thread thread = new InventoryThread();
            thread.start();

            Thread.sleep(400);
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.w("writeUHF","写的标签"+epcCode);
        // 进行读操作
        int mresult = writeLable(startIndex, length, str, memIndex);
        if(mresult==0){
            DevBeep.PlayOK();
            resultMap.put("flag","yes");
            resultMap.put("epc", epcCode);
            resultMap.put("info", "写入成功");
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("epc", epcCode==""? "无标签":epcCode);
            resultMap.put("info","写入失败");
        }

        return resultMap;
    }


    /**
     * 子线程用于获取epcCode，拿到标签
     */
    class InventoryThread extends Thread {
        private List<TagModel> tagList;

        @Override
        public void run() {
            super.run();

            // 读取标签，获取编码信息
            tagList = manager.inventoryRealTime();
            if(tagList != null && tagList.size()>0){
                TagModel tag = tagList.get(0); // 取一个即可
                String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
                byte rssi = tag.getmRssi();

                EPC epcTag = new EPC();
                epcTag.setEpc(epcStr);
                epcTag.setCount(1);
                epcTag.setRssi(rssi);
                listEPC.add(epcTag);
                listepc.add(epcStr);
            }
            tagList = null ;
        }
    }

    /**
     * readLable：读取标签信息，要求先取到标签
     * @return
     */
    private int readLable(int startIndex, int length, int memIndex){
        if (listepc.size()<=0) {
            epcCode = "";
            readResult = "未识别标签!";
            return -1;
        }
        epcCode = listepc.get(0);
        if ("".equals(epcCode)){
            return -1; //Please select the EPC tags
        }

        int nadd = startIndex;
        int ndatalen = length;
        int membank = memIndex; // 0123分别对应RESEVER/EPC/TID/USER

        String mimaStr = "00000000"; //密码8个0
        byte[] passwB = stringToBytes(mimaStr);
        byte[] epcB = stringToBytes(epcCode);

        if (null != epcB)
        {
            // 先让模块确定标签
            manager.selectEPC(epcB);
            // read data
            byte[] data = manager.readFrom6C(membank, startIndex, length, passwB);
            if (data != null && data.length > 1) {
                String dataStr = Tools.Bytes2HexString(data, data.length);
                readResult = dataStr;
            } else {
                if (data != null) {
                    readResult = (data[0] & 0xff) + "";
                } else {
                    readResult = "return null!";
                }
                return -1;
            }
        }
        return 0;
    }

    /**
     * writeLable：写入标签信息，要求先取到标签
     * @return
     */
    private int writeLable(int startIndex, int length, String str, int memIndex) {
        if (listepc.size()<=0) {
            epcCode = "";
            readResult = "未识别标签!";
            return -1;
        }
        epcCode = listepc.get(0);
        if ("".equals(epcCode)){
            return -1; //Please select the EPC tags
        }

        int nadd = startIndex;
        int ndatalen = length; // 长度
        int membank = memIndex;
        String mimaStr = "00000000"; //密码8个0
        byte[] passwB = stringToBytes(mimaStr);
        byte[] myByte = stringToBytes(str);
        byte[] epcB = stringToBytes(epcCode);

        if (null != epcB)
        {
            // 先让模块确定标签
            manager.selectEPC(epcB);

            // dataLen = dataBytes/2
            boolean writeFlag = manager.writeTo6C(passwB, membank,
                    startIndex, myByte.length / 2, myByte);
            if (writeFlag) {
                return 0; // 写入成功
            } else {
                return -1; // 写入失败
            }
        }
        return -1;


        /* // yb-以前模块写入方式，长度参数有使用
        byte[] pwrite = new byte[ndatalen * 2];
        System.arraycopy(myByte, 0, pwrite, 0,
                myByte.length > ndatalen * 2 ? ndatalen * 2 : myByte.length);
        byte[] epc = stringToBytes(epcCode);
        int  iswrite = uhf_6c.write(passw, epc.length, epc, btMemBank, (byte)nadd, (byte) ndatalen * 2, pwrite);
        return iswrite;*/
    }

    /**
     * show Toast
     */
    private Toast mToast;
    private void showToast(String message){
        if (mToast == null){
            mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
            mToast.show();
        }else {
            mToast.setText(message);
            mToast.show();
        }
    }


    private static byte[] stringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    private String bytesToString(byte[] b, int nS, int ncount) {
        String ret = "";
        int nMax = ncount > (b.length - nS) ? b.length - nS : ncount;
        for (int i = 0; i < nMax; i++) {
            String hex = Integer.toHexString(b[i + nS] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
}
