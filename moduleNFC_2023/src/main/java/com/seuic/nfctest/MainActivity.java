package com.seuic.nfctest;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseNfcActivity {

    TextView tvInfo;
    Button btnRead;
    Button btnWrite;
    Button btnMuRead;
    Button btnMuWrite;
    Button btnM1Read;
    Button btnM1Write;
    Button btnRead15693;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        {
            tvInfo=(TextView)findViewById(R.id.tvInfo);
            btnRead=(Button)findViewById(R.id.btnRead);
            btnWrite=(Button)findViewById(R.id.btnWrite);
            btnMuRead=(Button)findViewById(R.id.btnMuRead);
            btnMuWrite=(Button)findViewById(R.id.btnMuWrite);
            btnM1Read=(Button)findViewById(R.id.btnM1Read);
            btnM1Write=(Button)findViewById(R.id.btnM1Write);
            btnRead15693=(Button)findViewById(R.id.btnRead15693);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!ifNFCUse()) {
            btnRead.setEnabled(false);
            btnWrite.setEnabled(false);
        } else tvInfo.setText("NFC功能已开启！");

    }



    /**
     * 检测工作,判断设备的NFC支持情况
     *
     * @return
     */
    protected Boolean ifNFCUse() {
        if (mNfcAdapter == null) {
            tvInfo.setText("设备不支持NFC！");
            return false;
        }
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            tvInfo.setText("请在系统设置中先启用NFC功能！");
            return false;
        }
        return true;
    }


}
