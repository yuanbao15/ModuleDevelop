package com.cus.maguhf;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.olc.mode.ReadMode;
import com.olc.uhf.UhfAdapter;
import com.olc.uhf.tech.ISO1800_6C;
import com.olc.uhf.tech.IUhfCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 2016.12.21
 * 
 * @author donqiwu
 */
public class ReadWriteActivity extends Activity implements OnClickListener {

	/**/
	public static ReadWriteActivity instance = null;

	private ISO1800_6C uhf_6c;
	// ******************************************************
	Button m_btnDubiaoqian, m_btnXiebiaoqian, m_readepc;
	ImageButton btn_main_back;
	EditText m_editAddress, m_editLength, m_editInput, m_editmima;
	TextView m_result, m_textepc, tx_resultView;
	Spinner m_spinner;
	RadioButton Radio_RFU, Radio_EPC, Radio_TID, Radio_USER;
	// *******************************************************
	public static long lastTime; // 
	// *******************************************************
	byte btMemBank = 0x03;
	String m_strresult = "";
	ArrayAdapter<String> m_adapter;
	private Handler mHandler=new MainHandler();
	// ******************************************************

	private List<ReadMode> readermodes = new ArrayList<ReadMode>();
	// ****************************************************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_readwrite);
		Log.w("onCreate", "--------正在模块初始化");
		initUHFModule();
		Log.w("onCreate", "--------模块初始化结束");
		initview();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}
	// yb-初始化UHF超高频模块
	private void initUHFModule(){
		if(App.mService == null)
		{
			Log.w("onCreate", "--------模块初始化--yes");
			App.mService = UhfAdapter.getUhfManager(this.getApplicationContext());
			if (App.mService != null) {
				App.mService.open();
			}
		}
		uhf_6c = (ISO1800_6C) App.mService.getISO1800_6C();
		DevBeep.init(ReadWriteActivity.this);
	}

	private void initview() {
		Radio_RFU = (RadioButton) findViewById(R.id.Radio_RFU);
		Radio_EPC = (RadioButton) findViewById(R.id.Radio_EPC);
		Radio_TID = (RadioButton) findViewById(R.id.Radio_TID);
		Radio_USER = (RadioButton) findViewById(R.id.Radio_USER);
		Radio_RFU.setOnClickListener(this);
		Radio_EPC.setOnClickListener(this);
		Radio_TID.setOnClickListener(this);
		Radio_USER.setOnClickListener(this);
		btn_main_back = (ImageButton) findViewById(R.id.btn_main_back);
		m_readepc = (Button) findViewById(R.id.btn_readepc);
		m_btnDubiaoqian = (Button) findViewById(R.id.btn_dubiaoqian);
		m_btnXiebiaoqian = (Button) findViewById(R.id.btn_xiebiaoqian);
		m_readepc.setOnClickListener(this);
		m_btnDubiaoqian.setOnClickListener(this);
		m_btnXiebiaoqian.setOnClickListener(this);
		btn_main_back.setOnClickListener(this);
		m_textepc = (TextView) findViewById(R.id.textEPC);
		m_editAddress = (EditText) findViewById(R.id.address);
		m_editLength = (EditText) findViewById(R.id.datalength);
		m_editInput = (EditText) findViewById(R.id.inputdata);
		m_editmima = (EditText) findViewById(R.id.password);
		m_result = (TextView) findViewById(R.id.resultView);
		tx_resultView =(TextView) findViewById(R.id.tx_resultView);
	}
	public static byte[] stringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * readLable：读取标签信息，要求先取到标签
	 * @return
	 */
	public int readLable(){
		m_strresult = "";
		m_result.setText(m_strresult);
		if ("".equals(m_textepc.getText().toString())){
			Toast.makeText(ReadWriteActivity.this,
					"Please select the EPC tags", Toast.LENGTH_SHORT).show();
			return -1;
		}
		String stradd = m_editAddress.getText().toString().trim();
		if (stradd.equals("")) {
			stradd = m_editAddress.getHint().toString();
		}
		int nadd = Integer.valueOf(stradd);

		String strdatalength = m_editLength.getText().toString().trim();
		if (strdatalength.equals("")) {
			strdatalength = m_editLength.getHint().toString();
		}
		int ndatalen = Integer.valueOf(strdatalength);

		String mimaStr = m_editmima.getText().toString().trim();
		if (mimaStr == null || mimaStr.equals("")) {
			m_strresult = "Please enter your 8 - digit password!!\n";
			mimaStr = m_editmima.getHint().toString().trim();
		}
		if (mimaStr.length() != 8) {
			Toast.makeText(ReadWriteActivity.this,
					"Please enter your 8 - digit password!!",
					Toast.LENGTH_SHORT).show();
			return -1;
		}
		byte[] passw = stringToBytes(mimaStr);
		byte[] epc =stringToBytes(m_textepc.getText().toString());
		if (null!=epc) 
		{
			byte []dataout=new byte[ndatalen*2];
			if (btMemBank == 1)
			{
				int result=uhf_6c.read(passw, epc.length, epc, (byte) btMemBank,nadd, ndatalen, dataout, 0, ndatalen);
			    if(result==0)
			    {
			    	m_result.setText(BytesToString(dataout,0,ndatalen*2));
					return 0;
			    }
			    else
			    {
			    	return result;
			    }
			    
			} else {
				int result=uhf_6c.read(passw, epc.length, epc, (byte) btMemBank,nadd, ndatalen, dataout, 0, ndatalen);
				if(result==0)
				{	
					m_result.setText(BytesToString(dataout,0,ndatalen*2));
					return 0;
				}else{
					return result;
				}
			}
		}
		return -1;
	}
	public  String BytesToString(byte[] b, int nS, int ncount) {
		String ret = "";
		int nMax = ncount > (b.length - nS) ? b.length - nS : ncount;
		for (int i = 0; i < nMax; i++) {
			String hex = Integer.toHexString(b[i + nS] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	private int writeLable() {

		m_strresult = "";
		tx_resultView.setText("");
		if ("".equals(m_textepc.getText().toString())) {
			Toast.makeText(ReadWriteActivity.this,
					"Please select the EPC tags", Toast.LENGTH_SHORT).show();
			return -1;
		}
		String stradd = m_editAddress.getText().toString().trim();
		if (stradd.equals("")) {
			stradd = m_editAddress.getHint().toString().trim();
		}
		int nadd = Integer.valueOf(stradd);
		String strdatalength = m_editLength.getText().toString().trim(); // 87784441
		if (strdatalength.equals("")) {
			strdatalength = m_editLength.getHint().toString().trim(); // 87784441
		}
		int ndatalen = Integer.valueOf(strdatalength);

		String mimaStr = m_editmima.getText().toString().trim();
		if (mimaStr == null || mimaStr.equals("")) {
			mimaStr = m_editmima.getHint().toString().trim();
		}
		if (mimaStr.length() != 8) {
			Toast.makeText(ReadWriteActivity.this,
					"Please enter your 8 - digit password!!",
					Toast.LENGTH_SHORT).show();
			return -1;
		}
		byte[] passw =stringToBytes(mimaStr);
		byte[] pwrite = new byte[ndatalen * 2];

		String dataE = m_editInput.getText().toString().trim();
		if (dataE.equals("")) {
			m_strresult = getResources().getString(R.string.Lable_write_null);
			dataE = m_editInput.getHint().toString();
		}
		byte[] myByte =stringToBytes(dataE);
		System.arraycopy(myByte, 0, pwrite, 0,
				myByte.length > ndatalen * 2 ? ndatalen * 2 : myByte.length);
		byte[] epc = stringToBytes(m_textepc.getText().toString());
		int  iswrite=uhf_6c.write(passw, epc.length, epc, btMemBank, (byte)nadd, (byte) ndatalen * 2, pwrite);	
		return iswrite;
	}
	/**
	 * @author dongqiwu
	 * */
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			if(msg.what ==1)
			{
				DevBeep.PlayOK();
				m_textepc.setText(msg.obj.toString());
			}
		}
	}
	IUhfCallback callback = new IUhfCallback.Stub() {
		@Override
		public void doInventory(List<String> str) throws RemoteException {
			Log.d("dqw", "count111=" + str.size());
			for (int i = 0; i < str.size(); i++) {
				String strepc = (String) str.get(i);
				Log.d("wyt", "RSSI=" + strepc.substring(0, 2));
				Log.d("wyt", "PC=" + strepc.substring(2, 6));
				Log.d("wyt", "EPC=" + strepc.substring(2, 6)+strepc.substring(6));
				//DevBeep.PlayOK();
				String strEpc =strepc.substring(2, 6)+strepc.substring(6);
				Message msg = new Message();
				msg.what = 1;
				msg.obj = strEpc;
				mHandler.sendMessage(msg);
			}
		}
		@Override
		public void doTIDAndEPC(List<String> str) throws RemoteException {
			for (Iterator it2 = str.iterator(); it2.hasNext();) {
				String strepc = (String) it2.next();
				// Log.d("wyt", strepc);
				int nlen = Integer.valueOf(strepc.substring(0, 2), 16);
				// Log.d("wyt", "PC=" + strepc.substring(2, 6));
				// Log.d("wyt", "EPC=" + strepc.substring(6, (nlen + 1) * 2));
				// Log.d("wyt", "TID=" + strepc.substring((nlen + 1) * 2));

			}
		}

	};
	public boolean IsDoubClick() {
		boolean flag = false;
		long time = System.currentTimeMillis() - lastTime;
		if (time > 500) {
			flag = true;
		}
		lastTime = System.currentTimeMillis();
		return flag;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_main_back) {
			finish();
		}else if (v.getId() == R.id.Radio_RFU) {
			btMemBank = 0x00;
			m_editAddress.setText("0");
			m_editLength.setText("4");
		}else if (v.getId() == R.id.Radio_EPC) {
			btMemBank = 0x01;
			m_editAddress.setText("2");
		}else if (v.getId() == R.id.Radio_TID) {
			m_editAddress.setText("0");
			btMemBank = 0x02;
		}else if (v.getId() == R.id.Radio_USER) {
			m_editAddress.setText("0");
			btMemBank = 0x03;
		}else if (v.getId() == R.id.btn_readepc) {
			Log.w("selectEPC","--------选择标签EPC");
			if (IsDoubClick()) {
				uhf_6c.inventory(callback);
			}
		}else if (v.getId() == R.id.btn_dubiaoqian) {
			if (IsDoubClick()) {
				int mresult= readLable();
				if(mresult==0){
					DevBeep.PlayOK();
					tx_resultView.setText("OK");
				}else{
					tx_resultView.setText(uhf_6c.getErrorDescription(mresult));
					DevBeep.PlayErr();
				}
			}
		}else if (v.getId() == R.id.btn_xiebiaoqian) {
			if (IsDoubClick()) {
				int mresult= writeLable();
				if(mresult==0){
					tx_resultView.setText("OK");
					DevBeep.PlayOK();
				}else{
					tx_resultView.setText(uhf_6c.getErrorDescription(mresult));
					DevBeep.PlayErr();
				}
			}
		}

	};


	/*--------------------------取MainActivity中初始化的操作--------------------------------*/


}