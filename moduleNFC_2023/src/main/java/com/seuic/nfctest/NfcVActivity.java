package com.seuic.nfctest;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NfcVActivity extends BaseNfcActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_v);
        ButterKnife.bind(this);

        textView=(TextView)findViewById(R.id.textView);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        try{
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i("zy",DataUtil.bytesToHexString(tag.getId()));
            for (String tech : tag .getTechList()) {
                Log.i("zy",tech);
            }
            IsoDep isodep = IsoDep.get(tag);
            if (isodep != null){
                isodep.connect(); // 建立连接
                byte[] data = new byte[20];
                byte[] response = isodep.transceive(data); // 传送消息
                isodep.close(); // 关闭连接
            }else {
                NfcV tech = NfcV.get(tag);
                tech.connect();
                NfcVUtils nfcVUtils=new NfcVUtils(tech);
                String tmp=nfcVUtils.getUID();
                tmp=nfcVUtils.getAFI();
                tmp=nfcVUtils.getDSFID();
                String content="";
                for (int i=0;i<nfcVUtils.getBlockNumber();i++){
                    tmp=nfcVUtils.readOneBlock(i);
                    Log.e("kyl","第"+i+"块，"+"大小:"+nfcVUtils.getOneBlockSize()+",块数据:"+tmp);
                    content+=tmp;
                }
                textView.setText(content);
                Log.e("kyl",content);
                tech.close();
            }
        }catch (Exception e){
            textView.setText(e.getMessage());
            e.printStackTrace();
        }




    }
}
