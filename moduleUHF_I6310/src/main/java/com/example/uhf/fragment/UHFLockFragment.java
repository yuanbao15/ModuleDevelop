package com.example.uhf.fragment;



import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.uhf.activity.Reader;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.UIHelper;
import com.yuanbao.moduleuhf_i6310.R;


public class UHFLockFragment extends KeyDwonFragment implements  OnClickListener{

    private static final String TAG = "UHFLockFragment";
    private UHFMainActivity mContext;
    EditText EtAccessPwd_Lock;
    Button btnLock;
    EditText etLockCode;



    CheckBox cb_filter_lock;
    EditText etPtr_filter_lock;
    EditText etLen_filter_lock;
    EditText etData_filter_lock;
    RadioButton rbEPC_filter_lock;
    RadioButton rbTID_filter_lock;
    RadioButton rbUser_filter_lock;
    public byte select=0;
    public byte setprotect=0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uhf_lock_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        etLockCode = (EditText) getView().findViewById(R.id.etLockCode);
        EtAccessPwd_Lock = (EditText) getView().findViewById(R.id.EtAccessPwd_Lock);
        btnLock = (Button) getView().findViewById(R.id.btnLock);

        etPtr_filter_lock = (EditText) getView().findViewById(R.id.etPtr_filter_lock);
        etLen_filter_lock = (EditText) getView().findViewById(R.id.etLen_filter_lock);


        rbEPC_filter_lock= (RadioButton) getView().findViewById(R.id.rbEPC_filter_lock);
        rbTID_filter_lock= (RadioButton) getView().findViewById(R.id.rbTID_filter_lock);
        rbUser_filter_lock= (RadioButton) getView().findViewById(R.id.rbUser_filter_lock);

        cb_filter_lock = (CheckBox) getView().findViewById(R.id.cb_filter_lock);
        etData_filter_lock = (EditText) getView().findViewById(R.id.etData_filter_lock);

        rbEPC_filter_lock.setOnClickListener(this);
        rbTID_filter_lock.setOnClickListener(this);
        rbUser_filter_lock.setOnClickListener(this);


        cb_filter_lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    String data = etData_filter_lock.getText().toString().trim();
                    String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                    if(data==null || data.isEmpty() || !data.matches(rex)) {
                        UIHelper.ToastMessage(mContext,"过滤的数据必须是十六进制数据");
                        cb_filter_lock.setChecked(false);
                        return;
                    }
                }
            }
        });

        etLockCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.tvLockCode);
                final View vv = LayoutInflater.from(mContext).inflate(R.layout.uhf_dialog_lock_code, null);
                builder.setView(vv);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        etLockCode.getText().clear();
                    }
                });

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RadioButton rbOpen = (RadioButton) vv.findViewById(R.id.rbOpen);
                        RadioButton rbLock= (RadioButton) vv.findViewById(R.id.rbLock);
                        RadioButton rbLockforever= (RadioButton) vv.findViewById(R.id.rbLockforever);
                        RadioButton rbOpenforever= (RadioButton) vv.findViewById(R.id.rbOpenforever);


                        RadioButton cbKill = (RadioButton) vv.findViewById(R.id.cbKill);
                        RadioButton cbAccess = (RadioButton) vv.findViewById(R.id.cbAccess);
                        RadioButton cbEPC = (RadioButton) vv.findViewById(R.id.cbEPC);
                        RadioButton cbTid = (RadioButton) vv.findViewById(R.id.cbTid);
                        RadioButton cbUser = (RadioButton) vv.findViewById(R.id.cbUser);
                        if(rbOpen.isChecked())
                            setprotect=0;
                        if(rbLock.isChecked())
                            setprotect=2;
                        if(rbOpenforever.isChecked())
                            setprotect=1;
                        if(rbLockforever.isChecked())
                            setprotect=3;


                        if(cbKill.isChecked())
                            select =0;
                        if(cbAccess.isChecked())
                            select =1;
                        if(cbEPC.isChecked())
                            select =2;
                        if(cbTid.isChecked())
                            select =3;
                        if(cbUser.isChecked())
                            select =4;
                        String mask = "";
                        String value = "";

                        etLockCode.setText(String.valueOf(select)+","+String.valueOf(setprotect));
                        /*int[] data=new int[20];
                        if(cbUser.isChecked()) {
                            data[11] = 1;
                            if (cbPerm.isChecked()) {
                                data[0] = 1;
                                data[10] = 1;
                            }
                            if(rbLock.isChecked()){
                                data[1]=1;
                            }
                        }
                        if(cbTid.isChecked()){
                            data[13]=1;
                            if(cbPerm.isChecked()){
                                data[12]=1;
                                data[2]=1;
                            }
                            if(rbLock.isChecked()){
                                data[3]=1;
                            }
                        }
                        if(cbEPC.isChecked()){
                            data[15]=1;
                            if(cbPerm.isChecked()){
                                data[14]=1;
                                data[4]=1;
                            }
                            if(rbLock.isChecked()){
                                data[5]=1;
                            }
                        }
                        if(cbAccess.isChecked()){
                            data[17]=1;
                            if(cbPerm.isChecked()){
                                data[16]=1;
                                data[6]=1;
                            }
                            if(rbLock.isChecked()){
                                data[7]=1;
                            }
                        }
                        if(cbKill.isChecked()){
                            data[19]=1;
                            if(cbPerm.isChecked()){
                                data[18]=1;
                                data[8]=1;
                            }
                            if(rbLock.isChecked()){
                                data[9]=1;
                            }
                        }
                        StringBuffer stringBuffer=new StringBuffer();
                        stringBuffer.append("0000");
                        for(int k=data.length-1;k>=0;k--){
                            stringBuffer.append(data[k]+"");
                        }

                        String code =binaryString2hexString(stringBuffer.toString());
                        Log.i(TAG, "  tempCode="+stringBuffer.toString()+"  code=" + code);

                        etLockCode.setText(code.replace(" ", "0") + "");*/
                    }
                });
                builder.create().show();
            }
        });

        btnLock.setOnClickListener(new btnLockOnClickListener());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rbEPC_filter_lock) {
            etPtr_filter_lock.setText("32");
        } else if (view.getId() == R.id.rbTID_filter_lock) {
            etPtr_filter_lock.setText("0");
        } else if (view.getId() == R.id.rbUser_filter_lock) {
            etPtr_filter_lock.setText("0");
        }
    }


    public class btnLockOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            String strPWD = EtAccessPwd_Lock.getText().toString().trim();// 访问密码
            String strLockCode = etLockCode.getText().toString().trim();

            if (!TextUtils.isEmpty(strPWD)) {
                if (strPWD.length() != 8) {
                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_addr_must_len8);
                    return;
                } else if (!mContext.vailHexInput(strPWD)) {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nohex);
                    return;
                }
            } else {
                UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nopwd);
                return;
            }

            if (TextUtils.isEmpty(strLockCode)) {
                UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nolockcode);
                return;
            }
            boolean result=false;
            if(cb_filter_lock.isChecked()){
                String filterData=etData_filter_lock.getText().toString();
                if(filterData==null || filterData.isEmpty()){
                    UIHelper.ToastMessage(mContext, "过滤数据不能为空!");
                    return;
                }
                if(etPtr_filter_lock.getText().toString()==null || etPtr_filter_lock.getText().toString().isEmpty()){
                    UIHelper.ToastMessage(mContext, "过滤起始地址不能为空");
                    return;
                }
                if(etLen_filter_lock.getText().toString()==null || etLen_filter_lock.getText().toString().isEmpty()){
                    UIHelper.ToastMessage(mContext, "过滤数据长度不能为空");
                    return;
                }
                int filterPtr=Integer.parseInt(etPtr_filter_lock.getText().toString());
                int filterCnt=Integer.parseInt(etLen_filter_lock.getText().toString());
                int maskmem=1;
                if(rbEPC_filter_lock.isChecked()){
                    maskmem=1;
                }else if(rbTID_filter_lock.isChecked()){
                    maskmem=2;
                }else if(rbUser_filter_lock.isChecked()){
                    maskmem=3;
                }
                int fCmdRet = Reader.rrlib.LockByMask(maskmem,filterPtr, filterCnt,filterData,select,setprotect,strPWD);
                if (fCmdRet==0) {
                    result=true;
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_lock_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_lock_fail);
                }
            }else{
                int fCmdRet = Reader.rrlib.LockByEPC("",select,setprotect,strPWD);
                if (fCmdRet==0) {
                    result=true;
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_lock_succ);
                } else {
                    UIHelper.ToastMessage(mContext, R.string.rfid_mgs_lock_fail);
                }
            }
            if(!result){
                mContext.playSound(2);
            }else{
                mContext.playSound(1);
            }

        }
    }
    public static String binaryString2hexString(String bString)
    {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4)
        {
            iTmp = 0;
            for (int j = 0; j < 4; j++)
            {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }

}
