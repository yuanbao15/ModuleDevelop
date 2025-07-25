package com.epichust.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.olc.nfcmanager.Constants;
import com.olc.nfcmanager.ISO15693.I15693Utils;
import com.olc.nfcmanager.ParseListener;
import com.olc.nfcmanager.R;
import com.olc.nfcmanager.Utils;

/**
 * Created by yuanbao on 2019/3/21
 * 写也是开启这个activity，识别到tag后写入操作，然后返回写入结果。
 * 这段逻辑未测试。
 */
public class NFCWriteActivity extends Activity implements ParseListener
{
    private Context mContext = null;
    private NfcAdapter mNfcAdapter = null;
    private NFCReadActivity.MyHandler mHandler;
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
    private String blockData = ""; // 需要操作写入的数据

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write);
        mContext = getApplicationContext();
        // 处理带过来的参
        mIntent = getIntent();
        blockIndex = mIntent.getIntExtra("blockIndex", 0);
        blockNum = mIntent.getIntExtra("blockNum", 1);
        blockData = mIntent.getStringExtra("blockData");
        // NFC模块初始化
        initNfc();
        processIntent(mIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }
    private void processIntent(Intent intent) {
        if (intent != null){
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
                Log.w("writeNFC","-------activity写入到tag");
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                mTag = tag;
            }
        }
        // 取到标签就结束activity，把tag信息和tag中数据回传
        if (mTag != null)
        {
            // 取到uid和tech信息
            String[] techList = mTag.getTechList();
            byte[] tagId = mTag.getId();
            mExtraId = tagId;
            String supportStr = "ID: "+ Utils.bytesToHexString(tagId)+"\n";
            supportStr += "type: "+ mTag.describeContents()+"\n";
            boolean is15693 = false;
            String techStr ="";
            for (String tech : techList) {
                supportStr+=tech+" \n ";
                techStr+=tech.substring(tech.lastIndexOf(".")+1)+" ";
                if (tech.equals(Constants.NFCV)) {
                    is15693 = true;
                }
            }
            String uid = "";
            for (int i=0; i<tagId.length; i++){
                if (i != 0) {
                    uid+= ":";
                }
                uid += Utils.bytesToHexString(new byte[]{tagId[i]});
            }
            uid = uid.toUpperCase();
            // 保存相关信息
            mUid = uid;
            mTech = techStr;

            // 模块标签实例化，便于取data信息
            I15693Utils.getInstance().parse15693Tag(mTag, mExtraId, new ParseListener() {
                @Override
                public void onParseComplete(String info) {
                    mInfo = info; // 取到info信息

                    boolean isSuccess = true;
                    // 待写入的数据转换一下bytes
                    byte[] blockDataBytes = hexStringToBytes(blockData);
                    if (blockNum == 1){ // 写单块
                        isSuccess = I15693Utils.getInstance().writeSingleBlock(blockIndex, blockDataBytes);
                    }else if (blockNum > 1){ // 读4块
                        // 这儿实测有个坑，读多块时必须先执行一下读单块的方法
                        isSuccess = I15693Utils.getInstance().writeMultipleBlocks(blockIndex, blockNum, blockDataBytes); // 两种模式，0读两块，1读四块
                    }

                    Log.w("writeNFC","uid:"+mUid);
                    Log.w("writeNFC","isSuccess:"+isSuccess);
                    mIntent.putExtra("tag", mTag);
                    mIntent.putExtra("uid", mUid);
                    mIntent.putExtra("tech", mTech);
                    mIntent.putExtra("info", mInfo);
//                    mIntent.putExtra("data", mData);
                    mIntent.putExtra("status", isSuccess);  // 返回状态表明是否写入成功
                    setResult(2, mIntent);
                    finish(); // 消除这个activity页面
                }
            });
        }

    }

    @Override
    public void onParseComplete(final String info) {
        if (!TextUtils.isEmpty(info)) {
            Message msg = new Message();
            msg.obj = info;
            mHandler.sendMessage(msg);
        }
    }
    private void initNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null){
            Toast.makeText(this, "Unsupport NFC!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        try{
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "Please enter setting to open NFC!", Toast.LENGTH_LONG).show();
                finish();

                return;
            }
        }catch (Exception e){
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String info = (String)msg.obj;
            Log.w("writeNFC","-------写入info:"+info);
        }
    }

    // 工具：16进制字符串转byte[]
    /** * Convert hex string to byte[] * @param hexString the hex string * @return byte[] */
    public static byte[] hexStringToBytes(String hexString) {
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
    /** * Convert char to byte * @param c char * @return byte */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}