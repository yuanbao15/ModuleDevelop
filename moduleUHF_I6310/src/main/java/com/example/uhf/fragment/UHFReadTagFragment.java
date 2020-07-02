package com.example.uhf.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.uhf.activity.Reader;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.rfid.trans.ReadTag;
import com.rfid.trans.TagCallback;
import com.yuanbao.moduleuhf_i6310.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class UHFReadTagFragment extends KeyDwonFragment {

    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Button BtClear;
    TextView tv_count;
    TextView tv_time;
    TextView tv_alltag;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;
   // Button Btimport;
    Button BtInventory;
    ListView LvTags;
 //   private Button btnFilter;//过滤
    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;
    public boolean isStopThread=false;
    MsgCallback callback = new MsgCallback();
    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MSG_UPDATE_TIME = 1;
    private static final int MSG_UPDATE_ERROR = 2;
    private static final int MSG_UPDATE_STOP = 3;
    private Timer timer;
    public long beginTime;
    public long CardNumber;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");
        return inflater
                .inflate(R.layout.uhf_readtag_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        tagList = new ArrayList<HashMap<String, String>>();
        BtClear = (Button) getView().findViewById(R.id.BtClear);
      //  Btimport = (Button) getView().findViewById(R.id.BtImport);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        tv_time = (TextView) getView().findViewById(R.id.tv_times);
        tv_alltag = (TextView) getView().findViewById(R.id.tv_alltag);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        String tr = "";
        RbInventorySingle = (RadioButton) getView()
                .findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView()
                .findViewById(R.id.RbInventoryLoop);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi});

        BtClear.setOnClickListener(new BtClearClickListener());
        //Btimport.setOnClickListener(new BtImportClickListener());
        RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());

        Reader.rrlib.SetCallBack(callback);
        //btnFilter = (Button) getView().findViewById(R.id.btnFilter);

        LvTags.setAdapter(adapter);
        clearData();
        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try{
                    switch (msg.what) {
                        case MSG_UPDATE_LISTVIEW:
                            String result = msg.obj+"";
                            String[] strs = result.split(",");
                            addEPCToList(strs[0], strs[1]);
                            mContext.playSound(1);
                            break;
                        case MSG_UPDATE_TIME:
                            String ReadTime = msg.obj+"";
                            tv_time.setText(ReadTime);
                            break;
                        case MSG_UPDATE_ERROR:

                            break;
                        case MSG_UPDATE_STOP:
                            setViewEnabled(true);
                            BtInventory.setText(mContext.getString(R.string.btInventory));
                            break;
                        default:
                            break;
                    }
                }catch(Exception ex)
                {ex.toString();}
            }
        };
    }

    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isStopThread =false;
        CardNumber=0;
    }

    /**
     * 添加EPC到列表中
     *
     * @param epc
     */
    private void addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            map = new HashMap<String, String>();

            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);
            CardNumber++;
            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));

                tagList.set(index, map);

            }
            tv_alltag.setText(String.valueOf(CardNumber));
            adapter.notifyDataSetChanged();

        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            clearData();

        }
    }


    public class BtImportClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            if (BtInventory.getText().equals(
                    mContext.getString(R.string.btInventory))) {
                if(tagList.size()==0) {

                    UIHelper.ToastMessage(mContext, "无数据导出");
                    return;
                }
                boolean re = FileImport.daochu("", tagList);
                if (re) {
                    UIHelper.ToastMessage(mContext, "导出成功");

                    tv_count.setText("0");

                    tagList.clear();

                    Log.i("MY", "tagList.size " + tagList.size());

                    adapter.notifyDataSetChanged();
                }
            }
            else
            {
                UIHelper.ToastMessage(mContext, "请停止扫描后再导出");
            }
        }


    }

    private void clearData() {
       tv_count.setText("0");
        tv_time.setText("0");
        tv_alltag.setText("0");
        tagList.clear();
        CardNumber =0;
        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
           // llContinuous.setVisibility(View.GONE);
            if (checkedId == RbInventorySingle.getId()) {
                // 单步识别
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                // 单标签循环识别
                inventoryFlag = 1;
             //   llContinuous.setVisibility(View.VISIBLE);
            }
        }
    }


    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(
                mContext.getString(R.string.btInventory)))// 识别标签
        {
            switch (inventoryFlag) {
                case 0:// 单步
                {
                    String epcid = Reader.rrlib.SingleInventory();
                    if(epcid!=null)
                    {
                        mContext.playSound(1);
                    }
                }
                break;
                case 1:
                {
                    int result = Reader.rrlib.StartRead();
                    if(result==0)
                    {
                        BtInventory.setText(mContext
                                .getString(R.string.title_stop_Inventory));
                        setViewEnabled(false);
                        if(timer == null) {
                            beginTime = System.currentTimeMillis();
                            timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    long ReadTime = System.currentTimeMillis() - beginTime;
                                    Message msg = handler.obtainMessage();
                                    msg.what = MSG_UPDATE_TIME;
                                    msg.obj = String.valueOf(ReadTime) ;
                                    handler.sendMessage(msg);
                                }
                            }, 0, 10);
                        }

                    }
                }
                break;
                default:
                    break;
            }
        } else {// 停止识别

            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
     //   btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        Reader.rrlib.StopRead();
        if(timer != null){
            timer.cancel();
            timer = null;
            BtInventory.setText(mContext.getString(R.string.title_stoping_Inventory));
        }
    }


    public class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag arg0) {
            // TODO Auto-generated method stub
            String epc = arg0.epcId.toUpperCase();
            String rssi = String.valueOf(arg0.rssi);
            Message msg = handler.obtainMessage();
            msg.what = MSG_UPDATE_LISTVIEW;
            msg.obj =epc+","+rssi ;
            handler.sendMessage(msg);
        }

        @Override
        public int CRCErrorCallBack(int reason) {
            // TODO Auto-generated method stub

            return 0;
        }

        @Override
        public void FinishCallBack() {
            // TODO Auto-generated method stub
            Message msg = handler.obtainMessage();
            msg.what = MSG_UPDATE_STOP;
            msg.obj ="" ;
            handler.sendMessage(msg);
        }

        @Override
        public int tagCallbackFailed(int reason) {
            // TODO Auto-generated method stub
            return 0;
        }};

    /**
     * 判断EPC是否在列表中
     *
     * @param strEPC 索引
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    class TagThread extends Thread {
        public void run() {

        }
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }

}
