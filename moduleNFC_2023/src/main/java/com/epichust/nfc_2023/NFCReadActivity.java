package com.epichust.nfc_2023;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;
import com.seuic.nfctest.BaseNfcActivity;
import com.seuic.nfctest.R;

import java.util.Arrays;

/**
 * @ClassName: NFCReadActivity
 * @Description: 读取的activity <br>
 * @Author: yuanbao
 * @Date: 2023/11/17
 **/
public class NFCReadActivity extends BaseNfcActivity
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
        setContentView(R.layout.activity_2023_read);

        mContext = getApplicationContext();
        // 处理带过来的参
        mIntent = getIntent();
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

        // 2.取到标签就结束activity，把tag信息和tag中数据回传
        if (mTag != null)
        {
            //获取Ndef的实例
            Ndef ndef = Ndef.get(mTag);
            if (ndef != null)
            {
                try
                {
                    // 获取标签信息
                    mTagInfo = "卡片类型:" + ndef.getType() + "\n最大存储:" + ndef.getMaxSize() + "bytes\n";
                    Log.w("readNFC","-------tagInfo：" + mTagInfo);
                    // 继续读取标签
                    readNfcTag(intent);
                    Log.w("readNFC","-------tagData：" + mTagText);
                } catch (Exception ex)
                {
                    operateStatus = false;
                    operateMsg = ex.toString();
                    Toast.makeText(this, "" + ex.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                operateStatus = false;
                operateMsg = "读取失败，未读取到NFC标签！";
                Toast.makeText(this, "读取失败，未读取到NFC标签！", Toast.LENGTH_SHORT).show();
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
     * 读取NFC标签文本数据
     */
    private void readNfcTag(Intent intent)
    {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try
            {
                if (msgs != null)
                {
                    NdefRecord record = msgs[0].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagInfo += "读取内容：" + textRecord + "\n内容长度:" + contentSize + " bytes";
                    mTagText = textRecord;
                } else {
                    mTagInfo += "读取内容：[为空]";
                    mTagText = null;
                }
                operateStatus = true;
            } catch (Exception e)
            {
                Log.e("readNFC", "读取失败：" + e.getMessage());
                operateStatus = false;
                operateMsg = e.getMessage();
            }

        } else {
            operateStatus = false;
            operateMsg = "识别的标签不匹配，协议为" + intent.getAction();
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     *
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord)
    {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN)
        {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT))
        {
            return null;
        }
        try
        {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);

            Log.w("readNFC","-------activity读取到tag信息：" + textRecord);
            return textRecord;
        } catch (Exception e)
        {
            throw new IllegalArgumentException();
        }
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
