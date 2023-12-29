package com.epichust.nfczbl;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import com.epichust.nfczbl.R;
import com.olc.nfcmanager2.Constants;
import com.olc.nfcmanager2.I15693Utils;
import com.olc.nfcmanager2.ParseListener;
import com.olc.nfcmanager2.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yuanbao on 2023/12/21
 */
public class NFCReadActivity extends Activity implements ParseListener
{
    private Context mContext = null;
    private NfcAdapter mNfcAdapter = null;
    private MyHandler mHandler;
    private PendingIntent mPendingIntent;
    private Intent mIntent;
    private Tag mTag;
    private String mUid; //标签的相关信息
    private String mTech;
    private String mInfo;
    private String mData;
    private byte[] mExtraId; // 存放读取的标签id，可以放多个
    private int blockIndex = 0; // 需要操作的块位置
    private int blockNum = 1; // 需要操作的块数量
    private int dataType = 0; // 写入数据值的类型，0为16进制，1为utf8，默认为0

    // YB-新增状态和失败信息的返回
    private Boolean operateStatus = false; // 操作状态，成功or失败，默认false
    private String operateMsg; // 操作信息、报错信息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2023_read);
        mContext = getApplicationContext();
        // 处理带过来的参
        mIntent = getIntent();
        blockIndex = mIntent.getIntExtra("blockIndex", 0); // 默认读块起始位为0
        blockNum = mIntent.getIntExtra("blockNum", 1); // 默认读单块
        dataType = mIntent.getIntExtra("dataType", 0); // 默认值类型为16进制
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
    protected void onResume()
    {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent)
    {
        if (intent != null)
        {
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
            {
                Log.w("readNFC", "-------activity读取到tag");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                mTag = tag;
            }
        }
        // 取到标签就结束activity，把tag信息和tag中数据回传
        if (mTag != null)
        {
            try {
                // 取到uid和tech信息
                String[] techList = mTag.getTechList();
                byte[] tagId = mTag.getId();
                mExtraId = tagId;
                String supportStr = "ID: " + Utils.bytesToHexString(tagId) + "\n";
                supportStr += "type: " + mTag.describeContents() + "\n";
                boolean is15693 = false;
                String techStr = "";
                for (String tech : techList)
                {
                    supportStr += tech + " \n ";
                    techStr += tech.substring(tech.lastIndexOf(".") + 1) + " ";
                    if (tech.equals(Constants.NFCV))
                    {
                        is15693 = true;
                    }
                }
                String uid = "";
                for (int i = 0; i < tagId.length; i++)
                {
                    if (i != 0)
                    {
                        uid += ":";
                    }
                    uid += Utils.bytesToHexString(new byte[] { tagId[i] });
                }
                uid = uid.toUpperCase();
                // 保存相关信息
                mUid = uid;
                mTech = techStr;

                // 模块标签实例化，便于取data信息
                I15693Utils.getInstance().parse15693Tag(mTag, mExtraId, new ParseListener()
                {
                    @Override
                    public void onParseComplete(String info)
                    {

                        try
                        {
                            mInfo = info; // 取到info信息

                            String data = null;
                            // 取到data信息
                            if (blockNum == 1)
                            { // 读单块
                                data = I15693Utils.getInstance().readSingleBlock(blockIndex);
                            } else if (blockNum > 1)
                            { // 读多块
                                data = I15693Utils.getInstance().readMultipleBlocks(blockIndex, blockNum); // 传递读块的数量，问题已解决
                            }
                            if (!TextUtils.isEmpty(data))
                            {
                                // 截取数据只取hex：后的数据，多块的就拼接起来
                                String regex = "(?<=hex:).*";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(data);
                                mData = "";
                                while (matcher.find())
                                {
                                    mData += matcher.group(0).trim();
                                }
                            }

                            Log.w("readNFC", "uid:" + mUid);
                            Log.w("readNFC", "data:（读取到的完整数据）" + data);
                            Log.w("readNFC", "data:（转码前）" + mData);

                            // 默认读到的为16进制字符串，如果需要再转换为UTF8
                            if (dataType == 1)
                            {
                                // 转换成16进制字符
                                String originStr = mData;
                                String newStr = convertHexStr2Str(originStr);
                                mData = newStr;
                                Log.w("readNFC", "data:（转码后）" + mData);
                            }

                            operateStatus = true;
                            operateMsg = "操作成功";
                        } catch (Exception e2)
                        {
                            operateStatus = false;
                            operateMsg = e2.toString();
                        } finally
                        {
                            mIntent.putExtra("uid", mUid);
                            mIntent.putExtra("tech", mTech);
                            mIntent.putExtra("info", mInfo);
                            mIntent.putExtra("data", mData);
                            mIntent.putExtra("operateStatus", operateStatus);  // 返回状态表明是否读取成功
                            mIntent.putExtra("operateMsg", operateMsg);
                            setResult(2, mIntent);
                            finish(); // 消除这个activity页面
                        }
                    }
                });
            } catch (Exception ex)
            {
                operateStatus = false;
                operateMsg = ex.toString();
                Toast.makeText(this, "读取失败：" + ex.toString(), Toast.LENGTH_SHORT).show();

                mIntent.putExtra("uid", mUid);
                mIntent.putExtra("tech", mTech);
                mIntent.putExtra("info", mInfo);
                mIntent.putExtra("data", mData);
                mIntent.putExtra("operateStatus", operateStatus);  // 返回状态表明是否读取成功
                mIntent.putExtra("operateMsg", operateMsg);
                setResult(2, mIntent);
                finish(); // 消除这个activity页面
            }
        }
    }

    @Override
    public void onParseComplete(final String info)
    {
        if (!TextUtils.isEmpty(info))
        {
            Message msg = new Message();
            msg.obj = info;
            mHandler.sendMessage(msg);
        }
    }

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

    class MyHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            String info = (String) msg.obj;
            Log.w("readNFC", "-------读取info:" + info);
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
     * @MethodName: convertHexStr2Str
     * @Description: 16进制转化为字符串
     * @Param hexStr 16进制字符串
     * @Return String 一般字符串
     * @Author: yuanbao
     * @Date: 2023/12/22
     **/
    public static String convertHexStr2Str(String hexStr)
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
}
