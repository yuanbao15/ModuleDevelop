package com.example.uhf.fragment;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.yuanbao.moduleuhf_i6310.R;
import com.example.uhf.activity.Reader;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.UIHelper;


public class UHFKillFragment extends KeyDwonFragment {

    private static final String TAG = "UHFKillFragment";

    private UHFMainActivity mContext;

    CheckBox CkWithUii_Kill;
    EditText EtTagUii_Write;
    EditText EtAccessPwd_Kill;
    Button BtUii_Kill;
    Button btnKill;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uhf_kill_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();

        CkWithUii_Kill = (CheckBox) getView().findViewById(R.id.CkWithUii_Kill);
        EtTagUii_Write = (EditText) getView().findViewById(R.id.EtTagUii_Write);

        EtAccessPwd_Kill = (EditText) getView().findViewById(R.id.EtAccessPwd_Kill);
        BtUii_Kill = (Button) getView().findViewById(R.id.BtUii_Kill);
        btnKill = (Button) getView().findViewById(R.id.btnKill);

        BtUii_Kill.setEnabled(false);
        EtTagUii_Write.setKeyListener(null);

        CkWithUii_Kill.setOnCheckedChangeListener(new CkWithUii_WriteCheckedChangedListener());
        BtUii_Kill.setOnClickListener(new BtUii_WriteClickListener());

        btnKill.setOnClickListener(new btnKillOnClickListener());

    }

    public class BtUii_WriteClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            String uiiStr = Reader.rrlib.SingleInventory();

            if (uiiStr != null) {
                EtTagUii_Write.setText(uiiStr);
            } else {
                EtTagUii_Write.setText("");

                UIHelper.ToastMessage(mContext, R.string.uhf_msg_read_tag_fail);
//                mContext.playSound(2);
            }
        }
    }

    public class btnKillOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            String strPWD = EtAccessPwd_Kill.getText().toString().trim();// 访问密码

            if (!TextUtils.isEmpty(strPWD)) {
                if (strPWD.length() != 8) {
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_addr_must_len8);
                    return;
                } else if (!mContext.vailHexInput(strPWD)) {
                    UIHelper.ToastMessage(mContext,
                            R.string.rfid_mgs_error_nohex);

                    return;
                }
            } else {
                UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nopwd);

                return;
            }

            if (CkWithUii_Kill.isChecked())// 指定标签
            {

                String strUII = EtTagUii_Write.getText().toString().trim();
                if (TextUtils.isEmpty(strUII)) {
                    UIHelper.ToastMessage(mContext,
                            R.string.uhf_msg_tag_must_not_null);
                    return;
                }

                if (Reader.rrlib.Kill(strUII,strPWD)==0) {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_succ);
                      mContext.playSound(1);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_fail);
                    mContext.playSound(2);
                }

            } else {
                String strUII="";
                if (Reader.rrlib.Kill(strUII,strPWD)==0) {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_succ);
                    mContext.playSound(1);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_kill_fail);
                    mContext.playSound(2);
                }
            }
        }
    }

    public class CkWithUii_WriteCheckedChangedListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			EtTagUii_Write.setText("");

            if (isChecked) {
            	BtUii_Kill.setBackgroundResource(R.drawable.button_bg);
            	BtUii_Kill.setEnabled(true);
//                BtUii_Write.setVisibility(View.VISIBLE);
            } else {
            	BtUii_Kill.setBackgroundResource(R.drawable.button_bg_gray);
            	BtUii_Kill.setEnabled(false);
//                BtUii_Write.setVisibility(View.INVISIBLE);
            }
		}
    }

}
