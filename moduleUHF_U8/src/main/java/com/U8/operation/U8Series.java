package com.U8.operation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.U8.AndroidVersions;
import com.U8.Device;
import com.U8.DeviceModel;
import com.U8.Loger;
import com.U8.UHFApplication;
import com.U8.model.IResponseHandler;
import com.U8.model.Message;
import com.U8.reader.CMD;
import com.U8.reader.ERROR;
import com.U8.reader.model.InventoryBuffer;
import com.U8.reader.model.OperateTagBuffer;
import com.U8.reader.model.ReaderSetting;
import com.U8.reader.server.ReaderBase;
import com.U8.reader.server.ReaderHelper;
import com.U8.utils.Tools;
import com.fntech.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


public class U8Series implements IUSeries {
	private static final String TAG = "USeries";
	private Tools tools = new Tools();
	private static Context mContext;
	public static final Object object = new Object();// 设置参数锁
	public static final Object readObject = new Object();// read Tag线程锁对象
	/**
	 * notify标识,防止出现接收线程已经收到串口返回并notify,而发送线程并未开始sleep造成异常;
	 * 此标志位在每次使用前必须恢复初始值false
	 */
	public static boolean havenotify;

	private final int SUCCEED = 0;
	private final int FAILED = 1;

	public static final String REFRESHTEXT = "REFRESHTEXT";
	public static final String REFRESHLIST = "REFRESHLIST";
	/**
	 * 射频功率
	 */
	public static final String PARA_POWER = "PARA_POWER";
	/**
	 * 软件提示音模式
	 */
	public static final String BEER_STATE = "BEER_STATE";
	/**
	 * 软件提示音开关 0-打开;1-关闭
	 */
	public static final String SOFT_SOUND = "SOFT_SOUND";
	/**
	 * 模块温度
	 */
	public static final String TEMPERATURE = "TEMPERATURE";
	/**
	 * session
	 */
	public static final String SESSIONSTATE = "SESSIONSTATE";
	/**
	 * flag state
	 */
	public static final String FLAGSTATE = "FLAGSTATE";

	private SerialPort mSerialPort = null;
	// 以下三个字符串供上下电配置使用,不可更改
	private String model = "U8";
	private String packageName = "com.fn.useries";
	private String activityName = "com.fn.useries.activity.MainActivity";

	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;
	private ReaderSetting m_curReaderSetting;
	public InventoryBuffer m_curInventoryBuffer;
	private OperateTagBuffer m_curOperateTagBuffer;

	private static U8Series mUSeries;
	private List<InventoryBuffer.InventoryTagMap> mTagMaps;
	/**

	 */
	public String readDataString = "";
	/**
	 * 写/锁/杀标签结果标志位,成功:0,失败:1; 注意操作前复位为1
	 */
	public static int operationTagResult;
	/**
	 * 错误信息,当解析到错误代码时被赋值
	 */
	public static String errorData = "";
	/**
	 *
	 */
	public static String temperature;

	private IResponseHandler mResponseHandler;

	private boolean enableSaveDataWhenGoOnInventory = true;// 重新盘存保存上一次数据
	private boolean firstInventoryFlag = true;

	private int timeout = 3 * 1000;

	/**
	 * 锁标签操作类型
	 *
	 */
	public static enum lockOperation {
		LOCK_FREE, LOCK_FREE_EVER, LOCK_LOCK, LOCK_LOCK_EVER
	}

	private U8Series() {

	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static U8Series getInstance() {
		if (mUSeries == null) {
			mUSeries = new U8Series();
			registerReceiver();
			return mUSeries;
		}
		return mUSeries;
	}


	/**
	 * 开始盘询
	 * @param responseHandler 盘询结果回调
	 * @return
	 */
	@Override
	public boolean startInventory(IResponseHandler responseHandler) {
		this.mResponseHandler = responseHandler;
		try {
			initInventoryParam(); //初始化，开启数据接收线程
		} catch (Exception e) {
			Loger.disk_log("Exception", "initInventoryParamException,info = " + e.toString(), "M10_U8");
			return false;
		}
		m_curInventoryBuffer.clearInventoryPar();
		m_curInventoryBuffer.bLoopCustomizedSession = true;
//		m_curInventoryBuffer.btSession = (byte) (UHFApplication.getSessionState() & 0xFF);
//		m_curInventoryBuffer.btTarget = (byte) (UHFApplication.getFlagState() & 0xFF);
		m_curInventoryBuffer.btSession = (byte) (0 & 0xFF);
		m_curInventoryBuffer.btTarget = (byte) (0 & 0xFF);
		m_curInventoryBuffer.lAntenna.add((byte) 0x01);
		m_curInventoryBuffer.bLoopInventoryReal = true;
		m_curInventoryBuffer.btRepeat = (byte) 1;

		if (enableSaveDataWhenGoOnInventory) {
			if (firstInventoryFlag)// add by lyz
				m_curInventoryBuffer.clearInventoryRealResult();
		} else {
			m_curInventoryBuffer.clearInventoryRealResult();
		}
		mReaderHelper.setInventoryTotal(1);
		mReaderHelper.setInventoryFlag(true);
		if (enableSaveDataWhenGoOnInventory) {
			if (firstInventoryFlag)// add by lyz
				mReaderHelper.clearInventoryTotal();
		} else {
			mReaderHelper.clearInventoryTotal();
		}

		byte btWorkAntenna = m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer.nIndexAntenna);
		if (btWorkAntenna < 0)
			btWorkAntenna = 0;
		mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);//设置工作天线
		//mReader.getFirmwareVersion(m_curReaderSetting.btReadId);
		return true;
	}

	/**
	 * 停止盘询
	 * @return
	 */
	@Override
	public boolean stopInventory() {
		try {
			mReaderHelper.setInventoryFlag(false);
			m_curInventoryBuffer.bLoopInventory = false;
			m_curInventoryBuffer.bLoopInventoryReal = false;
//			if (enableSaveDataWhenGoOnInventory)
//				mReader.resetRecevice();
			Log.i("toolsdebug", " stopInventory()  ");
			Loger.disk_log("stopInventory", " stopInventory ", "U8");
		} catch (Exception e) {
			Loger.disk_log("Exception", "stopInventoryException" + getExceptionAllinformation(e), "M10_U8");
			return false;
		}
		return true;
	}

	/**
	 * 读标签
	 * @param EPC
	 * @param block
	 *            读取区域
	 * @param w_count
	 *            读取长度
	 * @param w_offset
	 *            偏移
	 * @param acs_pwd
	 *            访问密码
	 * @return
	 */
	@Override
	public Message readTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] acs_pwd) {
		Message message = new Message();
		if(setEpcMatch(EPC)==FAILED){
			message.setCode(FAILED);
			return message;
		}
		flagReset();
		m_curOperateTagBuffer.clearBuffer();
		mReader.readTag(m_curReaderSetting.btReadId, block, w_offset, w_count, acs_pwd);
		// 发送指令后线程挂起 等待数据返回
		synchronized (readObject) {
			try {
				readObject.wait(timeout);
			} catch (InterruptedException e) {
				readObject.notify();
			}
		}
		// 未读到标签数据
		if (readDataString == null) {
			mReader.resetRecevice();
			message.setCode(FAILED);
			message.setMessage(TextUtils.isEmpty(errorData) ? receviceIncompleteError() : errorData);
			return message;
		}
		// notify后返回数据
		message.setCode(SUCCEED);
		message.setResult(readDataString);
		return message;
	}

	/**
	 * 写标签
	 * @param EPC
	 * @param block
	 *            写入区域
	 * @param w_count
	 *            写入长度
	 * @param w_offset
	 *            偏移
	 * @param data
	 *            写入数据
	 * @param acs_pwd
	 *            访问密码
	 * @return
	 */
	@Override
	public Message writeTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] data, byte[] acs_pwd) {
		Message message = new Message();
		if(setEpcMatch(EPC)==FAILED){
			message.setCode(FAILED);
			return message;
		}
		flagReset();
		m_curOperateTagBuffer.clearBuffer();
		mReader.writeTag(m_curReaderSetting.btReadId, acs_pwd, block, w_offset, w_count, data);
		// 发送指令后线程挂起 等待数据返回
		synchronized (mReaderHelper) {
			try {
				mReaderHelper.wait(timeout);
			} catch (InterruptedException e) {
				Loger.disk_log("Exception", "writeTagMemoryWaitException" + getExceptionAllinformation(e), "M10_U8");
				mReaderHelper.notify();
			}
		}

		message.setCode(operationTagResult);
		message.setMessage(TextUtils.isEmpty(errorData) ? receviceIncompleteError() : errorData);
		return message;
	}

	/**
	 * 锁标签
	 * @param EPC
	 * @param block
	 *            锁定区域
	 * @param operation
	 * @param acs_pwd
	 *            访问密码
	 * @return
	 */
	@Override
	public Message lockTagMemory(byte[] EPC, byte block, Enum operation, byte[] acs_pwd) {
		Message message = new Message();
		if(setEpcMatch(EPC)==FAILED){
			message.setCode(FAILED);
			return message;
		}
		flagReset();
		m_curOperateTagBuffer.clearBuffer();
		byte operat = 0x04;
		if (operation.name().equals(lockOperation.LOCK_FREE.name())) {
			operat = 0x00;
		} else if (operation.name().equals(lockOperation.LOCK_FREE_EVER.name())) {
			operat = 0x02;
		} else if (operation.name().equals(lockOperation.LOCK_LOCK.name())) {
			operat = 0x01;
		} else if (operation.name().equals(lockOperation.LOCK_LOCK_EVER.name())) {
			operat = 0x03;
		}
		mReader.lockTag(m_curReaderSetting.btReadId, acs_pwd, block, operat);

		// 发送指令后线程挂起 等待数据返回
		synchronized (mReaderHelper) {
			try {
				mReaderHelper.wait(timeout);
			} catch (InterruptedException e) {
				Loger.disk_log("Exception", "lockTagMemoryException" + getExceptionAllinformation(e), "M10_U8");
				mReaderHelper.notify();
			}
		}
		message.setCode(operationTagResult);
		message.setMessage(TextUtils.isEmpty(errorData) ? receviceIncompleteError() : errorData);
		return message;

	}

	/**
	 * 销毁标签
	 * @param EPC
	 * @param kill_pwd
	 *            销毁密码
	 * @return
	 */
	@Override
	public Message killTag(byte[] EPC, byte[] kill_pwd) {
		Message message = new Message();
		if(setEpcMatch(EPC)==FAILED){
			message.setCode(FAILED);
			return message;
		}
		flagReset();
		m_curOperateTagBuffer.clearBuffer();
		mReader.killTag(m_curReaderSetting.btReadId, kill_pwd);

		// 发送指令后线程挂起 等待数据返回
		synchronized (mReaderHelper) {
			try {
				mReaderHelper.wait(timeout);
			} catch (InterruptedException e) {
				Loger.disk_log("Exception", "writeTagMemoryException" + getExceptionAllinformation(e), "M10_U8");
				mReaderHelper.notify();
			}
		}
		message.setCode(operationTagResult);
		message.setMessage(TextUtils.isEmpty(errorData) ? receviceIncompleteError() : errorData);
		return message;

	}

	/**
	 * 设置
	 * @param paraName
	 *            参数名(详见SDK)
	 * @param paraValue
	 *            参数值(详见SDK)
	 * @return
	 */
	@Override
	public boolean setParams(String paraName, String paraValue) {
		flagReset();
		byte paramsValue = 0;
		try {
			paramsValue = Byte.parseByte(paraValue);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Loger.disk_log("Exception", "setParamsException" + getExceptionAllinformation(e), "M10_U8");
			return false;
		}
		if (paraName.equals(PARA_POWER)) {
			m_curReaderSetting.btAryOutputPower = new byte[] { paramsValue };
			mReader.setOutputPower(m_curReaderSetting.btReadId, paramsValue);
			synchronized (object) {
				try {
					if (!havenotify)
						object.wait(timeout);
				} catch (InterruptedException e) {
					object.notify();
				}
			}
			return m_curReaderSetting.blnSetResult;
		} else if (paraName.equals(BEER_STATE)) {
			mReader.setBeeperMode(m_curReaderSetting.btReadId, paramsValue);
			synchronized (object) {
				try {
					if (!havenotify)
						object.wait(timeout);
				} catch (InterruptedException e) {
					object.notify();
				}
			}
			if (m_curReaderSetting.blnSetResult) {
				m_curReaderSetting.btBeeperMode = paramsValue;
				UHFApplication.saveBeeperState((paramsValue & 0xFF));
			}
			return m_curReaderSetting.blnSetResult;
		} else if (paraName.equals(SOFT_SOUND)) {
			UHFApplication.saveSoftSound(paramsValue);
			return true;
		} else if (paraName.equals(TEMPERATURE)) {
			// 温度只能获取无法设置
		} else if (paraName.equals(SESSIONSTATE)) {
			try {
				UHFApplication.saveSessionState(paramsValue);
			} catch (Exception e) {
				e.printStackTrace();
				Loger.disk_log("Exception", "set_SESSIONSTATE_Exception:" + e.toString(), "M10_U8");
				return false;
			}
			return true;
		} else if (paraName.equals(FLAGSTATE)) {
			try {
				UHFApplication.saveFlagState(paramsValue);
			} catch (Exception e) {
				e.printStackTrace();
				Loger.disk_log("Exception", "set_FLAGSTATE_Exception:" + e.toString(), "M10_U8");
				return false;
			}
			return true;

		}
		return false;
	}

	/**
	 * 获取参数
	 * @param paraName
	 *            参数名(详见SDK)
	 * @return
	 */
	@Override
	public String getParams(String paraName) {
		flagReset();
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			e.printStackTrace();
		}

		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		if (paraName.equals(PARA_POWER)) {

			// 获取
			mReader.getOutputPower(m_curReaderSetting.btReadId);
			// 发送指令后线程挂起 等待数据返回
			synchronized (object) {
				try {
					if (!havenotify)
						object.wait(timeout);
				} catch (InterruptedException e) {
					object.notify();
				}
			}
			if (m_curReaderSetting.btAryOutputPower != null) {
				int powerValue = m_curReaderSetting.btAryOutputPower[0] & 0xFF;
				return Integer.toString(powerValue);
			} else {
				return receviceIncompleteError();
			}

		} else if (paraName.equals(BEER_STATE)) {
			m_curReaderSetting.btBeeperMode = (byte) UHFApplication.getVeeperState();
			if (m_curReaderSetting.btBeeperMode == 0) {
				return "0";
			} else if (m_curReaderSetting.btBeeperMode == 1) {
				return "1";
			} else if (m_curReaderSetting.btBeeperMode == 2) {
				return "2";
			}
		} else if (paraName.equals(TEMPERATURE)) {
			// 这里暂时将符号位作为是否正确返回标志,协议中规定符号位只能为0x00,0x01,这里赋值为0x02
			m_curReaderSetting.btPlusMinus = 0x02;
			int result = mReader.getReaderTemperature(m_curReaderSetting.btReadId);
			if (result == 0) {
				synchronized (object) {
					try {
						if (!havenotify)
							object.wait(timeout);
					} catch (InterruptedException e) {
						object.notify();
					}
				}
			}
			String strTemperature = "";
			if (m_curReaderSetting.btPlusMinus == 0x00) {
				strTemperature = "-" + String.valueOf(m_curReaderSetting.btTemperature & 0xFF) + "℃";
			} else if (m_curReaderSetting.btPlusMinus == 0x01) {
				strTemperature = String.valueOf(m_curReaderSetting.btTemperature & 0xFF) + "℃";
			} else {// 接收异常
				return receviceIncompleteError();
			}
			// 标志位复位
			return strTemperature;
		} else if (paraName.equals(SESSIONSTATE)) {
			return UHFApplication.getSessionState() + "";
		} else if (paraName.equals(FLAGSTATE)) {
			return UHFApplication.getFlagState() + "";
		}
		return null;
	}

	/********************************************************/
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {

			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				switch (btCmd) {
				case CMD.REAL_TIME_INVENTORY:
				case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:
					try {
						mResponseHandler.onSuccess(REFRESHTEXT, null, null);
					} catch (Exception e) {
						System.out.println(e.toString());
					}
					break;
				case ReaderHelper.INVENTORY_END:
					mTagMaps = m_curInventoryBuffer.lsTagList;
					//mTagMaps = tools.bubbleSort(mTagMaps);
					mResponseHandler.onSuccess(REFRESHLIST, mTagMaps, null);
					break;
				}

			} else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {

			}
			// 读写锁杀页面
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				byte type = intent.getByteExtra("type", (byte) 0x00);
				final String msg = intent.getStringExtra("msg");

				switch (btCmd) {
				case CMD.GET_ACCESS_EPC_MATCH:
					break;
				case CMD.READ_TAG:
					break;
				case CMD.WRITE_TAG:
					break;
				case CMD.LOCK_TAG:
					break;
				case CMD.KILL_TAG:
					break;
				}
			}
			// 设置功率
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				if (btCmd == CMD.GET_OUTPUT_POWER || btCmd == CMD.SET_OUTPUT_POWER) {
				}
				// 温度
				if (btCmd == CMD.GET_READER_TEMPERATURE) {

				}
			}

		}
	};

	@Override
	public Message Inventory() {
		startInventory(new IResponseHandler() {

			@Override
			public void onSuccess(String msg, Object data, byte[] parameters) {
				if (msg.equalsIgnoreCase(REFRESHLIST)) {
					List<InventoryBuffer.InventoryTagMap> InventoryOnceResult = (List<InventoryBuffer.InventoryTagMap>) data;
					stopInventory();
					synchronized (object) {
						object.notifyAll();
					}
				}
			}

			@Override
			public void onFailure(String msg) {
				stopInventory();
				synchronized (object) {
					object.notifyAll();
				}
			}
		});
		synchronized (object) {
			try {
				object.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 打开串口
	 * @param moduleName 模块名
	 * @return
	 */
	@Override
	public Message openSerialPort(String moduleName) {
		DeviceModel deviceModel = new DeviceModel(mContext);
		Device device = deviceModel.getDeviceFromModel(moduleName);
		Message msg = new Message();
		if (device == null) {
			msg.setCode(1);
			return msg;
		}
		String serialPortPath = device.getSerialPort();
		int baudRate = device.getBaudRate();
		try {

			if (android.os.Build.VERSION.RELEASE.equals(AndroidVersions.V_4_0_3)) {
				try {
					mSerialPort = new SerialPort(new File(serialPortPath), baudRate, 8,1,0);
				} catch (SecurityException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				} catch (IOException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				}
			} else if (android.os.Build.VERSION.RELEASE.equals(AndroidVersions.V_5_1_1)) {
				try {
					mSerialPort = new SerialPort(new File(serialPortPath), baudRate, 8,1,0);
				} catch (SecurityException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				} catch (IOException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				}
			} else if (android.os.Build.VERSION.RELEASE.equals(AndroidVersions.V9)) {
				try {
					mSerialPort = new SerialPort(new File(serialPortPath), baudRate, 8,1,0);
				} catch (SecurityException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				} catch (IOException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				}
			} else if (android.os.Build.VERSION.RELEASE.equals(AndroidVersions.V_7_1_2)) {
				try {
					mSerialPort = new SerialPort(new File(serialPortPath), baudRate, 8,1,0);
				} catch (SecurityException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				} catch (IOException e1) {
					e1.printStackTrace();
					msg.setCode(1);
					return msg;
				}
			}else {
				Toast.makeText(mContext, "程序版本有误，请联系技术支持人员！", Toast.LENGTH_SHORT).show();
				msg.setCode(1);
				return msg;
			}

			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReaderHelper.setReader(mSerialPort.getInputStream(), mSerialPort.getOutputStream());
			mReader = mReaderHelper.getReader();
			m_curReaderSetting = mReaderHelper.getCurReaderSetting();
			m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		} catch (Exception e) {
			e.printStackTrace();
			msg.setCode(1);
			return msg;
		}
		msg.setCode(0);
		return msg;
	}

	/**
	 * 关闭串口
	 * @return
	 */
	@Override
	public Message closeSerialPort() {
		Message msg = new Message();
		if (mSerialPort != null) {
			try {
				mSerialPort.CloseSerialPort();
				msg.setCode(0);
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				msg.setCode(1);
				return msg;
			}

		}
		msg.setCode(1);
		return msg;
	}

	/**
	 * 模块上电
	 * @param moduleName 模块名
	 * @return
	 */
	@Override
	public Message modulePowerOn(String moduleName) {
		Message msg = new Message();
		DeviceModel deviceModel = new DeviceModel(mContext);
		Device device = deviceModel.getDeviceFromModel(moduleName);
		if (device != null) {
			try {
				device.powerOn();
				msg.setCode(0);
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				msg.setCode(1);
				return msg;
			}
		} else {
		}
		msg.setCode(1);
		return msg;
	}

	/**
	 * 模块下电
	 * @param moduleName 模块名
	 * @return
	 */
	@Override
	public Message modulePowerOff(String moduleName) {
		Message msg = new Message();
		DeviceModel deviceModel = new DeviceModel(mContext);
		Device device = deviceModel.getDeviceFromModel(moduleName);
		if (device != null) {
			try {
				device.powerOff();
				msg.setCode(0);
				return msg;
			} catch (Exception e) {
				e.printStackTrace();
				msg.setCode(1);
				return msg;
			}

		}
		msg.setCode(1);
		return msg;
	}

	/**
	 * 标志位复位,包括:</br> havenotify :notify标志位</br> errorData :错误信息标志位</br>
	 * readDataString :读标签数据标志位</br> operationTagResult :写/锁/杀标签结果标志位</br>
	 * m_curReaderSetting.blnSetResult :设置结果标志位</br>
	 */
	private void flagReset() {
		havenotify = false;
		errorData = null;
		readDataString = null;
		operationTagResult = FAILED;
		m_curReaderSetting.blnSetResult = false;
		m_curReaderSetting.btAryOutputPower = null;
	}

	/**
	 * 设置匹配标签
	 *
	 * @param EPC
	 *            要绑定的EPC号(需要完整EPC,只绑定部分EPC时读写操作会返回无可操作标签错误),
	 *            需要解绑标签时此方法传进"Cancel".getBytes()即可
	 *
	 * @return 绑定/解绑结果:成功 :0, 失败:-1
	 */
	private int setEpcMatch(byte[] EPC) {
		flagReset();
		if (new String(EPC).equalsIgnoreCase("Cancel")) {
			 mReader.cancelAccessEpcMatch(m_curReaderSetting.btReadId);
			// 发送指令后线程挂起 等待数据返回
				synchronized (object) {
					try {
						object.wait(timeout);
					} catch (InterruptedException e) {
						Loger.disk_log("Exception", "setEpcMatchWaitException" + getExceptionAllinformation(e), "M10_U8");
						mReaderHelper.notify();
					}
				}
				Log.e("see", "m_curReaderSetting.blnSetResult ==>"+m_curReaderSetting.blnSetResult);
				if(m_curReaderSetting.blnSetResult)
					return SUCCEED;
				else {
					return FAILED;
				}
		} else {
			byte[] btAryEpc = EPC;
			 mReader.setAccessEpcMatch(m_curReaderSetting.btReadId, (byte) (btAryEpc.length & 0xFF), btAryEpc);
			// 发送指令后线程挂起 等待数据返回
				synchronized (object) {
					try {
						object.wait(timeout);
					} catch (InterruptedException e) {
						Loger.disk_log("Exception", "setEpcMatchWaitException" + getExceptionAllinformation(e), "M10_U8");
						mReaderHelper.notify();
					}
				}
				if(m_curReaderSetting.blnSetResult)
					return SUCCEED;
				else {
					return FAILED;
				}
		}
	}

	/**
	 * 初始化盘询所需资源
	 *
	 * @throws Exception
	 */
	private void initInventoryParam() throws Exception {

		mReaderHelper = ReaderHelper.getDefaultHelper();
		mReader = mReaderHelper.getReader();

		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();

		if (mReader != null) {
			if (!mReader.IsAlive())
				mReader.StartWait();
		}
	}


	/**
	 * 注册广播接收器
	 */
	private static void registerReceiver() {

		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);

		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
		lbm.registerReceiver(mUSeries.mRecv, itent);
	}

	/**
	 * 获取全部异常信息.
	 *
	 * @param ex
	 *            异常
	 * @return
	 */
	private static String getExceptionAllinformation(Exception ex) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream(out);
		ex.printStackTrace(pout);
		String ret = new String(out.toByteArray());
		pout.close();
		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private String receviceIncompleteError() {
		mReader.resetRecevice();
		return ERROR.RECEVICE_INCOMPLETE;
	}
}
