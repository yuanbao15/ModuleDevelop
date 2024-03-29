package com.seuic.nfctest;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Author:Created by Ricky on 2017/8/25.
 * Email:584182977@qq.com
 * Description:
 */
public class WriteTextActivity extends BaseNfcActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_text);

        editText=(EditText)findViewById(R.id.etTag);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (editText.getText() == null)
            return;
        //获取Tag对象
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefMessage ndefMessage = new NdefMessage(
                new NdefRecord[]{createTextRecord(editText.getText().toString())});
        try {
            boolean result = writeTag(ndefMessage, detectedTag);
            if (result) {
                Toast.makeText(this, "写入成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "写入失败", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(this,""+e.toString(),Toast.LENGTH_SHORT).show();
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


}
