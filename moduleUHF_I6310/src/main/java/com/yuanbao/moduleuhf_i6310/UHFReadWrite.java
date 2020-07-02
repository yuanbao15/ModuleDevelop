package com.yuanbao.moduleuhf_i6310;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.uhf.activity.Reader;
import com.rfid.trans.ReadTag;
import com.rfid.trans.StringUtility;
import com.rfid.trans.TagCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * UHFReadWrite:作为调用方法的管理类，提供方法去读写
 *
 * Created by yuanbao on 2019/5/20.
 */
public class UHFReadWrite {
    Context mContext;
    MsgCallback callback = new MsgCallback();
    private Handler handler = new MainHandler();

    // *******************************************************
    String epcCode = ""; // 标签编码
    String readResult = ""; // 读取结果
    // *******************************************************

    /**
     * 初始化方法，由于作为library没有使用application，需要传入上下文
     * @param context 上下文
     */
    public boolean initUHFModule(Context context){
        this.mContext = context;

        DevBeep.init(context); // yb自己的提示音初始化
        setGPIOEnabled(true);
        clearData();
        //set53CGPIOEnabled(true);
        SystemClock.sleep(200);

        // 通过串口连接UHF手柄
        boolean initFlag = ConnectUHF();
        if (!initFlag)
            DevBeep.PlayErr();
        return initFlag;
    };

    // 读标签接口
    public Map<String, String> readUHF(int startIndex, int length, int memIndex){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 获取到标签信息
            // 由于是开启多线程去读的，等读到后再放回结果
            Reader.rrlib.SetCallBack(callback);
            Reader.rrlib.SingleInventory(); // 读一次epcCode
            Thread.sleep(300);
        } catch (Exception e){
            e.printStackTrace();
        }

        Log.w("readUHF","读的标签"+epcCode);
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
            resultMap.put("epc", epcCode==""? "未找到标签":epcCode);
            resultMap.put("info","读取失败");
        }

        return resultMap;
    }

    // 写标签接口
    public Map<String, String> writeUHF(int startIndex, int length, String str, int memIndex){
        Map<String, String> resultMap = new HashMap<>();

        try{
            // 获取到标签信息
            // 由于是开启多线程去读的，等读到后再放回结果
            Reader.rrlib.SetCallBack(callback);
            Reader.rrlib.SingleInventory(); // 读一次epcCode
            Thread.sleep(300);
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
            resultMap.put("info", "写入成功！");
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("epc", epcCode==""? "未找到标签":epcCode);
            resultMap.put("info","写入失败！");
        }

        return resultMap;
    }

    /**
     * readLable：读取标签信息，要求先取到标签
     * @return
     */
    private int readLable(int startIndex, int length, int memIndex){
        // 这个接口不需要先拿到标签
        if ("".equals(epcCode)){
            return -1;
        }
        int result = -1;

        String EPCStr = epcCode;
        Log.w("readLable","读的标签"+epcCode);
        byte WordPtr = (byte)(startIndex);
        byte Num  = (byte)(length);
        byte Mem = (byte)(memIndex);
        String pwdStr = "00000000"; //密码8个0
        String HexData = Reader.rrlib.ReadDataByEPC(EPCStr, Mem, WordPtr, Num, pwdStr);
        if(HexData!=null)
        {
            result = 0;
            readResult = HexData;
        } else {
            result = -1;
            readResult = "";
        }

        return result;
    }

    /**
     * writeLable：写入标签信息，要求先取到标签
     * @return
     */
    private int writeLable(int startIndex, int length, String str, int memIndex) {
        // 这个接口不需要先拿到标签
        if ("".equals(epcCode)){
            return -1;
        }
        int result = -1;

        String EPCStr = epcCode;
        byte WordPtr = (byte)(startIndex);
        byte Num  = (byte)(length);
        byte Mem = (byte)(memIndex);
        String pwdStr = "00000000"; //密码8个0

        int fCmdRet = Reader.rrlib.WriteDataByEPC(EPCStr,Mem,WordPtr,pwdStr,str);
        if (fCmdRet==0) {
            result = 0;
        } else {
            result = -1;
        }

        return result;
    }

    // 从回调里去设置数据
    // demo中使用handler是因为在ui线程中更新展示数据
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.what ==1)
            {
//                epcCode = msg.obj.toString();
            }
        }
    }

    // 获取标签的回调方法，多线程方式
    public class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag arg0) {
            // TODO Auto-generated method stub
            String epc = arg0.epcId.toUpperCase();
            String rssi = String.valueOf(arg0.rssi);
            Log.w("Handler","cpcCode:"+epcCode+",rssi:"+rssi);
            Message msg = handler.obtainMessage();
            msg.what = 1;
            msg.obj = epc ;
            epcCode = epc;
            handler.sendMessage(msg);
        }

        @Override
        public int CRCErrorCallBack(int reason) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void FinishCallBack() {
            // TODO Auto-generated method stub
        }

        @Override
        public int tagCallbackFailed(int reason) {
            // TODO Auto-generated method stub
            return 0;
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

    // 串口连接初始化，检查是否连接手柄
    public boolean ConnectUHF() {
        String project = getValueFromProp();
        int result =-1;
        if(project.equals("SQ52TG")||project.equals("SQ52TGW")||project.equals("SQ52T")||project.equals("SQ52")||project.equals("SQ52C") ||project.equals("SQ52W")){
            android.util.Log.i("wujinquan","SQ52T project:"+project);
            result = Reader.rrlib.Connect("/dev/ttyUSB0", 57600);
        }else if(project.equals("SQ53C")) {
            result = Reader.rrlib.Connect("/dev/ttyHSL0", 57600);
        }else if(project.equals("SQ53")){
            result = Reader.rrlib.Connect("/dev/ttyMSM1", 57600);
        }else if(project.equals("SQ31T")){
            result = Reader.rrlib.Connect("/dev/ttyMT0", 57600);
            if(result==0)
                setBrightness("/sys/class/ugp_ctrl/uhf_enable/enable", "1".getBytes());
        }
        SystemClock.sleep(200);
        if(result ==0){
//            Toast.makeText(mContext,R.string.Connectsuccess,
//                    Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(mContext, R.string.Connectfailed,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void setGPIOEnabled(boolean enable){
        String project=getValueFromProp();
        android.util.Log.i("wujinquan","project:"+project);
        if(project.equals("SQ52TG")||project.equals("SQ52TGW")||project.equals("SQ52T")||project.equals("SQ52")||project.equals("SQ52C")||project.equals("SQ52W")){
            set52OTGEnabled(enable);
        }else if(project.equals("SQ53C")) {
            set53CGPIOEnabled(enable);
        }else if(project.equals("SQ53")){
            setBrightness("/sys/devices/soc/c170000.serial/pogo_uart",enable?"1".getBytes():"0".getBytes());//20190919
            set53Enabled(enable);
        }else if(project.equals("SQ31T")){
            setBrightness("/sys/class/ugp_ctrl/uhf/pwrctl",enable?"1".getBytes():"0".getBytes());
            setBrightness("/sys/class/ugp_ctrl/uhf/enctl",enable?"1".getBytes():"0".getBytes());
        }
    }

    private String getValueFromProp() {
        String value = "";
        Class<?> classType = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            //拿到get方法，此方法传入一个String类型参数。
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            // 键值persist.sys.sb.hide，其他的也可以。
            value = (String) getMethod.invoke(classType, new Object[]{"pwv.project"});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    private void set52OTGEnabled(boolean enable){
        FileOutputStream f = null;
        try{
            f = new FileOutputStream("/sys/devices/soc/78db000.usb/dpdm_pulldown_enable");
            f.write(enable?"otgenable".getBytes():"otgdisable".getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(f != null){
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void set53Enabled(boolean enable){
        FileOutputStream f = null;
        try{
            f = new FileOutputStream("sys/devices/virtual/Usb_switch/usbswitch/function_otg_en");
            f.write(enable?"2".getBytes():"0".getBytes());

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(f != null){
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void set53CGPIOEnabled(boolean enable){
        FileOutputStream f = null;
        FileOutputStream f1 = null;
        try{
            f = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_pogo_5v_ctrl/enable");
            f.write(enable?"1".getBytes():"0".getBytes());
            f1 = new FileOutputStream("/sys/devices/soc/soc:sectrl/ugp_ctrl/gp_otg_en_ctrl/enable");
            f1.write(enable?"1".getBytes():"0".getBytes());

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(f != null){
                try {
                    f.close();
                    f1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private synchronized void setBrightness(String ledDev, byte[] lightOn) {
        try {
            FileOutputStream fLed = new FileOutputStream(ledDev);
            fLed.write(lightOn);
            fLed.close();
        } catch (Exception e) {
            System.out.print(e);
        }
    }


    // 清空数据
    private void clearData() {
        epcCode = "";
        readResult = "";
    }

    // 停止识别
    private void stopInventory() {
        Reader.rrlib.StopRead();
    }

    // toast弹窗提示信息
    private void showTips(String str){
        Toast toast = Toast.makeText(this.mContext, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    // 判断是否为16进制字符串
    public boolean vailHexInput(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }

    /*initSound();
    this.playSound(1);

    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;
    private void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this.mContext, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this.mContext, R.raw.serror, 1));
        am = (AudioManager) this.mContext.getSystemService(mContext.AUDIO_SERVICE);// 实例化AudioManager对象
    }
    // 播放提示音 @param id 成功1，失败2
    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();

        }
    }*/
}
