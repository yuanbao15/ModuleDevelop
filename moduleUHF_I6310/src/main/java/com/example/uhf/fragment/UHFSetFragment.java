package com.example.uhf.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.example.uhf.activity.Reader;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.yuanbao.moduleuhf_i6310.R;

public class UHFSetFragment extends KeyDwonFragment implements OnClickListener {
    private UHFMainActivity mContext;

    private Button btnSetFre;
    private Button btnGetFre;
    private Spinner spMode;


    private ArrayAdapter adapter; //频点列表适配器

    private DisplayMetrics metrics;
    private AlertDialog dialog;
    private long[] timeArr;

    private Handler mHandler = new Handler();
    private int arrPow; //输出功率
    private int arrwPow; //输出功率

    public UHFSetFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater
                .inflate(R.layout.activity_uhfset, container, false);
        ViewUtils.inject(this, root);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (UHFMainActivity) getActivity();

        btnSetFre = (Button) getView().findViewById(R.id.BtSetFre);
        btnGetFre = (Button) getView().findViewById(R.id.BtGetFre);

        spMode = (Spinner) getView().findViewById(R.id.SpinnerMode);
        spMode.setOnItemSelectedListener(new MyOnTouchListener());


        btnSetFre.setOnClickListener(new SetFreOnclickListener());
        btnGetFre.setOnClickListener(new GetFreOnclickListener());

        String ver = Reader.rrlib.GetModuleType();
        arrPow = R.array.arrayPower;
        arrwPow = R.array.arrayPower;
        if (ver != null && ver.contains("981")) {
            arrPow = R.array.arrayPower2;
            arrwPow = R.array.arrayPower2;
        }
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, arrPow, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(mContext, arrwPow, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter adapter3 = ArrayAdapter.createFromResource(mContext, R.array.arrayAntiQ, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter adapter4 = ArrayAdapter.createFromResource(mContext, R.array.arraysession, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter adapter5 = ArrayAdapter.createFromResource(mContext, R.array.arrayNum, android.R.layout.simple_spinner_item);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter adapter6 = ArrayAdapter.createFromResource(mContext, R.array.men_scantime, android.R.layout.simple_spinner_item);
        adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getFre();
                getLinkParams();
            }
        });
    }

    /**
     * 工作模式下拉列表点击选中item监听
     */
    public class MyOnTouchListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class SetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            int mode =spMode.getSelectedItemPosition();
            switch (mode)
            {
                case 0:
                    mode+=1;
                    break;
                case 1:
                    mode+=1;
                    break;
                case 2:
                    mode+=1;
                    break;
                case 3:
                    mode+=1;
                    break;
                case 4:
                    mode=8;
                    break;
                default:
                    mode=2;
                    break;
            }
            if(Reader.rrlib.setFrequencyMode(mode)==0)
            {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_succ);
            }
            else
            {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_set_frequency_fail);
            }
        }
    }

    public void getFre() {
        int idx = Reader.rrlib.getFrequencyMode();

        if (idx != -1) {
            int count = spMode.getCount();
            switch (idx)
            {
                case 1:
                    spMode.setSelection(0);
                    break;
                case 2:
                    spMode.setSelection(1);
                    break;
                case 3:
                    spMode.setSelection(2);
                    break;
                case 4:
                    spMode.setSelection(3);
                    break;
                case 8:
                    spMode.setSelection(4);
                    break;
               default:
                    spMode.setSelection(1);
                    break;
            }
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_read_frequency_fail);
        }
    }


    /**
     * 获取链路参数
     */
    public void getLinkParams() {
        int profile =Reader.rrlib.getRFLink();
        if (profile!=-1) {

        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_get_para_fail);
        }
    }

    public class SetPWMOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }

    public class GetFreOnclickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            getFre();
        }
    }

    public class OnMyCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {


            }
        }
    }



    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            default:
                break;
        }
    }



    /**
     * 判断是否为累计点击5次且时间少于1600毫秒（调用一次即点击一次）
     *
     * @return
     */
    private boolean isFiveClick() {
        if (timeArr == null) {
            timeArr = new long[5];
        }
        System.arraycopy(timeArr, 1, timeArr, 0, timeArr.length - 1);
        timeArr[timeArr.length - 1] = System.currentTimeMillis();
        return System.currentTimeMillis() - timeArr[0] < 1600;
    }


    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public int getWindowWidth() {
        if (metrics == null) {
            metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        }
        return metrics.widthPixels;
    }

}
