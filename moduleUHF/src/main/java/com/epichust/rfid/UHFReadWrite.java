package com.epichust.rfid;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.cus.maguhf.App;
import com.cus.maguhf.DevBeep;
import com.olc.uhf.UhfAdapter;
import com.olc.uhf.tech.ISO1800_6C;
import com.olc.uhf.tech.IUhfCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * UHFReadWrite:作为调用方法的管理类，提供方法去读写
 *
 * Created by yuanbao on 2019/3/18.
 */
public class UHFReadWrite {

    private ISO1800_6C uhf_6c;
    // *******************************************************
    byte btMemBank = 0x03;
    ArrayAdapter<String> m_adapter;
    private Handler mHandler=new MainHandler();
    // *******************************************************
    String epcCode = ""; // 标签编码
    String readResult = ""; // 读取结果
    // *******************************************************

    /**
     * 初始化方法，由于作为library没有使用application，需要传入上下文
     * @param context 上下文
     */
    public void initUHFModule(Context context){
        if(App.mService == null)
        {
            Log.w("onCreate", "--------模块初始化--yes");
            App.mService = UhfAdapter.getUhfManager(context);
            if (App.mService != null) {
                App.mService.open();
            }
        }
        uhf_6c = (ISO1800_6C) App.mService.getISO1800_6C();
        DevBeep.init(context);
    };

    public Map<String, String> readUHF(int startIndex, int length){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 获取到标签信息
            uhf_6c.inventory(callback); // 由于是开启多线程去读的，等读到后再放回结果
            Thread.sleep(200);
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.w("readUHF","读的标签"+epcCode);
        // 进行读操作
        int mresult = readLable(startIndex, length);
        if(mresult==0){
            DevBeep.PlayOK();
            resultMap.put("flag","yes");
            resultMap.put("epc", epcCode);
            resultMap.put("info", readResult);
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("epc", epcCode==""? "无标签":epcCode);
            resultMap.put("info",uhf_6c.getErrorDescription(mresult));
        }

        return resultMap;
    }

    public Map<String, String> writeUHF(int startIndex, int length, String str){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 获取到标签信息
            uhf_6c.inventory(callback); // 由于是开启多线程去读的，等读到后再放回结果
            Thread.sleep(200);
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.w("writeUHF","写的标签"+epcCode);
        // 进行读操作
        int mresult = writeLable(startIndex, length, str);
        if(mresult==0){
            DevBeep.PlayOK();
            resultMap.put("flag","yes");
            resultMap.put("epc", epcCode);
            resultMap.put("info", "写入成功");
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("epc", epcCode==""? "无标签":epcCode);
            resultMap.put("info",uhf_6c.getErrorDescription(mresult));
        }

        return resultMap;
    }

    /**
     * readLable：读取标签信息，要求先取到标签
     * @return
     */
    private int readLable(int startIndex, int length){
        if ("".equals(epcCode)){
            return -1; //Please select the EPC tags
        }

        int nadd = startIndex;
        int ndatalen = length;

        String mimaStr = "00000000"; //密码8个0
        byte[] passw = stringToBytes(mimaStr);
        byte[] epc = stringToBytes(epcCode);

        if (null != epc)
        {
            byte []dataout = new byte[ndatalen*2];
            if (btMemBank == 1)
            {
                int result = uhf_6c.read(passw, epc.length, epc, (byte) btMemBank,nadd, ndatalen, dataout, 0, ndatalen);
                if(result==0)
                {
                    // 记录读取结果
                    readResult = bytesToString(dataout,0,ndatalen*2);
                    return 0;
                }
                else
                {
                    return result;
                }
            } else {
                int result=uhf_6c.read(passw, epc.length, epc, (byte) btMemBank,nadd, ndatalen, dataout, 0, ndatalen);
                if(result==0)
                {
                    // 记录读取结果
                    readResult = bytesToString(dataout,0,ndatalen*2);
                    return 0;
                }else{
                    return result;
                }
            }
        }
        return -1;
    }

    /**
     * writeLable：写入标签信息，要求先取到标签
     * @return
     */
    private int writeLable(int startIndex, int length, String str) {
        if ("".equals(epcCode)){
            return -1; //Please select the EPC tags
        }
        int nadd = startIndex;
        int ndatalen = length; // 长度
        String mimaStr = "00000000"; //密码8个0
        byte[] passw =stringToBytes(mimaStr);
        byte[] pwrite = new byte[ndatalen * 2];

        byte[] myByte =stringToBytes(str);
        System.arraycopy(myByte, 0, pwrite, 0,
                myByte.length > ndatalen * 2 ? ndatalen * 2 : myByte.length);
        byte[] epc = stringToBytes(epcCode);
        int  iswrite = uhf_6c.write(passw, epc.length, epc, btMemBank, (byte)nadd, (byte) ndatalen * 2, pwrite);
        return iswrite;
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.what ==1)
            {
                DevBeep.PlayOK();
                epcCode = msg.obj.toString();
            }
        }
    }
    IUhfCallback callback = new IUhfCallback.Stub() {
        @Override
        public void doInventory(List<String> str) throws RemoteException {
            Log.w("readFinish", "count=" + str.size());
            for (int i = 0; i < str.size(); i++) {
                String strepc = (String) str.get(i);
                Log.w("readFinish", "RSSI=" + strepc.substring(0, 2));
                Log.w("readFinish", "PC=" + strepc.substring(2, 6));
                Log.w("readFinish", "EPC=" + strepc.substring(2, 6)+strepc.substring(6));
                //DevBeep.PlayOK();
                String strEpc =strepc.substring(2, 6)+strepc.substring(6);
                epcCode = strEpc; // 将epc标签赋予给一个字符串，然后再对其进行读写
                Message msg = new Message();
                msg.what = 1;
                msg.obj = strEpc;
                mHandler.sendMessage(msg);
            }
        }
        @Override
        public void doTIDAndEPC(List<String> str) throws RemoteException {
            for (Iterator it2 = str.iterator(); it2.hasNext();) {
                String strepc = (String) it2.next();
                // Log.d("wyt", strepc);
                int nlen = Integer.valueOf(strepc.substring(0, 2), 16);
                // Log.d("wyt", "PC=" + strepc.substring(2, 6));
                // Log.d("wyt", "EPC=" + strepc.substring(6, (nlen + 1) * 2));
                // Log.d("wyt", "TID=" + strepc.substring((nlen + 1) * 2));
            }
        }
    };
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
