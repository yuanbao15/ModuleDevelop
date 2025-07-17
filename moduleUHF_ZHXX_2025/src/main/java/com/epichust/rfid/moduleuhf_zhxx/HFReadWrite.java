package com.epichust.rfid.moduleuhf_zhxx;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.magicrf.uhfreader.UhfReaderDevice;
import com.magicrf.uhfreader.Util;
import com.magicrf.uhfreaderlib.reader.Tools;
import com.pl.serialport.SerialPort;

import java.io.*;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: UHFReadWrite
 * @Description: 具体实现读写的类 <br>
 * @Author: yuanbao
 * @Date: 2025/7/11
 **/
public class HFReadWrite
{
    private static final String TAG = "HF_READER";

    Context mContext; // YB-上个页面传递过来的上下文

    byte mCmd= (byte)0x0; // 当前操作模式的命令：读-0xA3、写-0xA4、查询标签-0x0
    byte[] mTBuffer = new byte[32]; // 发送数据缓存
    protected byte[] DataBuffer = new byte[64];
    protected int DataCount = 0;

    private StringBuffer m_uartTemp = new StringBuffer(); // 串口接收数据缓存
    private volatile boolean isReadingBlock = false; // 读取块数据拼接标志，默认为false，因为要4次连续读取后拼接在一块，所以定义一个标识符表示拼接进行中。

    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private boolean readThreadFlag = false;

    private String serialport ;
    private int baudrate ;

    public Handler handler = new Handler() ;

    // *******************************************************
    private String epcCode = ""; // 标签编码
    private String readResult = ""; // 读取结果
    private int dataType = 0; // 读写数据值的类型，0为16进制，1为utf8，默认为0
    // *******************************************************

    // 截止符固定为“24”
    private static final String END_FLAG = "24";

    // 新增：读块回调接口
    public interface OnReadBlockListener {
        void onReadBlockResult(Map<String, String> result);
    }
    private OnReadBlockListener mReadBlockListener = null;

    // 新增：写块回调接口
    public interface OnWriteBlockListener {
        void onWriteBlockResult(Map<String, String> result);
    }
    private OnWriteBlockListener mWriteBlockListener = null;

    // 新增：读取EPC回调接口
    public interface OnReadEPCListener {
        void onReadEPCResult(Map<String, String> result);
    }
    private OnReadEPCListener mReadEPCListener = null;


    /**
     * 初始化方法，由于作为library没有使用application，需要传入上下文
     * @param context 上下文
     */
    public void initUHFModule(Context context){
        mContext = context;

        // 初始化超高频读写器
        UhfReaderDevice.getInstance();

        //初始化声音池
        Util.initSoundPool(context);
//        DevBeep.init(context); // yb自己的提示音初始化

        // 初始化串口模块
        initSerialPort();
    };

    /**
     * @MethodName: readBlock
     * @Description: 读块：固定的读取1扇区2块里的内容，起始位从0开始。块内数据长度为16字节/32位
     * @param listener 读块结果回调
     * @param dataTypeP 数据类型，0为16进制，1为utf8
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void readBlock(int dataTypeP, OnReadBlockListener listener){
        dataType = dataTypeP;
        m_uartTemp = new StringBuffer(); // 清空缓存
        mCmd = (byte)0xA3;
        mReadBlockListener = listener;
        isReadingBlock = false; // 新增，标记未开始拼接
        Log.w(TAG,"Sending Read Block cmd...\n");
        try {
            if ((mOutputStream != null)) {
                mTBuffer[0] = 0x01; //01 08 A3 20 0A 01 00 7E
                mTBuffer[1] = 0x08;
                mTBuffer[2] = (byte)0xA3;
                mTBuffer[3] = 0x20;
                mTBuffer[4] = 0x06; // 块号06（扇区1块2）
                mTBuffer[5] = 0x01;
                mTBuffer[6] = 0x00;
                CheckSum(mTBuffer,(byte)8);
                //				mTBuffer[7] = 0x7E;

                writeSerial(mTBuffer);
                Log.w(TAG, "Send completion!\n");
                // 新增：启动超时处理
                handler.postDelayed(timeoutRunnable, 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DevBeep.PlayErr();
            // 发生异常时也回调错误
            if (mReadBlockListener != null) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "异常");
                resultMap.put("info", e.getMessage());
                mReadBlockListener.onReadBlockResult(resultMap);
                mReadBlockListener = null;
            }
        }
    }

    /**
     * @MethodName: writeData
     * @Description: 写入数据
     * @param str 待写入的值
     * @param dataTypeP 数据类型，0为16进制，1为utf8
     * @param listener 写块结果回调
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void writeData(String str, int dataTypeP, OnWriteBlockListener listener){
        dataType = dataTypeP;
        m_uartTemp = new StringBuffer(); // 清空缓存
        mCmd = (byte)0xA4;
        mWriteBlockListener = listener;


        try {
            byte[] mBlockData = new byte[16];
            String mStrData = str;

            // YB-默认16进制字符串，如果是UTF8再转为16进制。先转换再截止和超长处理。
            if (dataType != 0)
            {
                // 转换成16进制字符
                String originStr = mStrData;
                String newStr = convertStr2Hex(originStr);
                mStrData = newStr;
                Log.w(TAG, "写入：UTF8为：" + originStr + "，转码后16进制为：" + newStr);
            } else
            {
                // 增加校验：若数据类型为16进制，传参字符串中含有非16进制字符，则返回错误
                if ( !mStrData.matches("^[0-9A-Fa-f]+$") )
                {
                    throw new IllegalArgumentException("写入数据必须为16进制字符串，当前数据：" + mStrData + " 包含非16进制字符。请检查输入。");
                }
            }

            // YB-截止符和超长处理
            if (mStrData.length() <= 30)
            {
                // 如果16进制位数不超过30位需要在后面增加截止符
                mStrData = mStrData + END_FLAG;
                Log.w(TAG, "写入：16进制位数不足30位，已自动添加截止符。最终为" + mStrData);
            } else if (mStrData.length() > 32)
            {
                // 如果超过32位则截断到32位
                mStrData = mStrData.substring(0, 32);
                Log.w(TAG, "写入：16进制位数超过32位，已截断。最终为" + mStrData);
            }

            // 32位补0
            while(mStrData.length() < 32)
            {
                mStrData += '0';
            }
            mBlockData = Tools.HexString2Bytes(mStrData) ;//ByteUtil.hexStrToByte(mStrData);
            mTBuffer[0] = 0x01;
            mTBuffer[1] = 0x17;
            mTBuffer[2] = (byte)0xA4;
            mTBuffer[3] = 0x20;
            mTBuffer[4] = 0x06; // 块号06（扇区1块2）
            mTBuffer[5] = 0x01;
            for(int i=0;i<16;i++)
            {
                mTBuffer[6 + i] = mBlockData[i];
            }
            CheckSum(mTBuffer,(byte)23);
            writeSerial(mTBuffer);
            // 新增：启动写入超时处理
            handler.postDelayed(timeoutWriteRunnable, 3000);
        } catch (Exception e) {
            e.printStackTrace();
            DevBeep.PlayErr();
            // 发生异常时也回调错误
            if (mWriteBlockListener != null) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "异常");
                resultMap.put("info", e.getMessage());
                mWriteBlockListener.onWriteBlockResult(resultMap);
                mWriteBlockListener = null;
            }
        }
    }


    public void readEPC(OnReadEPCListener listener)
    {
        mCmd = (byte)0xA1; // 读取EPC指令
        m_uartTemp = new StringBuffer(); // 清空缓存
        mReadEPCListener = listener;
        isReadingBlock = false; // 新增，标记未开始拼接
        Log.w(TAG,"Sending Read EPC cmd...\n");
        try {
            if ((mOutputStream != null)) {
                mTBuffer[0] = 0x01; //01 08 A1 20 0A 01 00 7E
                mTBuffer[1] = 0x08;
                mTBuffer[2] = (byte)0xA1;
                mTBuffer[3] = 0x20;
                mTBuffer[4] = 0x06; // 块号06（扇区1块2）
                mTBuffer[5] = 0x01; // 状态：0x00关，0x01开
                mTBuffer[6] = 0x00;
                CheckSum(mTBuffer,(byte)8);
                //				mTBuffer[7] = 0x7E;

                writeSerial(mTBuffer);
                Log.w(TAG, "Send completion!\n");
                // 新增：启动超时处理
                handler.postDelayed(timeoutRunnable, 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DevBeep.PlayErr();
            // 发生异常时也回调错误
            if (mReadEPCListener != null) {
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "异常");
                resultMap.put("info", e.getMessage());
                mReadEPCListener.onReadEPCResult(resultMap);
                mReadEPCListener = null;
            }
        }
    }


    // 新增：读块的超时处理Runnable
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mReadBlockListener != null) {
                DevBeep.PlayErr();
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "无标签");
                resultMap.put("info", "读取-未识别到标签，请将标签靠近设备后重试");
                mReadBlockListener.onReadBlockResult(resultMap);
                mReadBlockListener = null;
                clearCache(); // 清空缓存
            }
        }
    };

    // 新增：写入的超时处理Runnable
    private Runnable timeoutWriteRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWriteBlockListener != null) {
                DevBeep.PlayErr();
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "无标签");
                resultMap.put("info", "写入-未识别到标签，请将标签靠近设备后重试");
                mWriteBlockListener.onWriteBlockResult(resultMap);
                mWriteBlockListener = null;
                clearCache(); // 清空缓存
            }
        }
    };

    // 新增：写入的超时处理Runnable
    private Runnable timeoutEPCRunnable = new Runnable() {
        @Override
        public void run() {
            if (mReadEPCListener != null) {
                DevBeep.PlayErr();
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("flag","no");
                resultMap.put("epc", "无标签");
                resultMap.put("info", "标签读取超时，请将标签靠近设备后重试");
                mReadEPCListener.onReadEPCResult(resultMap);
                mReadEPCListener = null;
                clearCache(); // 清空缓存
            }
        }
    };

    /**
     * @MethodName: clearCache
     * @Description: 读取或写入结束后清空缓存数据
     * @param
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/15
     **/
    private void clearCache() {
        m_uartTemp = new StringBuffer(); // 清空缓存
        readResult = ""; // 清空读取结果

        isReadingBlock = false; // 关键：重置读取标志
        readThreadFlag = false; // 停止读取线程
    }

    /**
     * @MethodName: initSerialPort
     * @Description: 初始化串口
     * @param
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    private void initSerialPort()
    {
        // 初始化串口时，先作下查询标签EPC编码的动作
        mCmd= (byte)0x0;

        Log.w("SerialportActivity", "串口初始化：++++onCreate") ;
        try {
            serialport = "/dev/ttyS1"; // 串口设备名
            baudrate = 9600 ; // 默认波特率
            mSerialPort = new SerialPort(serialport, baudrate);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            /* Create a receiving thread */
            mReadThread = new ReadThread();
            readThreadFlag = true;
            mReadThread.start();

//            showToast("HF模块连接成功");
            Log.i("SerialportActivity", "串口连接HF模块成功") ;

        } catch (SecurityException e) {
            Log.e("SerialportActivity", e.getMessage()) ;
            //DisplayError(R.string.error_security);
        } catch (IOException e) {
            Log.e("SerialportActivity", e.getMessage()) ;
            //DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            Log.e("SerialportActivity", e.getMessage()) ;
            //DisplayError(R.string.error_configuration);
        }
    }

    /**
     * @MethodName: CheckSum
     * @Description: 串口数据校验
     * @param buf
     * @param len
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void CheckSum(byte[] buf, byte len)
    {
        byte i;
        byte checksum;
        checksum = 0;
        for (i = 0; i < (len - 1); i++)
        {
            checksum ^= buf[i];
        }
        buf[len - 1] = (byte)~checksum;
    }

    /**
     * @Description: 开启子线程读卡和写卡，通过串口调用实现
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if(!readThreadFlag)
                    return;

                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;

                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 串口数据接收处理
     * @param buffer 接收到的数据
     * @param size 数据长度
     */
    private void onDataReceived( final byte[] buffer,final int size) {
//        handler.post(new Runnable()
//        {
//            @Override
//            public void run()
//            {

                String bufferStr;
                bufferStr = Tools.Bytes2HexString(buffer, size);
                Log.i(TAG, "读取到 bufferStr: " + bufferStr);
                // 不用switch case句式，换成if else
                if (mCmd == (byte) 0xA3)
                {
                    Log.d(TAG, "READ-isReadingBlock: " + isReadingBlock);
                    if (bufferStr != null && bufferStr.length() >= 8)
                    {
                        if (!isReadingBlock)
                        {
                            int idx = bufferStr.indexOf("0116A320");
                            if (idx != -1)
                            {
                                // 第一次遇到包头，清空并开始拼接
                                m_uartTemp = new StringBuffer();
                                m_uartTemp.append(bufferStr.substring(idx));
                                isReadingBlock = true;
                                Log.d(TAG, "READ-isReadingBlock changed: " + isReadingBlock);
                            }
                            // 没遇到包头，丢弃
                        } else
                        {
                            // 已经开始拼接，直接追加
                            m_uartTemp.append(bufferStr);
                        }
                    }
                    Log.d(TAG, "READ-m_uartTemp: " + m_uartTemp);
                    if (m_uartTemp.length() >= 44 && m_uartTemp.substring(0, 8).equals("0116A320"))
                    {
                        readResult = m_uartTemp.substring(10, 42);
                        if (mReadBlockListener != null)
                        {
                            //                    DevBeep.PlayOK();
                            Util.play(1, 0);
                            handler.removeCallbacks(timeoutRunnable);

                            // YB-截止符处理，若包含截止符则作阶段处理。先截止再转换。
                            if (readResult.indexOf(END_FLAG) != -1)
                            {
                                // 截止符存在，先去掉截止符
//                                readResult = readResult.substring(0, readResult.indexOf(END_FLAG));
                                readResult = process32CharStr(readResult);
                                Log.w(TAG, "READ-读取：16进制数值含截止符，已处理。最终为" + readResult);
                            }

                            // YB-默认读到的为16进制字符串，如果需要再转换为UTF8
                            if (dataType != 0)
                            {
                                // 转换成16进制字符
                                String originStr = readResult;
                                String newStr = convertHex2Str(originStr);
                                readResult = newStr;
                                Log.w(TAG, "READ-读取：16进制为：" + originStr + "，转码后UTF8为：" + newStr);
                            }

                            Map<String, String> resultMap = new HashMap<>();
                            resultMap.put("flag", "yes");
                            resultMap.put("epc", epcCode);
                            resultMap.put("info", readResult);
                            mReadBlockListener.onReadBlockResult(resultMap);
                            mReadBlockListener = null;
                        }
                        clearCache();
                    }
                } else if (mCmd == (byte) 0xA4)
                {
                    // 写入块
                    Log.d(TAG, "WRITE-isReadingBlock: " + isReadingBlock);
                    if (bufferStr != null)
                    {
                        if (!isReadingBlock && bufferStr.length() >= 8)
                        {
                            if (bufferStr.substring(0, 8).equals("0108A420"))
                            {
                                // 第一次遇到包头，清空并开始拼接
                                m_uartTemp = new StringBuffer();
                                m_uartTemp.append(bufferStr);
                                isReadingBlock = true;
                                Log.d(TAG, "WRITE-isReadingBlock changed: " + isReadingBlock);
                            }
                            // 没遇到包头，丢弃
                        } else
                        {
                            // 已经开始拼接，直接追加
                            m_uartTemp.append(bufferStr);
                        }
                    }

                    Log.d(TAG, "WRITE-m_uartTemp: " + m_uartTemp);
                    if (m_uartTemp.length() >= 16 && m_uartTemp.substring(0, 8).equals("0108A420"))
                    { // 完成接收
                        //                    DevBeep.PlayOK();
                        Util.play(1, 0);
                        String rightPrefix = "0108A42000000072";
                        // 绝了，明明一模一样，为啥是false？
                        Log.d(TAG, "WRITE-m_uartTemp: " + m_uartTemp + ", rightPrefix: " + rightPrefix);
                        if(m_uartTemp.toString().equals(rightPrefix))
                        {
                            Log.i(TAG, "WRITE-写卡成功！"); // 因为是通过非UI线程访问的，所以不能直接在UI线程显示Toast
                            if (mWriteBlockListener != null)
                            {
                                handler.removeCallbacks(timeoutWriteRunnable);
                                Map<String, String> resultMap = new HashMap<>();
                                resultMap.put("flag", "yes");
                                resultMap.put("epc", epcCode);
                                resultMap.put("info", "写入成功");
                                mWriteBlockListener.onWriteBlockResult(resultMap);
                                mWriteBlockListener = null;
                            }
                        }
                        else {
                            Log.i(TAG, "WRITE-写卡失败！"); // 因为是通过非UI线程访问的，所以不能直接在UI线程显示Toast
                            if (mWriteBlockListener != null)
                            {
                                handler.removeCallbacks(timeoutWriteRunnable);
                                Map<String, String> resultMap = new HashMap<>();
                                resultMap.put("flag", "no");
                                resultMap.put("epc", epcCode);
                                resultMap.put("info", "写入失败");
                                mWriteBlockListener.onWriteBlockResult(resultMap);
                                mWriteBlockListener = null;
                            }
                        }
                        clearCache(); // 清空缓存
                    }
                } else if (mCmd == (byte) 0x0)
                {
                    // 读取EPC标签信息
                    Log.d(TAG, "EPC-isReadingBlock: " + isReadingBlock);
                    if (bufferStr != null && bufferStr.length() >= 6)
                    {
                        if (!isReadingBlock)
                        {
                            if (bufferStr.substring(0, 6).equals("040C02"))
                            {
                                // 第一次遇到包头，清空并开始拼接
                                m_uartTemp = new StringBuffer();
                                m_uartTemp.append(bufferStr);
                                isReadingBlock = true;
                                Log.d(TAG, "EPC-isReadingBlock changed: " + isReadingBlock);
                            }
                            // 没遇到包头，丢弃
                        } else
                        {
                            // 已经开始拼接，直接追加
                            m_uartTemp.append(bufferStr);
                        }
                    }

                    Log.d(TAG, "EPC-m_uartTemp: " + m_uartTemp);
                    if (m_uartTemp.length() >= 24)
                    {
                        //                        addToList(listEPC, m_uartTemp.substring(14, 22));
                        epcCode = m_uartTemp.substring(14, 22); // 记录EPC编码
                        Log.w(TAG, "EPC-读取到的EPC编码: " + epcCode);
                        Util.play(1, 0);

                        if (mReadBlockListener != null)
                        {
                            //                    DevBeep.PlayOK();
                            Util.play(1, 0);
                            handler.removeCallbacks(timeoutEPCRunnable);

                            Map<String, String> resultMap = new HashMap<>();
                            resultMap.put("flag", "yes");
                            resultMap.put("epc", epcCode);
                            resultMap.put("info", readResult);
                            mReadEPCListener.onReadEPCResult(resultMap);
                            mReadEPCListener = null;
                        }
                        clearCache(); // 清空缓存
                    }
                } else if (mCmd == (byte) 0xA1)
                {
                    // 读取EPC标签信息-自己按官方接口文档写的
                    Log.d(TAG, "0xA1-EPC-isReadingBlock: " + isReadingBlock);
                    if (bufferStr != null && bufferStr.length() >= 6)
                    {
                        if (!isReadingBlock)
                        {
//                            if (bufferStr.substring(0, 6).equals("040C02"))
                            {
                                // 第一次遇到包头，清空并开始拼接
                                m_uartTemp = new StringBuffer();
                                m_uartTemp.append(bufferStr);
                                isReadingBlock = true;
                                Log.d(TAG, "0xA1-EPC-isReadingBlock changed: " + isReadingBlock);
                            }
                            // 没遇到包头，丢弃
                        } else
                        {
                            // 已经开始拼接，直接追加
                            m_uartTemp.append(bufferStr);
                        }
                    }

                    Log.d(TAG, "0xA1-EPC-m_uartTemp: " + m_uartTemp);
//                    if (m_uartTemp.length() >= 24)
                    if (m_uartTemp.length() >= 10)
                    {
                        //                        addToList(listEPC, m_uartTemp.substring(14, 22));
//                        epcCode = m_uartTemp.substring(14, 22); // 记录EPC编码
                        epcCode = m_uartTemp.substring(2, 10); // 记录EPC编码
                        readResult = "获取EPC标签号成功";
                        Log.w(TAG, "0xA1-EPC-读取到的EPC编码: " + epcCode);

                        if (mReadEPCListener != null)
                        {
                            //                    DevBeep.PlayOK();
                            Util.play(1, 0);
                            handler.removeCallbacks(timeoutEPCRunnable);

                            Map<String, String> resultMap = new HashMap<>();
                            resultMap.put("flag", "yes");
                            resultMap.put("epc", epcCode);
                            resultMap.put("info", readResult);
                            mReadEPCListener.onReadEPCResult(resultMap);
                            mReadEPCListener = null;
                        }
                        clearCache(); // 清空缓存
                    }
                }

//            }
//        });

    }

    //  RFID写入日志文件
    public static void writeLog(String barCode) {
        String fileName = "uhfData.csv";

        //====加上時間======
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        barCode = "'"+barCode + "," +formatter.format(curDate) + "\r\n";

        String path = Environment.getExternalStorageDirectory().getPath() +  "/scanservice/"; //文件路径
        FileWriter writer = null;
        try {
            File file = new File(path);
            if (!file.exists()) {  //没有创建文件夹则创建
                file.mkdirs();
            }
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(path + fileName, true);
            writer.write(barCode);
            writer.flush();
            if (writer != null) {
                //关闭流
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: onDestroy
     * @Description: 注销读写器和串口
     * @param
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void onDestroy() {

        UhfReaderDevice.powerOff();
        closeSerialPort();
        clearCache(); // 清空缓存数据
    }

    /**
     * @MethodName: writeSerial
     * @Description:  串口写入数据
     * @param bytes
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    public void writeSerial(byte[] bytes){
        if(mOutputStream != null){
            try {
                mOutputStream.write(bytes);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @MethodName: closeSerialPort
     * @Description: 关闭串口
     * @param
     * @Return void
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    private void closeSerialPort()
    {
        if (mReadThread != null) {
            mReadThread.interrupt();
            readThreadFlag = false;
        }
        if(mSerialPort != null){
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * @MethodName: stringToBytes
     * @Description:  字符串转字节数组
     * @param hexString
     * @Return byte
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
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
    /**
     * @MethodName: charToByte
     * @Description: 字符转字节
     * @param c
     * @Return byte
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    /**
     * @MethodName: bytesToString
     * @Description: 字节数组转字符串
     * @param b
     * @param nS
     * @param ncount
     * @Return java.lang.String
     * @Author: yuanbao
     * @Date: 2025/7/14
     **/
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

    /**
     * @MethodName: convertStr2Hex
     * @Description: 字符串转化为16进制
     * @Param str 一般字符串
     * @Return String 16进制字符串
     * @Author: yuanbao
     * @Date: 2023/12/22
     **/
    public static String convertStr2Hex(String str)
    {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }
    /**
     * @MethodName: convertHex2Str
     * @Description: 16进制转化为字符串
     * @Param hexStr 16进制字符串
     * @Return String 一般字符串
     * @Author: yuanbao
     * @Date: 2023/12/22
     **/
    public static String convertHex2Str(String hexStr)
    {
        hexStr = hexStr.toUpperCase(); // 增加先转换为大写后再去按utf8转字符串
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    /**
     * @MethodName: process32CharStr
     * @Description: 从32位字符串中处理数据，若含截止符需截取截止符前面的数据
     *      实现逻辑：
     *          最右边第一位如果为0时，则从右往左遍历，取最靠右边的24作为截止，再截取前面的内容。
     *          最右边第一位如果不为0时，判断最后两位是否为24：为24则截取前面的内容，不为24则取整个全部。
     *          如果字符串长度少于32位，也是按上面逻辑处理
     * @param str
     * @Return String
     * @Author: yuanbao
     * @Date: 2025/7/16
     **/
    public static String process32CharStr(String str) {
        // 检查字符串是否为空或null
        if (str == null || str.isEmpty()) {
            return "";
        }

        // 获取最右边第一位（最后一个字符）
        char lastChar = str.charAt(str.length() - 1);

        // 情况1：最右边第一位为 '0'
        if (lastChar == '0') {
            // 从右往左查找 "24"
            for (int i = str.length() - 2; i >= 2; i -= 2) {
                if (i + 2 <= str.length() && str.substring(i, i + 2).equals(END_FLAG)) {
                    return str.substring(0, i);
                }
            }
            // 如果找不到 "24"，返回空字符串或根据需求处理
            return "";
        }
        // 情况2：最右边第一位不为 '0'
        else {
            // 检查最后两位是否为 "24"
            if (str.length() >= 2 && str.substring(str.length() - 2).equals(END_FLAG)) {
                return str.substring(0, str.length() - 2);
            } else {
                return str; // 保留完整字符串
            }
        }
    }
}