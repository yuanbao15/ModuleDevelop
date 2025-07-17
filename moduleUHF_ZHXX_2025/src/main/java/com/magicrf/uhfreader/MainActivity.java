package com.magicrf.uhfreader;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhfdemo.R;
import com.magicrf.uhfreaderlib.reader.Tools;
import com.magicrf.uhfreaderlib.reader.UhfReader;
import com.pl.serialport.SerialPort;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "HF_READER";

	private Button buttonClear;
	private Button buttonConnect;
	private Button buttonStart;
	private Button btn_set,readBlockBnt,writeBlockBnt;
	private TextView textVersion ;
	private ListView listViewData;
	private ArrayList<EPC> listEPC;
	private ArrayList<Map<String, Object>> listMap;
	private boolean runFlag = true;
	private boolean startFlag = false;
	private boolean connectFlag = false;
	private String serialPortPath = "/dev/ttyS1";
	private UhfReader reader; //超高频读写器
	private String hardwareVersion;

	private LinearLayout ll_read,ll_inv;
	private EditText readBlockEditText,WriteDataeditText;

	private ScreenStateReceiver screenReceiver ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setOverflowShowingAlways();
		setContentView(R.layout.main);
		initView();

		UhfReaderDevice.getInstance();

		//添加广播，默认屏灭时休眠，屏亮时唤醒
		screenReceiver = new ScreenStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenReceiver, filter);

		//初始化声音池
		Util.initSoundPool(this);
	}

	private void initView(){
		buttonStart = (Button) findViewById(R.id.button_start);
		btn_set = (Button)findViewById(R.id.btn_set);
		buttonConnect = (Button) findViewById(R.id.button_connect);
		buttonClear = (Button) findViewById(R.id.button_clear);
		listViewData = (ListView) findViewById(R.id.listView_data);
		textVersion = (TextView) findViewById(R.id.textView_version);

		ll_inv = (LinearLayout) findViewById(R.id.ll_inv);
		ll_read = (LinearLayout) findViewById(R.id.ll_read);

		readBlockEditText = (EditText) findViewById(R.id.readBlockEditText);
		WriteDataeditText = (EditText) findViewById(R.id.WriteDataeditText);
		readBlockBnt =  (Button) findViewById(R.id.readBlockBnt);
		writeBlockBnt =  (Button) findViewById(R.id.writeBlockBnt);
		readBlockBnt.setOnClickListener(this);
		writeBlockBnt.setOnClickListener(this);

		buttonStart.setOnClickListener(this);
		btn_set.setOnClickListener(this);
		buttonConnect.setOnClickListener(this);
		buttonClear.setOnClickListener(this);
		setButtonClickable(buttonStart, false);
		setButtonClickable(btn_set, false);
		listEPC = new ArrayList<EPC>();
//		listViewData.setOnItemClickListener(this);
	}


	@Override
	protected void onPause() {
		startFlag = false;
		super.onPause();
	}


	//将读取的EPC添加到LISTVIEW
	private void addToList(final List<EPC> list, final String epc){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				//第一次读入数据
				if(list.isEmpty()){
					EPC epcTag = new EPC();
					epcTag.setEpc(epc);
					epcTag.setCount(1);
					writeLog(epc);
					list.add(epcTag);
				}else{
					for(int i = 0; i < list.size(); i++){
						EPC mEPC = list.get(i);
						//list中有此EPC
						if(epc.equals(mEPC.getEpc())){
							mEPC.setCount(mEPC.getCount() + 1);
							list.set(i, mEPC);
							break;
						}else if(i == (list.size() - 1)){
							//list中没有此epc
							EPC newEPC = new EPC();
							newEPC.setEpc(epc);
							newEPC.setCount(1);
							writeLog(epc);
							list.add(newEPC);
						}
					}
				}
				//将数据添加到ListView
				listMap = new ArrayList<Map<String,Object>>();
				int idcount = 1;
				for(EPC epcdata:list){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("ID", idcount);
					map.put("EPC", epcdata.getEpc());
					map.put("COUNT", epcdata.getCount());
					idcount++;
					listMap.add(map);
				}
				listViewData.setAdapter(new SimpleAdapter(MainActivity.this,
						listMap, R.layout.listview_item,
						new String[]{"ID", "EPC", "COUNT"},
						new int[]{R.id.textView_id, R.id.textView_epc, R.id.textView_count}));
			}
		});
	}

	//设置按钮是否可用
	private void setButtonClickable(Button button, boolean flag){
		button.setClickable(flag);
		if(flag){
			button.setTextColor(Color.BLACK);
		}else{
			button.setTextColor(Color.GRAY);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (screenReceiver != null) {
			unregisterReceiver(screenReceiver);
		}

		UhfReaderDevice.powerOff();
		closeSerialPort();
	}
	/**
	 * 清空listview
	 */
	private void clearData(){
		listEPC.removeAll(listEPC);
		listViewData.setAdapter(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.readBlockBnt) {
			readBlock();
		} else if (v.getId() == R.id.writeBlockBnt) {
			writeData();
		} else if (v.getId() == R.id.button_start) {
			// button_start 的点击事件为空
		} else if (v.getId() == R.id.button_connect) {
			initSerialPort(); // 打开串口
		} else if (v.getId() == R.id.button_clear) {
			clearData();
		} else if (v.getId() == R.id.btn_set) {
			if (btn_set.getText().toString().equals("写卡")) {
				ll_read.setVisibility(View.VISIBLE);
				ll_inv.setVisibility(View.GONE);
				btn_set.setText("连续读卡");
				mCmd = (byte) 0xFF;
			} else {
				ll_read.setVisibility(View.GONE);
				ll_inv.setVisibility(View.VISIBLE);
				btn_set.setText("写卡");
				mCmd = (byte) 0x0;
			}
		}
	}

	byte mCmd= (byte)0x0;
	byte[] mTBuffer = new byte[32];
	protected byte[] DataBuffer = new byte[64];
	protected int DataCount = 0;

	private void readBlock(){
		m_uartTemp = "";
		mCmd = (byte)0xA3;
		Log.e(TAG,"Sending Read Block cmd...\n");
		try {
			if ((mOutputStream != null)) {
				mTBuffer[0] = 0x01; //01 08 A3 20 0A 01 00 7E
				mTBuffer[1] = 0x08;
				mTBuffer[2] = (byte)0xA3;
				mTBuffer[3] = 0x20;
				mTBuffer[4] = 0x06; //块10
				mTBuffer[5] = 0x01;
				mTBuffer[6] = 0x00;
				CheckSum(mTBuffer,(byte)8);
//				mTBuffer[7] = 0x7E;

				writeSerial(mTBuffer);
				Log.e(TAG, "Send completion!\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeData(){
		m_uartTemp = "";
		mCmd = (byte)0xA4;
		byte[] mBlockData = new byte[16];
		String mStrData = WriteDataeditText.getText().toString();
		while(mStrData.length() < 32)
		{
			mStrData += '0';
		}
		mBlockData = Tools.HexString2Bytes(mStrData) ;//ByteUtil.hexStrToByte(mStrData);
		mTBuffer[0] = 0x01;
		mTBuffer[1] = 0x17;
		mTBuffer[2] = (byte)0xA4;
		mTBuffer[3] = 0x20;
		mTBuffer[4] = 0x06; //块10
		mTBuffer[5] = 0x01;
		for(int i=0;i<16;i++)
		{
			mTBuffer[6 + i] = mBlockData[i];
		}
		CheckSum(mTBuffer,(byte)23);
		writeSerial(mTBuffer);
	}

	public void CheckSum(byte[] buf, byte len)
	{
		byte i;
		byte checksum;
		checksum = 0;
		for (i = 0; i < (len - 1); i++)
		{
			checksum ^= buf[i];
		}
		buf[len - 1] = (byte)~checksum;
	}

	private void saveSharedVersion(String versionType, String version) {
		SharedPreferences shared = getSharedPreferences("versions", 0);
		Editor editor = shared.edit();
		editor.putString(versionType, version);
		editor.commit();
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
	 * 在actionbar上显示菜单按钮
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

	//  RFID写入日志文件
	public static void writeLog(String barCode) {
		String fileName = "uhfData.csv";

		//====加上時間======
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		barCode = "'"+barCode + "," +formatter.format(curDate) + "\r\n";

		String path = Environment.getExternalStorageDirectory().getPath() +  "/scanservice/"; //文件路径
		FileWriter writer = null;
		try {
			File file = new File(path);
			if (!file.exists()) {  //没有创建文件夹则创建
				file.mkdirs();
			}
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(path + fileName, true);
			writer.write(barCode);
			writer.flush();
			if (writer != null) {
				//关闭流
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>串口>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private boolean readThreadFlag = false;

	private String serialport ;
	private int baudrate ;

	public Handler handler = new Handler() ;
	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				if(!readThreadFlag)
					return;

				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	private String m_uartTemp = "";

	private void onDataReceived( final byte[] buffer,final int size) {
		handler.post(new Runnable() {
			@Override
			public void run() {

				String bufferStr;
				bufferStr = Tools.Bytes2HexString(buffer,size);
				Log.d(TAG, "run: " + bufferStr);
				if (mCmd == (byte) 0xA3) {
					if (bufferStr != null &&
							bufferStr.length() >=8 &&
							bufferStr.substring(0, 8).equals("0116A320"))
						m_uartTemp = "";
					m_uartTemp = m_uartTemp + bufferStr;
					if (m_uartTemp.length() >= 44 && m_uartTemp.substring(0, 8).equals("0116A320")) {//完成接收
						readBlockEditText.setText(m_uartTemp.substring(10, 42));
					}
				} else if (mCmd == (byte)0xA4) {
					if (bufferStr != null &&
							bufferStr.length() >=8 &&
							bufferStr.substring(0, 8).equals("0108A420"))
						m_uartTemp = "";
					m_uartTemp = m_uartTemp + bufferStr;
					if (m_uartTemp.length() >= 16 && m_uartTemp.substring(0, 8).equals("0108A420")) {//完成接收
						if(m_uartTemp.equals("0108A42000000072"))
							Toast.makeText(getApplicationContext(),"写卡成功！",Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(getApplicationContext(),"写卡失败！",Toast.LENGTH_SHORT).show();

						m_uartTemp = "";
					}
				} else if (mCmd == (byte)0xA) {
					Log.d(TAG, "run: " + m_uartTemp);
					if (bufferStr != null &&
							bufferStr.length() >=6 &&
							bufferStr.substring(0, 6).equals("040C02"))
						m_uartTemp = "";
					m_uartTemp = m_uartTemp + bufferStr;
					Log.d(TAG, "run: " + m_uartTemp);
					if (m_uartTemp.length() >= 24) {
						addToList(listEPC, m_uartTemp.substring(14, 22));
						Util.play(1, 0);
					}
				}
			}
		}) ;

	}

	private void initSerialPort()
	{
		Log.e("SerialportActivity", "++++onCreate") ;
		try {
			serialport = "/dev/ttyS1";
			baudrate = 9600 ;
			mSerialPort = new SerialPort(serialport, baudrate);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
			readThreadFlag = true;

			textVersion.setText("HF模块连接成功");
			buttonConnect.setEnabled(false);

			setButtonClickable(btn_set, true);
		} catch (SecurityException e) {
			//DisplayError(R.string.error_security);
		} catch (IOException e) {
			//DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			//DisplayError(R.string.error_configuration);
		}
	}


	public void writeSerial(byte[] bytes){
		if(mOutputStream != null){
			try {
				mOutputStream.write(bytes);
				mOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeSerialPort()
	{
		if (mReadThread != null) {
			mReadThread.interrupt();
			readThreadFlag = false;
		}
		if(mSerialPort != null){
			mSerialPort.close();
			mSerialPort = null;
		}
	}


}
