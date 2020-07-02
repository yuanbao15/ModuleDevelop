package com.epichust.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.olc.nfcmanager.Constants;
import com.olc.nfcmanager.ISO15693.I15693Utils;
import com.olc.nfcmanager.ParseListener;
import com.olc.nfcmanager.R;
import com.olc.nfcmanager.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个类最终没有用到，因为NFC必须要通过activity接收intent才能拿到tag信息
 *      无奈 = =.后使用NFCReadActivity类
 * Created by yuanbao on 2019/3/21
 */
public class NFCRead implements ParseListener {
    private static final String TAG = "nfcmanager";
    private NfcAdapter mNfcAdapter;
    private MyHandler mHandler;
    private PendingIntent mPendingIntent;

    private Tag mTag;
    private String mUid;
    private String mTech;
    private byte[] mExtraId;
    private Context mContext;
    private Intent mIntent;
    /**
     * @methodName    initNFCModule NFC模块初始化
     * @param    context    上下文
     * @author  yuanbao
     * @date    2019/3/21
     */
    public String initNFCModule(Context context){
        this.mContext = context;
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        mHandler = new MyHandler();
        if (mNfcAdapter == null){
            return "Unsupport NFC!";
        }
        try{
            if (!mNfcAdapter.isEnabled()) {
                return "Please enter setting to open NFC!";
            }
        }catch (Exception e){
            return "Error!";
        }

        // 活动意图初始化
        mIntent = new Intent(mContext, getClass());
        mIntent.setAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        mPendingIntent = PendingIntent.getActivity(mContext, 0, mIntent, 0);
        if (mNfcAdapter != null)
            // context强制转换为activity
            mNfcAdapter.enableForegroundDispatch((Activity)mContext, mPendingIntent, null, null);
        // 加入提示音
        DevBeep.init(mContext);

        processIntent(mIntent);
        return "";
    }
    /**
     * @methodName    readNFC   读取的框架方法
     *
     * @author  yuanbao
     * @date    2019/3/21
     */
    public Map<String, String> readNFC(int blockIndex){
        Map<String, String> resultMap = new HashMap<>();

        // 进行读操作
        String mresult = readNFCLabel(blockIndex);
        if(mresult != null){
            DevBeep.PlayOK();
            resultMap.put("flag","yes");
            resultMap.put("nfcCard", mUid);
            resultMap.put("data", mresult);
        }else{
            DevBeep.PlayErr();
            resultMap.put("flag","no");
            resultMap.put("nfcCard", "未识别");
            resultMap.put("data","未读到信息");
        }
        return resultMap;
    }

    /**
     * @methodName    readNFCLabel 具体的读取过程
     * @param   blockIndex 读取的区块起始位置只读一块
     *
     * @author  yuanbao
     * @date    2019/3/21
     */
    private String readNFCLabel(int blockIndex){
        String data = null;
        data = I15693Utils.getInstance().readSingleBlock(blockIndex);
        if (!TextUtils.isEmpty(data)) {
            data = data.replaceAll("block",R.string.string_block+"1");
            data = data.replaceAll("hex",R.string.string_hex +"2");
            data = data.replaceAll("text",R.string.string_text+"3");
        }

        return data;
    }



    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String info = (String)msg.obj;
            if (!TextUtils.isEmpty(info)) {
                String[] infoArray = info.split(",");
                String infoFormat = R.string.string_15693_info + "";
                info = String.format(infoFormat,infoArray[0],infoArray[1]);
            }
//            mUidView.setText(mUid);
//            mTypeView.setText(I15693Utils.getInstance().getType());
//            mTechView.setText(mTech);
//            mInfoView.setText(info);
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

    private void processIntent(Intent intent) {
        if (intent != null){
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
                Log.w("readNFC","-------NFCRead读取");
                processTagIntent(intent);
            } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
                if (tag == null)
                    return;
            }
        }
    }
    private void processTagIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null)
            return;
        String[] techList = tag.getTechList();
        byte[] tagId = tag.getId();
        String supportStr = "ID: "+ Utils.bytesToHexString(tagId)+"\n";
        supportStr += "type: "+tag.describeContents()+"\n";
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
            uid+= Utils.bytesToHexString(new byte[]{tagId[i]});
        }
        uid = uid.toUpperCase();
        // 将uid和techStr赋值保存
        mUid =uid;
        mTech = techStr;
    }
}
