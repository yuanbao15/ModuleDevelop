package com.epichust.nfc_2023;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import com.seuic.nfctest.BaseNfcActivity;
import com.seuic.nfctest.R;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

/**
 * @ClassName: NFCWriteActivity
 * @Description: 写入的activity <br>
 * @Author: yuanbao
 * @Date: 2023/11/17
 **/
public class NFCWriteActivity extends BaseNfcActivity
{
    private Context mContext = null;
    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mPendingIntent;

    private Intent mIntent;
    private Tag mTag; // 标签
    private String mTagInfo; // 标签信息
    private String mTagText; // 标签文本

    private Boolean operateStatus = false; // 操作状态，成功or失败，默认false
    private String operateMsg; // 操作信息、报错信息

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2023_write);

        mContext = getApplicationContext();
        // 处理带过来的参
        mIntent = getIntent();
        mTagText = mIntent.getStringExtra("text");
        // NFC模块初始化
        initNfc();
        processIntent(mIntent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    /**
     * @MethodName: processIntent
     * @Description: 处理intent，从intent中获取NFC标签的信息
     * @Param intent
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/11/17
     **/
    public void processIntent(Intent intent)
    {
        //1.获取Tag对象
        if (intent != null && intent.getAction() != null){
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
                Log.w("readNFC","-------activity读取到tag");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                mTag = tag;
            } else {
                operateStatus = false;
                operateMsg = "识别的标签不匹配，协议为" + intent.getAction();
                mIntent.putExtra("operateStatus", operateStatus);
                mIntent.putExtra("operateMsg", operateMsg);
                // 将读取结果带回，并销毁这个activity页面
                setResult(2, mIntent);
                finish();
            }
        }


        // 2.获取Ndef的实例，然后作写入mTagText。完成后销毁activity返回结果。
        if (mTag != null)
        {
            NdefMessage ndefMessage = new NdefMessage(
                    new NdefRecord[]{createTextRecord(mTagText)});
            try {
                boolean result = writeTag(ndefMessage, mTag);
                if (result) {
                    Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(this,""+e.toString(),Toast.LENGTH_SHORT).show();
            }

            mIntent.putExtra("operateStatus", operateStatus);
            mIntent.putExtra("operateMsg", operateMsg);
            mIntent.putExtra("info", mTagInfo);
            mIntent.putExtra("data", mTagText);
            // 将读取结果带回，并销毁这个activity页面
            setResult(2, mIntent);
            finish();
        }
    }

    /**
     * 创建NDEF文本数据
     *
     * @param text
     * @return
     */
    public static NdefRecord createTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }

    /**
     * 写数据
     *
     * @param ndefMessage 创建好的NDEF文本数据
     * @param tag         标签
     * @return
     */
    public static boolean writeTag(NdefMessage ndefMessage, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if(ndef==null){
                NdefFormatable format = NdefFormatable.get(tag);
                format.connect();
                format.format(ndefMessage);
                if(format.isConnected()){
                    format.close();
                }
            }else{
                ndef.connect();
                ndef.writeNdefMessage(ndefMessage);
                if(ndef.isConnected()){
                    ndef.close();
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("kyl",e.getMessage());
        }
        return false;
    }

    /**
     * @MethodName: initNfc
     * @Description: 初始化NFC标签
     * @Param
     * @Return void
     * @Author: yuanbao
     * @Date: 2023/11/17
     **/
    private void initNfc()
    {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null)
        {
            Toast.makeText(this, "Unsupport NFC!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        try
        {
            if (!mNfcAdapter.isEnabled())
            {
                Toast.makeText(this, "Please enter setting to open NFC!", Toast.LENGTH_LONG).show();
                finish();

                return;
            }
        } catch (Exception e)
        {
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }
}
