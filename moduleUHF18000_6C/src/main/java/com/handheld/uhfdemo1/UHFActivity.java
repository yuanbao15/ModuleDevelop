package com.handheld.uhfdemo1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;
import com.android.hdhe.uhf.readerInterface.TagModel;
import com.yuanbao.rfid.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pda.serialport.Tools;

public class UHFActivity extends Activity implements OnClickListener
{
    /****************** for view:**************************************/
    private LinearLayout l1;
    private LinearLayout l2;
    private LinearLayout l3;
    private LinearLayout l4;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private View view1;
    private View view2;
    private View view3;
    private View view4;
    private RelativeLayout l1epc;
    private LinearLayout l2readandwrite;
    private LinearLayout l3lockandkill;
    private LinearLayout l4settings;
    private LinearLayout l5moresettings;
    private Button button_moresetting;

    private EditText editAccesslock ;
    private String accessPwd ;
    /******************************************/
    /****for uhf operation:**************************************/
    /******************************************/
    private Spinner spinnerEPCRead;
    private Spinner spinnerEPCLock;
    private Button buttonClear;
    private Button buttonStart;
    private TextView textVersion;
    private ListView listViewData;
    private ArrayList<EPC> listEPC;
    private ArrayList<String> listepc = new ArrayList<String>();
    private ArrayAdapter<String> arr_adapter;
    private ArrayList<Map<String, Object>> listMap;
    private boolean runFlag = true;
    private boolean startFlag = false;
    private UhfReader manager; // UHF manager,UHF Operating handle
//	private ScreenStateReceiver screenReceiver;
    /******************************************/
    private Spinner spinnerMemBank;// mem area
    private EditText editPassword;// password
    private EditText editAddr;// begin address
    private EditText editLength;// read data length
    private Button buttonRead;
    private Button buttonWrite;
    private EditText editWriteData;// write data
    private EditText editReadData;// read data
    // RESERVE EPC TID USER:0,1,2,3
    private final String[] strMemBank = { "RESERVE", "EPC", "TID", "USER" };
    /************************************/
    private ArrayAdapter<String> adatpterMemBank;
    private Spinner spinnerLockType;//
    private Button buttonLock;//
    private EditText editKillPassword;//
    private Button buttonKill;//
    private ArrayAdapter<CharSequence> adapterLockType;
    private int membank;//
    private int lockMembank;
    private int addr = 0;// begin address
    private int length = 1;// read or write  data length
    private int lockType;//
    private Button buttonBack;
    /******************************************/
    private Button button1;//set button1
    private Button button2;//set button2
    private Button button3;//set button3
    private Button button4;//set button4
    private Spinner spinnerSensitive;//The sensitivity
    private Spinner spinnerPower;//RF power
    private Spinner spinnerWorkArea;//work area
    private EditText editFrequency;// frequency
    private String[] powers = {"26dbm","24dbm","20dbm","18dbm","17dbm","16dbm"};
//private String[] powers = {"26dbm","25dm","24dbm","23dbm","22dbm","21dbm","20dbm","19dbm","18dbm","17dbm","16dbm"};
    private String[] sensitives = null;

    private String[] lockMemArrays = {"Kill Password", "Access password", "EPC", "TID", "USER"} ;
    private int lockMem = 0 ;
    private Spinner spinnerLockMem ;

    private String[] areas = null;
    private ArrayAdapter<String> adapterSensitive;
    private ArrayAdapter<String> adapterPower;
    private ArrayAdapter<String> adapterArea;
    private int sensitive = 0;
    private int power = 0 ;//rate of work
    private int area = 0;
    private int frequency = 0;

    private String what = "uhf";
    private String selectEpc = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOverflowShowingAlways();
        setContentView(R.layout.activity_uhf);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            setTitle(getString(R.string.app_name) + "-v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // Get the Rf power, and set
        shared = getSharedPreferences("UhfRfPower", 0);
        editor = shared.edit();
        power = shared.getInt("power", 26);
		area = shared.getInt("area", 2);
        //init view
        initView();
        //start inventory thread
        Thread thread = new InventoryThread();
        thread.start();
        // init sound pool
        Util.initSoundPool(this);
    }
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        String powerString = "";
//		switch (UhfManager.Power) {
//			case SerialPort.Power_3v3:
//				powerString = "power_3V3";
//				break;
//			case SerialPort.Power_5v:
//				powerString = "power_5V";
//				break;
//			case SerialPort.Power_Scaner:
//				powerString = "scan_power";
//				break;
//			case SerialPort.Power_Psam:
//				powerString = "psam_power";
//				break;
//			case SerialPort.Power_Rfid:
        powerString = "rfid_power";
//				break;
//			default:
//				break;
//		}
        TextView textView_title_config;
        textView_title_config = (TextView) findViewById(R.id.textview_title_config);
        textView_title_config.setText("Port:com" + 13
                +";Power:" + powerString);
        manager = UhfReader.getInstance();
        if (manager == null) {
            textVersion.setText(getString(R.string.serialport_init_fail_));
            setButtonClickable(buttonClear, false);
            setButtonClickable(buttonStart, false);
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        registerReceiver();

//		Log.e("", "value" + power);
        manager.setOutputPower(power);
        manager.setWorkArea(area);
//		byte[] version_bs = manager.getFirmware();
//		if (version_bs!=null){
//			textView_title_config.append("("+new String(version_bs)+")");
//		}
    }
    @Override
    protected void onPause() {
        startFlag = false;
        buttonStart.setText(R.string.inventory);
        manager.close();
        super.onPause();
        unregisterReceiver();
    }
    @Override
    protected void onDestroy() {
        startFlag = false;
        runFlag = false;
        if (manager != null) {
            manager.close();
        }
        super.onDestroy();
    }

    private void initView() {
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonClear = (Button) findViewById(R.id.button_clear);
        listViewData = (ListView) findViewById(R.id.listView_data);
        textVersion = (TextView) findViewById(R.id.textView_version);
        buttonStart.setOnClickListener(this);
        buttonClear.setOnClickListener(this);
        editAccesslock = (EditText) findViewById(R.id.edittext_access_lock);
        listEPC = new ArrayList<EPC>();
        l1 = (LinearLayout) findViewById(R.id.linearLayoutUhfEpc);
        l1.setOnTouchListener(new myOnTouch());
        l2 = (LinearLayout) findViewById(R.id.linearLayoutUhfRead);
        l2.setOnTouchListener(new myOnTouch());
        l3 = (LinearLayout) findViewById(R.id.linearLayoutUhfLock);
        l3.setOnTouchListener(new myOnTouch());
        l4 = (LinearLayout) findViewById(R.id.linearLayoutUhfSet);
        l4.setOnTouchListener(new myOnTouch());
        textView1 = (TextView) findViewById(R.id.textViewUhfEpc);
        textView2 = (TextView) findViewById(R.id.textViewUhfMore);
        textView3 = (TextView) findViewById(R.id.textViewUhfLock);
        textView4 = (TextView) findViewById(R.id.textViewUhfSet);
        view1 = findViewById(R.id.viewUhfEpc);
        view2 = findViewById(R.id.viewUhfMore);
        view3 = findViewById(R.id.viewUhfLock);
        view4 = findViewById(R.id.viewUhfSet);
        l1epc = (RelativeLayout) findViewById(R.id.l1epc);
        l2readandwrite = (LinearLayout) findViewById(R.id.l2read);
        l3lockandkill = (LinearLayout) findViewById(R.id.l3lock);
        l4settings = (LinearLayout) findViewById(R.id.l4settings);

        arr_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listepc);
        arr_adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEPCRead = (Spinner) findViewById(R.id.spinnerEPCread);
        spinnerEPCRead.setAdapter(arr_adapter);
        spinnerEPCLock = (Spinner) findViewById(R.id.spinnerEPClock);
        spinnerEPCLock.setAdapter(arr_adapter);
        spinnerMemBank = (Spinner) findViewById(R.id.spinner_membank);
        spinnerLockMem = (Spinner) findViewById(R.id.spinner_lock_mem);
        editAddr = (EditText) findViewById(R.id.edittext_addr);
        editLength = (EditText) findViewById(R.id.edittext_length);
        editPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonRead = (Button) findViewById(R.id.button_read);
        buttonWrite = (Button) findViewById(R.id.button_write);
        buttonClear = (Button) findViewById(R.id.button_readClear);
        buttonLock = (Button) findViewById(R.id.button_lock_6c);
        buttonKill = (Button) findViewById(R.id.button_kill_6c);
        buttonBack = (Button) findViewById(R.id.button_back);
        button_moresetting = (Button) findViewById(R.id.button_uhf_more_settings);
        l5moresettings = (LinearLayout) findViewById(R.id.layout_uhf_more_settings);
        button_moresetting.setOnClickListener(this);
        editKillPassword = (EditText) findViewById(R.id.edit_kill_password);
        editWriteData = (EditText) findViewById(R.id.edittext_write);
        editReadData = (EditText) findViewById(R.id.linearLayout_readData);
        adatpterMemBank = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, strMemBank);
        adatpterMemBank
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLockType = (Spinner) findViewById(R.id.spinner_lock_type);
        adapterLockType = ArrayAdapter.createFromResource(this,
                R.array.arr_lockType, android.R.layout.simple_spinner_item);
        adapterLockType
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLockType.setAdapter(adapterLockType);
        spinnerMemBank.setAdapter(adatpterMemBank);
        buttonClear.setOnClickListener(this);
        buttonRead.setOnClickListener(this);
        buttonWrite.setOnClickListener(this);
        buttonKill.setOnClickListener(this);
        buttonLock.setOnClickListener(this);
        buttonBack.setOnClickListener(this);
        spinnerMemBank.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                switch (arg2) {
                    case 0:
//						membank = UhfManager.RESERVE;
                        membank = UhfReader.MEMBANK_RESEVER ;
                        break;
                    case 1:
                        membank = UhfReader.MEMBANK_EPC;
                        break;
                    case 2:
                        membank = UhfReader.MEMBANK_TID;
                        break;
                    case 3:
                        membank = UhfReader.MEMBANK_USER;
                        break;
                    default:
                        break;
                }

                lockMembank = arg2 + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        spinnerLockMem.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, lockMemArrays));
        spinnerLockMem.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("lockMem", "lockMem") ;
                switch (position) {
                    case 0://lock kill password
                        lockMem = 0;
                        break ;
                    case 1://lock access password
                        lockMem = 1;
                        break ;
                    case 2://lock EPC
                        lockMem = 2;
                        break ;
                    case 3://lock TID
                        lockMem = 3;
                        break ;
                    case 4://lock USER
                        lockMem = 4;
                        break ;
                }
                Log.e("lockMem", "lockMem = " + lockMem) ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerLockType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                lockType = arg2;

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        spinnerEPCRead.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                if (listepc==null||listepc.size()==0) {
                    return;
                }
                manager.selectEPC(Tools.HexString2Bytes(listepc.get(position)));
                selectEpc = listepc.get(position);
                spinnerEPCRead.setSelection(position,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        spinnerEPCLock.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                if (listepc.size()==0) {
                    return;
                }
                manager.selectEPC(Tools.HexString2Bytes(listepc.get(position)));
                selectEpc = listepc.get(position);
                spinnerEPCLock.setSelection(position,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        button1 = (Button) findViewById(R.id.button_min);
        button2 = (Button) findViewById(R.id.button_plus);
        button3 = (Button) findViewById(R.id.button_set);
        button4 = (Button) findViewById(R.id.button4);

        spinnerSensitive = (Spinner) findViewById(R.id.spinner1);
        spinnerPower = (Spinner) findViewById(R.id.spinner2);
        spinnerWorkArea = (Spinner) findViewById(R.id.spinner3);
        editFrequency = (EditText) findViewById(R.id.edit4);
        sensitives = getResources().getStringArray(R.array.arr_sensitivity);
        areas = getResources().getStringArray(R.array.arr_area);

        adapterSensitive = new  ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sensitives);
        adapterPower = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, powers);
        adapterArea = new ArrayAdapter<String>(this	, android.R.layout.simple_dropdown_item_1line, areas);
        spinnerSensitive.setAdapter(adapterSensitive);
        spinnerPower.setAdapter(adapterPower);
        spinnerWorkArea.setAdapter(adapterArea);
        int power_position = 0;
        switch (power) {
            case 26:
                power_position = 0;
                break;
            case 24:
                power_position = 1;
                break;
            case 20:
                power_position = 2;
                break;
            case 18:
                power_position = 3;
                break;
            case 17:
                power_position = 4;
                break;
            case 16:
                power_position = 5;
                break;
            default:
                break;
        }
//        switch (power) {
//            case 26:
//                power_position = 0;
//                break;
//            case 25:
//                power_position = 1;
//                break;
//            case 24:
//                power_position = 2;
//                break;
//            case 23:
//                power_position = 3;
//                break;
//            case 22:
//                power_position = 4;
//                break;
//            case 21:
//                power_position = 5;
//                break;
//            case 20:
//                power_position = 6;
//                break;
//            case 19:
//                power_position = 7;
//                break;
//            case 18:
//                power_position = 8;
//                break;
//            case 17:
//                power_position = 9;
//                break;
//            case 16:
//                power_position = 10;
//                break;
//            default:
//                break;
//        }
        spinnerPower.setSelection(power_position, true);
        int area_position;
        if (area != 6) {
            area_position = area - 1;
        }else {
            area_position = 4;
        }
        spinnerWorkArea.setSelection(area_position, true);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        spinnerWorkArea.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        area = 1;
                        break;
                    case 1:
                        area = 2;
                        break;
                    case 2:
                        area = 3;
                        break;
                    case 3:
                        area = 4;
                        break;
                    case 4:
                        area = 6;
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinnerSensitive.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {

                Log.e("", sensitives[position]);
                switch (position) {
                    case 0:
                        sensitive = 3;
                        break;
                    case 1:
                        sensitive = 2;
                        break;
                    case 2:
                        sensitive = 1;
                        break;
                    case 3:
                        sensitive = 0;
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinnerPower.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                Log.e("", powers[position]);
                switch (position) {
                    case 0:
                        power =26;
                        break;
                    case 1:
                        power = 24;
                        break;
                    case 2:
                        power = 20;
                        break;
                    case 3:
                        power = 18;
                        break;
                    case 4:
                        power = 17;
                        break;
                    case 5:
                        power = 16;
                        break;
                    default:
                        break;
//                    case 0:
//                        power = 26;
//                        break;
//                    case 1:
//                        power =25;
//                        break;
//                    case 2:
//                        power = 24;
//                        break;
//                    case 3:
//                        power = 23;
//                        break;
//                    case 4:
//                        power = 22;
//                        break;
//                    case 5:
//                        power = 21;
//                        break;
//                    case 6:
//                        power = 20;
//                        break;
//                    case 7:
//                        power =19;
//                        break;
//                    case 8:
//                        power = 18;
//                        break;
//                    case 9:
//                        power = 17;
//                        break;
//                    case 10:
//                        power = 16;
//                        break;
//                    default:
//                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }



    /**
     * Inventory EPC Thread
     */
    class InventoryThread extends Thread {
        private List<TagModel> tagList;
        byte[] accessPassword = Tools.HexString2Bytes("00000000");
        @Override
        public void run() {
            super.run();
            while (runFlag) {
                if (startFlag) {
                    tagList = manager.inventoryRealTime(); //实时盘存
                    if(tagList != null && !tagList.isEmpty()){
                        //播放提示音
                        Util.play(1, 0);
                        for(TagModel tag:tagList){
                            if(tag == null){
                                String epcStr = "";
//								String epcStr = new String(epc);
                                addToList(listEPC, epcStr, (byte)-1);
                            }else{
                                String epcStr = Tools.Bytes2HexString(tag.getmEpcBytes(), tag.getmEpcBytes().length);
//								String epcStr = new String(epc);
                                byte rssi = tag.getmRssi();
                                addToList(listEPC, epcStr, rssi);
                            }

                        }
                    }
                    tagList = null ;
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // EPC add to LISTVIEW
    private void addToList(final List<EPC> list, final String epc, final byte rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // The epc for the first time
                if (list.isEmpty()) {
                    EPC epcTag = new EPC();
                    epcTag.setEpc(epc);
                    epcTag.setCount(1);
                    epcTag.setRssi(rssi);
                    list.add(epcTag);
                    listepc.add(epc);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        EPC mEPC = list.get(i);
                        // list contain this epc
                        if (epc.equals(mEPC.getEpc())) {
                            mEPC.setCount(mEPC.getCount() + 1);
                            mEPC.setRssi(rssi);
                            list.set(i, mEPC);
                            break;
                        } else if (i == (list.size() - 1)) {
                            // list doesn't contain this epc
                            EPC newEPC = new EPC();
                            newEPC.setEpc(epc);
                            newEPC.setCount(1);
                            newEPC.setRssi(rssi);
                            list.add(newEPC);
                            listepc.add(epc);
                        }
                    }
                }
                // add the data to ListView
                listMap = new ArrayList<Map<String, Object>>();
                int idcount = 1;
                for (EPC epcdata : list) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("ID", idcount);
                    map.put("EPC", epcdata.getEpc());
                    map.put("COUNT", epcdata.getCount());
                    map.put("RSSI", epcdata.getRssi());
                    idcount++;
                    listMap.add(map);
                }
                listViewData.setAdapter(new SimpleAdapter(UHFActivity.this,
                        listMap, R.layout.listview_item, new String[] { "ID",
                        "EPC", "COUNT", "RSSI"}, new int[] {
                        R.id.textView_list_item_id,
                        R.id.textView_list_item_barcode,
                        R.id.textView_list_item_count,
                        R.id.textView_list_item_rssi}));
                spinnerEPCRead.setAdapter(arr_adapter);
                spinnerEPCLock.setAdapter(arr_adapter);
                // play sound
                Util.play(1, 0);
            }
        });
    }

    // Make the button clickable or unclickable
    private void setButtonClickable(Button button, boolean flag) {
        button.setClickable(flag);
        if (flag) {
            button.setTextColor(Color.BLACK);
        } else {
            button.setTextColor(Color.GRAY);
        }
    }


    /**
     * clear list and listview O
     */
    private void clearData() {
        listEPC.removeAll(listEPC);
        listViewData.setAdapter(null);
        listepc.removeAll(listepc);
    }

    @Override
    public void onClick(View v) {
        byte[] accessPassword = Tools.HexString2Bytes(editPassword.getText()
                .toString());
        addr = Integer.valueOf(editAddr.getText().toString());
        length = Integer.valueOf(editLength.getText().toString());
        if (v.getId() == R.id.button_start) {
            if (!startFlag) {
                startFlag = true;
                buttonStart.setText(R.string.stop_inventory);
            } else {
                startFlag = false;
                buttonStart.setText(R.string.inventory);
            }
        } else if (v.getId() == R.id.button_clear) {
            int frequency = manager.getFrequency();
            clearData();
        } else if (v.getId() == R.id.button_read) {
            manager.selectEPC(Tools.HexString2Bytes(selectEpc));
            if (accessPassword.length != 4) {
                showToast(getString(R.string.password_is_4_bytes));
                return;
            }
            // read data
            byte[] data = manager.readFrom6C(membank, addr, length, accessPassword);
            if (data != null && data.length > 1) {
                String dataStr = Tools.Bytes2HexString(data, data.length);
                editReadData.append(getString(R.string.read_data_) + dataStr + "\n");
            } else {
                if (data != null) {
                    editReadData.append(getString(R.string.read_fail_error) + (data[0] & 0xff) + "\n");
                    return;
                }
                editReadData.append(getString(R.string.read_fail_return_null) + "\n");
            }
        } else if (v.getId() == R.id.button_write) {
            manager.selectEPC(Tools.HexString2Bytes(selectEpc));
            if (accessPassword.length != 4) {
                showToast(getString(R.string.password_is_4_bytes));
                return;
            }
            String writeData = editWriteData.getText().toString();
            if (writeData.length() % 4 != 0) {
                showToast(getString(R.string.the_unit_is_word_1word_2bytes));
            }
            byte[] dataBytes = Tools.HexString2Bytes(writeData);
            // dataLen = dataBytes/2 dataLen
            boolean writeFlag = manager.writeTo6C(accessPassword, membank,
                    addr, dataBytes.length / 2, dataBytes);
            if (writeFlag) {
                editReadData.append(getString(R.string.write_successful_) + "\n");
            } else {
                editReadData.append(getString(R.string.write_failue_) + "\n");
            }
        } else if (v.getId() == R.id.button_lock_6c) {
            manager.selectEPC(Tools.HexString2Bytes(selectEpc));
            String password = editAccesslock.getText().toString();
            if (manager.lock6C(Tools.HexString2Bytes(password), lockMem, lockType)) {
                showToast(getString(R.string.lock_successful_));
            }else {
                showToast(getString(R.string.lock_fail_));
            }
        } else if (v.getId() == R.id.button_kill_6c) {
            final AlertDialog dlg = new AlertDialog.Builder(UHFActivity.this)
                    .setTitle(R.string.sure_kill)
                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            manager.selectEPC(Tools.HexString2Bytes(selectEpc));
                            String killPassword = ((EditText)findViewById(R.id.edit_kill_password)).getText().toString();
                            if (manager.kill6C(Tools.HexString2Bytes(killPassword))) {
                                showToast("Kill success!");
                            }else {
                                showToast("Kill fail!");
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .create();
            dlg.show();
        } else if (v.getId() == R.id.button_readClear) {
            editReadData.setText("");
        } else if (v.getId() == R.id.button_min) {
            manager.setSensitivity(sensitive);
            showToast(getString(R.string.setSuccess));
        } else if (v.getId() == R.id.button_plus) {
            editor.putInt("power", power);
            editor.commit();
            if (manager.setOutputPower(power)) {
                showToast(getString(R.string.setSuccess));
            }
        } else if (v.getId() == R.id.button_set) {
            manager.setWorkArea(area);
            editor.putInt("area", area);
            editor.commit();
            showToast(getString(R.string.setSuccess));
        } else if (v.getId() == R.id.button4) {
            String freqStr = editFrequency.getText().toString();
            if(freqStr == null || "".equals(freqStr)){
                showToast(getString(R.string.freqSetting));
                return;
            }
            showToast(getString(R.string.setSuccess));
        } else if (v.getId() == R.id.button_uhf_more_settings) {
            l5moresettings.setVisibility(View.GONE);
            AlertDialog dlg2 = new AlertDialog.Builder(UHFActivity.this)
                    .setTitle(R.string.note_the_following_operation_may_lead_to_module_does_not_work_)

                    .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            l5moresettings.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    })
                    .create();
            dlg2.show();
        }
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * on actionbar show menu button
     */
    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class myOnTouch implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent arg1) {
            if (view.getId() == R.id.linearLayoutUhfEpc) {
                SetVisible(null, textView1, view1);
            } else if (view.getId() == R.id.linearLayoutUhfRead) {
                SetVisible(l2readandwrite, textView2, view2);
            } else if (view.getId() == R.id.linearLayoutUhfLock) {
                SetVisible(l3lockandkill, textView3, view3);
            } else if (view.getId() == R.id.linearLayoutUhfSet) {
                SetVisible(l4settings, textView4, view4);
            }

            return true;
        }
    }

    private void SetVisible(LinearLayout layout, TextView textView, View view) {
        if (listepc.size()==0&&(layout==l2readandwrite||layout==l3lockandkill)){
            showToast("Please inventory!");
            return;
        }
        l1epc.setVisibility(View.GONE);
        l2readandwrite.setVisibility(View.GONE);
        l3lockandkill.setVisibility(View.GONE);
        l4settings.setVisibility(View.GONE);

        textView1.setTextColor(getResources().getColor(R.color.black));
        view1.setBackgroundColor(getResources().getColor(R.color.white));
        textView2.setTextColor(getResources().getColor(R.color.black));
        view2.setBackgroundColor(getResources().getColor(R.color.white));
        textView3.setTextColor(getResources().getColor(R.color.black));
        view3.setBackgroundColor(getResources().getColor(R.color.white));
        textView4.setTextColor(getResources().getColor(R.color.black));
        view4.setBackgroundColor(getResources().getColor(R.color.white));
        textView.setTextColor(getResources().getColor(R.color.tabSelect));
        view.setBackgroundColor(getResources().getColor(R.color.tabSelect));
        if (layout==null) {
            l1epc.setVisibility(View.VISIBLE);
            startFlag = false;
            buttonStart.setText(R.string.inventory);
        }else {

            layout.setVisibility(View.VISIBLE);
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime < 2000) {
                finish();
            } else {
                exitTime = System.currentTimeMillis();
                showToast("Double click to exit!");
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * show Toast
     */
    private Toast mToast;
    private void showToast(String message){
        if (mToast == null){
            mToast = Toast.makeText(UHFActivity.this, message, Toast.LENGTH_SHORT);
            mToast.show();
        }else {
            mToast.setText(message);
            mToast.show();
        }
    }

    private  Toast toast;
    private KeyReceiver keyReceiver;

    private void registerReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        registerReceiver(keyReceiver , filter);
    }
    private void unregisterReceiver(){
        unregisterReceiver(keyReceiver);
    }
    private class KeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
                if (toast == null) {
                    toast = Toast.makeText(UHFActivity.this, "KeyReceiver:keyCode = down" + keyCode, Toast.LENGTH_SHORT);
                } else {
                    toast.setText("KeyReceiver:keyCode = down" + keyCode);
                }
                toast.show();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                    case KeyEvent.KEYCODE_F2:
                    case KeyEvent.KEYCODE_F3:
                    case KeyEvent.KEYCODE_F4:
                    case KeyEvent.KEYCODE_F5:
                        onClick(buttonStart);
                        break;
                }
            }


        }
    }
}
