package com.seuic.nfctest;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;

import java.io.IOException;

public class M1ReadActivity extends BaseNfcActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m1_read);
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
        //MifareClissic16个分区，每个分区四个块，每个块16个字节数据;
        //分为64个块，前3个块用于存放厂商代码，已经固化，不可更改。从第四个块开始，每个块写入四个字节
        MifareClassic mfc = MifareClassic.get(detectedTag);
        String tmp=read(detectedTag,mfc);
//        String tmp=read(detectedTag,mfc);
        textView.setText(tmp);
    }


    //读取m1卡所有数据
    public String read(Tag tag, MifareClassic mifareClassic) {
        String nfcInfo = "";
        boolean auth = false;
        try {
            mifareClassic.connect();
            int sectorCount = mifareClassic.getSectorCount();
            nfcInfo += "卡片容量：" + mifareClassic.getSize() + "\n";
            nfcInfo += "扇区数量：" + sectorCount + "\n";
            //获取m1卡扇区数，一般m1卡扇区数为１６
            int count = 0;
            //0扇区为m1卡id,不可更改，从１扇区读取
            for (int i = 1; i < sectorCount; i++) {
                auth = mifareClassic.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);//用默认密码验证i块，正确可读数据
                if (auth) {
                    //每个扇区0-2块存储数据,3块为控制块
                    for (int j = 0; j < 4; j++) {
                        byte[] data = mifareClassic.readBlock(i * 4 + j);
                        nfcInfo += i + "扇区" + j + "块:" + DataUtil.bytesToHexString(data) + "\n";
                        Log.e("kyl", i + "扇区" + j + "块:" + new String(data) + "\n");
                    }
                } else {
                    nfcInfo += i + "扇区：验证失败！\n";
                }
            }
            //转换为字符串
            if (nfcInfo == null || nfcInfo.equals("")) {
                //Toast.makeText(this, "读取失败", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "数据:" + nfcInfo, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nfcInfo;
    }


    //读取指定扇区，指定块的数据
    private String readTag(Tag mTag, MifareClassic mfc,int sector,int block) {
        String tmp = "";
        try {
            mfc.connect();//打开连接
            boolean auth=false;
            auth = mfc.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT);//keyA验证扇区
            if (auth) {
                byte[]data=mfc.readBlock(block);
                tmp = DataUtil.bytesToHexString(data);
                Log.e("kyl",new String(DataUtil.hexStringToBytes(tmp.substring(6,tmp.length()-6))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmp;
    }

}
