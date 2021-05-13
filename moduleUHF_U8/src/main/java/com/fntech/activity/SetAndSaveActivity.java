package com.fntech.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.fntech.R;
import com.fntech.entity.Device;
import com.fntech.model.DeviceModel;
import com.fntech.model.DeviceModelImp;

public class SetAndSaveActivity extends Activity implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {

	public static final String _4_0_3 = "4.0.3";
	public static final String _7_1_2 = "7.1.2";
	public static final String _5_1_1 = "5.1.1";
	public static final String _9_0_0 = "9";
	public static final int M96DisplayHeight = 782;
	public static final int M10ADisplayHeight = 640;
	public static final int P01DisplayHeight = 782;
	private Button btn_save;
	private CheckBox cb_enable;
	private Spinner spinnerModel;
	private Spinner spinnerSerialPort;
	private Spinner spinnerBaud;

	private Context mContext;
	private List<String> modelList = new ArrayList<String>();
	private List<String> serialPortList = new ArrayList<String>();
	private List<String> baudList = new ArrayList<String>();
	// 写入list集合
	private List<Device> writeList = new ArrayList<Device>();
	// 读取出的数据信息存储集合
	private List<Device> readList = new ArrayList<Device>();
	// 读取出的数据model存储集合
	// private List<String> readModels = new ArrayList<>();

	// 为方便动态设置radioGroup被选择的RadioButton位置,创建List存储
	private List<RadioGroup> rgList = new ArrayList<>();
	// 新建一个btnList存储radiobutton的状态
	private List<String> btnList = new ArrayList<String>();

	// 声明DeviceModel
	private DeviceModelImp deviceModel;
	// 设置一默认参数Device方便设置调用
	private Device mDefaultDevice = new Device();
	private List<String> mDefaultGPIOList = new ArrayList<>();
	// 存储上下电区域TextView 方便动态添加及获取
	private List<TextView> onTvList = new ArrayList<>();

	private boolean versionCheckSucceed = true;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		deviceModel = new DeviceModel(mContext);
		setContentView(R.layout.activity_setandsave);
		// reload();
		initView();
		if (versionCheckSucceed) {
			setAdapter();

			getModelType();
			// 对Modelspinner及Button实现监听
			setListener();
		}
	}

	@Override
	public void finish() {
		if (versionCheckSucceed)
			jumpToModel();

		super.finish();

	}

	private void jumpToModel() {
		// 判断是完成否已经配置
		Intent in = new Intent();
		Intent jumpIntent = getIntent();
		if (jumpIntent.getStringExtra("packageName") == null || jumpIntent.getStringExtra("activityName") == null)
			return;
		in.setClassName(jumpIntent.getStringExtra("packageName"), jumpIntent.getStringExtra("activityName"));
		startActivity(in);
	}

	/**
	 * 获取intent传进的参数,并将当前页面
	 */
	private void getModelType() {
		String modeName = (String) getIntent().getStringExtra("modelName");
		if (modeName != null) {
			spinnerModel.setSelection(modelList.indexOf(modeName));
			setModelConfig(mDefaultDevice);
		}

	}

	/**
	 * 设置一个默认Device参数方便调用
	 */
	private void setDefaultDevice(int length) {
		mDefaultDevice.setSerialPort(serialPortList.get(0));
		mDefaultDevice.setBaudRate(9600);
		mDefaultDevice.setEnable(false);
		for (int i = 0; i < length; i++) {
			mDefaultGPIOList.add("无");
		}
		mDefaultDevice.setGpios(mDefaultGPIOList);
	}

	/**
	 * 根据形参Device刷新当前界面
	 */
	private void setModelConfig(Device d) {
		// 设置串口spinner
		for (int i = 0; i < serialPortList.size(); i++) {
			if (d.getSerialPort().equals(serialPortList.get(i))) {
				spinnerSerialPort.setSelection(i);
			}
		}
		// 设置波特率spinner
		for (int i = 0; i < baudList.size(); i++) {
			if (d.getBaudRate() == Integer.parseInt(baudList.get(i))) {
				spinnerBaud.setSelection(i);
			}
		}
		// 设置enableCheckBox
		cb_enable.setChecked(d.isEnable());
		// 设置上下电参数
		List<String> gpioList = d.getGpios();
		Log.i("see", "gpioList de size               " + gpioList.size() + "");
		Log.i("see", "rgList de size               " + rgList.size() + "");
		for (int i = 0; i < gpioList.size(); i++) {
			int index = getchildIndex(gpioList.get(i));
			rgList.get(i).check(rgList.get(i).getChildAt(index).getId());
		}
	}

	// 设置adapter
	private void setAdapter() {
		String[] baudsStrings = getResources().getStringArray(R.array.spinarr_baud);
		List<String> baudsList = new ArrayList<>();
		for (int i = 0; i < baudsStrings.length; i++) {
			baudsList.add(baudsStrings[i]);
		}

		ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, modelList);
		spinnerModel.setAdapter(modelAdapter);
		ArrayAdapter<String> baudAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, baudsList);
		spinnerBaud.setAdapter(baudAdapter);
		ArrayAdapter<String> portAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, serialPortList);
		spinnerSerialPort.setAdapter(portAdapter);
	}

	/**
	 * 整个界面设置监听
	 */
	private void setListener() {
		// spinner控件的监听
		spinnerModel.setOnItemSelectedListener(this);
		// saveBtn的监控
		btn_save.setOnClickListener(this);
		// checkBox监听
		cb_enable.setOnCheckedChangeListener(this);
	}

	/**
	 * 顶部spinner model切换监听
	 */
	// 上一次点击的position默认是0
	private int lastPosition = 0;

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position, long arg3) {

		if (versionCheckSucceed) {
			// 判断当前点击事件位置和上一次点击事件位置是否相同 ,不同则更新writeList
			if (position != lastPosition) {
				updataWriteList();
			}
			if (writeList.size() != 0) {
				// 若此次点击的model在读取到的集合中
				Device device = getDeviceFromModel(modelList.get(position));
				if (device != null) {
					setModelConfig(device);
				} else {
					setModelConfig(mDefaultDevice);
				}

			} else {
				// 若此次点击的model不在读取集合中,将各个设置项置为默认状态
				setModelConfig(mDefaultDevice);

			}
			lastPosition = position;
		}
	}

	/**
	 * 根据传进model字符串获取Device
	 *
	 * @return Device
	 */
	public Device getDeviceFromModel(String model) {
		Device device = null;
		for (Device d : writeList) {
			if (d.getModel().equals(model)) {
				device = d;
			}
		}
		return device;
	}

	// 在切换model或点击保存时将页面信息保存到内存中
	private void updataWriteList() {

		// 此处判断 此次要存到内存中的Device是否之前设置过,若设置过则把之前的remove掉

		Iterator<Device> iterator = writeList.iterator();
		while (iterator.hasNext()) {
			Device d = iterator.next();
			if (d.getModel().equals(modelList.get(lastPosition))) {
				iterator.remove(); // 注意这个地方
			}
		}

		Device device = new Device();
		device.setModel(modelList.get(lastPosition));
		device.setEnable(cb_enable.isChecked());
		device.setSerialPort(spinnerSerialPort.getSelectedItem().toString());
		device.setBaudRate(Integer.parseInt(spinnerBaud.getSelectedItem().toString()));

		initRadioBtn();

		for (int i = 0; i < checkedRBs.size(); i++) {
			btnList.add(checkedRBs.get(i).getText().toString());
		}
		device.setGpios(btnList);
		writeList.add(device);
		btnList.clear();
		Log.i("writesize", writeList.size() + "");
	}

	// 获取GPIO状态对应的RadioGroup中的位置
	private int getchildIndex(String state) {
		if (state.equals("1")) {
			return 0;
		} else if (state.equals("0")) {
			return 1;
		} else if (state.equals("无")) {
			return 2;
		}
		return -1;

	}

	/*
	 * private boolean haveData(String model) { for (String m : readModels) { if
	 * (m.equals(model)) { return true; } } return false;
	 *
	 * }
	 */

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.saveBtn && versionCheckSucceed) {
			updataWriteList();
			// 储存writeList中的model字符串
			List<String> bufList = new ArrayList<>();
			for (Device d : writeList) {
				bufList.add(d.getModel());
			}
			if (readList != null) {
				for (Device rd : readList) {
					Log.i("writesize", Boolean.toString(rd == null));
					if (!bufList.contains(rd.getModel())) {
						writeList.add(rd);
					}
				}

			}
			Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
			deviceModel.writeXML(writeList, rgList.size() / 2);
		}
		/*
		 * else if (id == R.id.readBtn) { // 点击读取后更新获得的List
		 *
		 * readList.clear(); List<Device> reloadList =
		 * deviceModel.readXML(rgList.size()/2); if (reloadList != null) {
		 * readList.addAll(reloadList); // 设置当前显示的model对应数据
		 *
		 * setModelConfig(deviceModel
		 * .reLoadDevice(modelList.get(lastPosition))); }else {
		 * Toast.makeText(mContext, "未找到保存的数据",Toast.LENGTH_SHORT).show(); } }
		 */
	}

	/**
	 * 动态加载GPIO区域控件
	 *
	 * @param release
	 */
	private void updataGPIOUI(String release) {
		String[] valueStrings = new String[] { "1", "0", "无" };
		if (release.equals("m10")) {

			String[] m10_GPIO = getResources().getStringArray(R.array.m10_GPIO);
			RelativeLayout onRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutON_main);
			addViewTo(m10_GPIO, valueStrings, onRelativeLayout, R.id.poweron);
			RelativeLayout offRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutOFF_main);
			addViewTo(m10_GPIO, valueStrings, offRelativeLayout, R.id.poweroff);

		} else if (release.equals("m10A")) {
			String[] m10A_GPIO = getResources().getStringArray(R.array.m10A_GPIO);
			RelativeLayout onRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutON_main);
			addViewTo(m10A_GPIO, valueStrings, onRelativeLayout, R.id.poweron);
			RelativeLayout offRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutOFF_main);
			addViewTo(m10A_GPIO, valueStrings, offRelativeLayout, R.id.poweroff);
		} else if (release.equals("m96")) {
			String[] m96_GPIO = getResources().getStringArray(R.array.m96_GPIO);
			RelativeLayout onRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutON_main);
			addViewTo(m96_GPIO, valueStrings, onRelativeLayout, R.id.poweron);
			RelativeLayout offRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutOFF_main);
			addViewTo(m96_GPIO, valueStrings, offRelativeLayout, R.id.poweroff);
		}
	}

	@SuppressWarnings("deprecation")
	private void addViewTo(String[] m10_GPIO, String[] valueStrings, RelativeLayout relativeLayout, int startViewId) {
		TextView textView1 = new TextView(mContext);
		textView1.setId(0x11);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		params.addRule(RelativeLayout.RIGHT_OF, startViewId);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.setMargins(23, 0, 0, 0);

		textView1.setLayoutParams(params);
		textView1.setPadding(0, 15, 0, 20);
		textView1.setText(m10_GPIO[0]);
		textView1.setTextSize(13);
		textView1.setTextColor(0xFF000000);

		relativeLayout.addView(textView1);

		/*********************** RadioButton ******************************/
		RadioGroup radioGroup1 = new RadioGroup(mContext);
		radioGroup1.setId(0x12);
		RelativeLayout.LayoutParams rgLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		rgLayoutParams.addRule(RelativeLayout.ALIGN_TOP, textView1.getId());
		// rgLayoutParams.addRule(RelativeLayout.RIGHT_OF, tv.getId());
		// rgLayoutParams.addRule(RelativeLayout.RIGHT_OF, textView1.getId());
		rgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rgLayoutParams.setMargins(0, 0, 50, 0);
		radioGroup1.setLayoutParams(rgLayoutParams);
		radioGroup1.setOrientation(RadioGroup.HORIZONTAL);

		for (int a = 0; a < valueStrings.length; a++) {
			RadioButton rbButton = new RadioButton(mContext);
			RadioGroup.LayoutParams rbParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
			rbButton.setLayoutParams(rbParams);
			rbButton.setText(valueStrings[a]);
			rbButton.setTextSize(12);
			rbButton.setTextColor(0xFF000000);
			rbButton.setId(1000 + a);
			Bitmap b = null;
			rbButton.setButtonDrawable(new BitmapDrawable(b));
			Drawable drawable = getResources().getDrawable(R.drawable.abc_radio);
			drawable.setBounds(0, 0, 50, 50);
			rbButton.setCompoundDrawables(drawable, null, null, null);
			rbButton.setPadding(5, 0, 0, 0);
			radioGroup1.addView(rbButton);
			if (a == 2) {
				radioGroup1.check(rbButton.getId());
			}
		}

		// rGroup.getChildAt(2).setSelected(true);
		relativeLayout.addView(radioGroup1);
		onTvList.add(textView1);
		rgList.add(radioGroup1);
		// 新建第一行有错

		for (int i = 1; i < m10_GPIO.length; i++) {
			// 设置
			TextView textView = new TextView(mContext);
			textView.setId(i);
			RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

			// tvParams.setMargins(15, 0, 0, 0); textView1
			tvParams.addRule(RelativeLayout.ALIGN_LEFT, textView1.getId());
			// Log.i("see", onTvList.size() + "");
			// Log.i("see", "i-1" + "  " + (i - 1) + "     " + (onTvList.get(i -
			// 1).getId()));

			tvParams.addRule(RelativeLayout.BELOW, onTvList.get(i - 1).getId());
			textView.setLayoutParams(tvParams);
			textView.setPadding(0, 10, 0, 20);
			textView.setText(m10_GPIO[i]);
			textView.setTextSize(13);
			textView.setTextColor(0xFF000000);

			relativeLayout.addView(textView);

			// RadioGroup
			RadioGroup rGroup = new RadioGroup(mContext);
			rGroup.setId(100 + i);
			RelativeLayout.LayoutParams rgParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

			rgParams.addRule(RelativeLayout.ALIGN_TOP, textView.getId());
			// rgLayoutParams.addRule(RelativeLayout.RIGHT_OF, tv.getId());
			rgParams.addRule(RelativeLayout.ALIGN_LEFT, radioGroup1.getId());
			rGroup.setLayoutParams(rgParams);
			rGroup.setOrientation(RadioGroup.HORIZONTAL);
			for (int a = 0; a < valueStrings.length; a++) {
				RadioButton rbButton = new RadioButton(mContext);
				RadioGroup.LayoutParams rbParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
				rbButton.setLayoutParams(rbParams);
				rbButton.setText(valueStrings[a]);
				rbButton.setTextSize(12);
				rbButton.setTextColor(0xFF000000);
				rbButton.setId(1000 + a);
				Bitmap b = null;
				rbButton.setButtonDrawable(new BitmapDrawable(b));
				Drawable drawable = getResources().getDrawable(R.drawable.abc_radio);
				drawable.setBounds(0, 0, 50, 50);
				rbButton.setCompoundDrawables(drawable, null, null, null);
				rbButton.setPadding(5, 0, 0, 0);
				rGroup.addView(rbButton);
				if (a == 2) {
					rGroup.check(rbButton.getId());
				}
			}

			// rGroup.getChildAt(2).setSelected(true);
			relativeLayout.addView(rGroup);
			onTvList.add(textView);
			rgList.add(rGroup);

		}
	}

	/**
	 * 实例化页面上方xml添加的控件
	 */
	@SuppressWarnings("deprecation")
	private void initView() {
		btn_save = (Button) findViewById(R.id.saveBtn);
		// readBtn = (Button) findViewById(R.id.readBtn);
		cb_enable = (CheckBox) findViewById(R.id.enableCheckBox);
		Bitmap b = null;
		cb_enable.setButtonDrawable(new BitmapDrawable(b));
		Drawable drawable = getResources().getDrawable(R.drawable.abc_checkbox);
		drawable.setBounds(0, 0, 60, 60);
		cb_enable.setCompoundDrawables(drawable, null, null, null);
		cb_enable.setPadding(5, 0, 0, 0);

		spinnerSerialPort = (Spinner) findViewById(R.id.serialport_spinner);
		spinnerBaud = (Spinner) findViewById(R.id.baudRate_spinner);
		spinnerModel = (Spinner) findViewById(R.id.model_spinner);

		String SystemRelease = android.os.Build.VERSION.RELEASE;
		if (SystemRelease.equals(_4_0_3)) {
			// 根据不同机型 重新设置界面显示
			updataGPIOUI("m10");
			// 设置串口spinner中数据
			String[] m10SerialPort = getResources().getStringArray(R.array.m10SerialPort);
			for (int i = 0; i < m10SerialPort.length; i++) {
				serialPortList.add(m10SerialPort[i]);
			}
			setDefaultDevice(rgList.size());

			String[] modelsStrings = getResources().getStringArray(R.array.m10_models);
			for (int i = 0; i < modelsStrings.length; i++) {
				modelList.add(modelsStrings[i]);
			}
		} else if (SystemRelease.equals(_5_1_1)) {
			if (getWindowManager().getDefaultDisplay().getHeight() == M10ADisplayHeight) {
				// 根据不同机型 重新设置界面显示
				updataGPIOUI("m10A");
				String[] m10ASerialPort = getResources().getStringArray(R.array.m10ASerialPort);
				for (int i = 0; i < m10ASerialPort.length; i++) {
					serialPortList.add(m10ASerialPort[i]);
				}
				setDefaultDevice(rgList.size());

				String[] modelsStrings = getResources().getStringArray(R.array.m10_models);
				for (int i = 0; i < modelsStrings.length; i++) {
					modelList.add(modelsStrings[i]);
				}
			}
		}else if (SystemRelease.equals(_7_1_2)) {
			if (getWindowManager().getDefaultDisplay().getHeight() == P01DisplayHeight) {
				// 根据不同机型 重新设置界面显示
				updataGPIOUI("P01");
				String[] m10ASerialPort = getResources().getStringArray(R.array.P01SerialPort);
				for (int i = 0; i < m10ASerialPort.length; i++) {
					serialPortList.add(m10ASerialPort[i]);
				}
				setDefaultDevice(rgList.size());

				String[] modelsStrings = getResources().getStringArray(R.array.m10_models);
				for (int i = 0; i < modelsStrings.length; i++) {
					modelList.add(modelsStrings[i]);
				}
			}
		}
		else if (SystemRelease.equals(_9_0_0)) {
			if (getWindowManager().getDefaultDisplay().getHeight() == P01DisplayHeight) {
				// 根据不同机型 重新设置界面显示
				updataGPIOUI("P01");
				String[] m10ASerialPort = getResources().getStringArray(R.array.P01SerialPort);
				for (int i = 0; i < m10ASerialPort.length; i++) {
					serialPortList.add(m10ASerialPort[i]);
				}
				setDefaultDevice(rgList.size());

				String[] modelsStrings = getResources().getStringArray(R.array.m10_models);
				for (int i = 0; i < modelsStrings.length; i++) {
					modelList.add(modelsStrings[i]);
				}
			} else {
				// 根据不同机型 重新设置界面显示
				updataGPIOUI("m96");
				String[] m10ASerialPort = getResources().getStringArray(R.array.m96SerialPort);
				for (int i = 0; i < m10ASerialPort.length; i++) {
					serialPortList.add(m10ASerialPort[i]);
				}
				setDefaultDevice(rgList.size());

				String[] modelsStrings = getResources().getStringArray(R.array.m96_models);
				for (int i = 0; i < modelsStrings.length; i++) {
					modelList.add(modelsStrings[i]);
				}
			}
		}
			 else {
				versionCheckSucceed = false;
				Toast.makeText(mContext, "此设备系统版本异常 请与技术支持人员联系", Toast.LENGTH_LONG).show();
			}

	}
	private List<RadioButton> checkedRBs = new ArrayList<>();

	/**
	 * 每次调用获得点击到的radiobutton
	 */
	public void initRadioBtn() {
		checkedRBs.clear();
		for (int i = 0; i < rgList.size(); i++) {
			RadioButton checkedRb = (RadioButton) findViewById(rgList.get(i).getCheckedRadioButtonId());
			checkedRBs.add(checkedRb);
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub

	}
}
