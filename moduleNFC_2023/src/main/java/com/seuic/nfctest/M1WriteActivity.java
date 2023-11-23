package com.seuic.nfctest;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

import java.io.IOException;

public class M1WriteActivity extends BaseNfcActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m1_write);
        ButterKnife.bind(this);

        textView=(TextView)findViewById(R.id.textView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //1.获取Tag对象
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (detectedTag==null)
        {
            Toast.makeText(this,"无法识别的标签！",Toast.LENGTH_SHORT);
            return;
        }
        String[] techList = detectedTag.getTechList();
        //MifareClissic16个分区，每个分区四个块，每个块16个字节数据;
        //分为64个块，前3个块用于存放厂商代码，已经固化，不可更改。从第四个块开始，每个块写入四个字节
        boolean haveMifareClissic = false;
        for (String tech : techList) {
            if (tech.indexOf("MifareClassic") >= 0) {
                haveMifareClissic = true;
                break;
            }
        }
        if (!haveMifareClissic) {
            Toast.makeText(this, "不支持MifareUltralight数据格式", Toast.LENGTH_SHORT).show();
            return;
        }
        MifareClassic mfc = MifareClassic.get(detectedTag);
        writeBlock(detectedTag,mfc,1,4);
    }


    private void writeBlock(Tag mTag, MifareClassic mfc,int sector,int block) {
        try {
            mfc.connect();//打开连接
            boolean auth;
            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_DEFAULT);//keyA验证扇区
            if (auth){
                mfc.writeBlock(block,"14443AC333333SSS".getBytes("UTF-8"));
                textView.setText("14443AC333333SSS——写入成功!");
                Toast.makeText(this,"写入成功!",Toast.LENGTH_SHORT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
