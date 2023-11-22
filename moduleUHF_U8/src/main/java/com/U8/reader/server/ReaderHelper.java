package com.U8.reader.server;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.LocalBroadcastManager;
import android.util.Log;

import com.U8.operation.U8Series;
import com.U8.reader.CMD;
import com.U8.reader.ERROR;
import com.U8.reader.HEAD;
import com.U8.reader.MessageTran;
import com.U8.reader.model.ISO180006BOperateTagBuffer;
import com.U8.reader.model.InventoryBuffer;
import com.U8.reader.model.OperateTagBuffer;
import com.U8.reader.model.ReaderSetting;
import com.U8.utils.MusicPlayer;
import com.U8.utils.StringTool;
import com.U8.utils.Tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

public class ReaderHelper {
	public final static String BROADCAST_ON_LOST_CONNECT = "com.reader.helper.onLostConnect";
	public final static String BROADCAST_WRITE_DATA = "com.reader.helper.writeData";
	public final static String BROADCAST_WRITE_LOG = "com.reader.helper.writeLog";
	public final static String BROADCAST_REFRESH_READER_SETTING = "com.reader.helper.refresh.readerSetting";
	public final static String BROADCAST_REFRESH_INVENTORY = "com.reader.helper.refresh.inventory";
	public final static String BROADCAST_REFRESH_INVENTORY_REAL = "com.reader.helper.refresh.inventoryReal";
	public final static String BROADCAST_REFRESH_FAST_SWITCH = "com.reader.helper.refresh.fastSwitch";
	public final static String BROADCAST_REFRESH_OPERATE_TAG = "com.reader.helper.refresh.operateTag";
	public final static String BROADCAST_REFRESH_ISO18000_6B = "com.reader.helper.refresh.ISO180006B";

	private static LocalBroadcastManager mLocalBroadcastManager = null;

	public final static byte INVENTORY_ERR = 0x00;
	public final static byte INVENTORY_ERR_END = 0x01;
	public final static byte INVENTORY_END = 0x02;

	public final static int WRITE_LOG = 0x10;
	public final static int REFRESH_READER_SETTING = 0x11;
	public final static int REFRESH_INVENTORY = 0x12;
	public final static int REFRESH_INVENTORY_REAL = 0x13;
	public final static int REFRESH_FAST_SWITCH = 0x14;
	public final static int REFRESH_OPERATE_TAG = 0x15;
	public final static int REFRESH_ISO18000_6B = 0x15;

	public final static int LOST_CONNECT = 0x20;

	private Tools tools = new Tools();

	private static ReaderBase mReader;
	private static Context mContext;

	private static ReaderHelper mReaderHelper;

	private static ReaderSetting m_curReaderSetting;
	private static InventoryBuffer m_curInventoryBuffer;
	private static OperateTagBuffer m_curOperateTagBuffer;
	private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

	// 盘存操作前，需要先设置工作天线，用于标识当前是否在执行盘存操作
	private boolean m_bInventory = false;
	private boolean m_bISO6BContinue = false;
	// 实时盘存次数
	private int m_nTotal = 0;

	/**
	 * 构造函数
	 */
	public ReaderHelper() {

		m_curReaderSetting = new ReaderSetting();
		m_curInventoryBuffer = new InventoryBuffer();
		m_curOperateTagBuffer = new OperateTagBuffer();
		m_curOperateTagISO18000Buffer = new ISO180006BOperateTagBuffer();
	}

	/**
	 * 设置Context。
	 * 
	 * @param context
	 *            设置Context
	 * @throws Exception
	 *             当Context为空时会抛出错误
	 */
	public static void setContext(Context context) throws Exception {
		mContext = context;
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);

		mReaderHelper = new ReaderHelper();
	}

	/**
	 * 返回helper中全局的读写器帮助类。
	 * 
	 * @return 返回helper中全局的读写器帮助类
	 * @throws Exception
	 *             当helper中全局的读写器帮助类为空时会抛出错误
	 */
	public static ReaderHelper getDefaultHelper() throws Exception {

		if (mReaderHelper == null || mContext == null)
			throw new NullPointerException("mReaderHelper Or mContext is Null!");

		return mReaderHelper;
	}

	/**
	 * 设置循环标志位。
	 * 
	 * @param flag
	 *            标志
	 */
	public void setInventoryFlag(boolean flag) {
		this.m_bInventory = flag;
	}

	/**
	 * 获取循环标志位。
	 * 
	 * @return 标志
	 */
	public boolean getInventoryFlag() {
		return this.m_bInventory;
	}

	/**
	 * 设置循环标志位。
	 * 
	 * @param flag
	 *            标志
	 */
	public void setISO6BContinue(boolean m_continue) {
		this.m_bISO6BContinue = m_continue;
	}

	/**
	 * 获取循环标志位。
	 * 
	 * @return 标志
	 */
	public boolean getISO6BContinue() {
		return this.m_bISO6BContinue;
	}

	public int getInventoryTotal() {
		return this.m_nTotal;
	}

	public void setInventoryTotal(int num) {
		this.m_nTotal = num;
	}

	public void clearInventoryTotal() {
		this.m_nTotal = 0;
	}

	public ReaderSetting getCurReaderSetting() {
		return m_curReaderSetting;
	}

	public InventoryBuffer getCurInventoryBuffer() {
		return m_curInventoryBuffer;
	}

	public OperateTagBuffer getCurOperateTagBuffer() {
		return m_curOperateTagBuffer;
	}

	public ISO180006BOperateTagBuffer getCurOperateTagISO18000Buffer() {
		return m_curOperateTagISO18000Buffer;
	}

	/**
	 * 显示log。
	 * 
	 * @param strLog
	 *            log信息
	 * @param type
	 *            log等级(0x10:正确, 0x11:错误) play
	 */
	private void writeLog(String strLog, int type) {
		Intent itent = new Intent(BROADCAST_WRITE_LOG);
		itent.putExtra("type", type);
		itent.putExtra("log", strLog);
		mLocalBroadcastManager.sendBroadcast(itent);
	};

	/**
	 * 读写器各参数刷新显示。
	 * 
	 * @param btCmd
	 *            命令类型(用于指定类型的刷新)
	 * @param curSetting
	 *            当前读写器各参数
	 */
	int a = 1;

	private void refreshReaderSetting(byte btCmd, ReaderSetting curReaderSetting) {
		Intent itent = new Intent(BROADCAST_REFRESH_READER_SETTING);
		itent.putExtra("cmd", btCmd);
		settingNotify();
		mLocalBroadcastManager.sendBroadcast(itent);
	}

	private void settingNotify() {
		Object object = U8Series.object;
		synchronized (object) {
			U8Series.havenotify = true;
			object.notifyAll();
		}
	};

	/**
	 * 存盘标签(缓存模式)，标签数据刷新。
	 * 
	 * @param btCmd
	 *            命令类型(用于指定类型的刷新)
	 * @param curInventoryBuffer
	 *            当前标签数据
	 */
	private void refreshInventory(byte btCmd, InventoryBuffer curInventoryBuffer) {
		Log.i("ReaderHelper", "refreshInventory指令");
		Intent itent = new Intent(BROADCAST_REFRESH_INVENTORY);
		itent.putExtra("cmd", btCmd);
		mLocalBroadcastManager.sendBroadcast(itent);
//		 if(curInventoryBuffer.nDataCount > 0){
		 if (btCmd != INVENTORY_END && btCmd != INVENTORY_ERR_END)
			 MusicPlayer.getInstance().play(MusicPlayer.Type.OK);
//		 }
	};

	/**
	 * 存盘标签(实时模式)，标签数据刷新。
	 * 
	 * @param btCmd
	 *            命令类型(用于指定类型的刷新)
	 * @param curInventoryBuffer
	 *            当前标签数据
	 */
	private void refreshInventoryReal(byte btCmd, InventoryBuffer curInventoryBuffer) {
		Log.i("ReaderHelper", "refreshInventoryReal指令");
		Intent itent = new Intent(BROADCAST_REFRESH_INVENTORY_REAL);
		itent.putExtra("cmd", btCmd);
		mLocalBroadcastManager.sendBroadcast(itent);
//		if (curInventoryBuffer.nDataCount > 0) {
			if (btCmd != INVENTORY_END && btCmd != INVENTORY_ERR_END)
				MusicPlayer.getInstance().play(MusicPlayer.Type.OK);
//		}

	};

	/**
	 * 存盘标签(快速模式)，标签数据刷新。
	 * 
	 * @param btCmd
	 *            命令类型(用于指定类型的刷新)
	 * @param curInventoryBuffer
	 *            当前标签数据
	 */
	private void refreshFastSwitch(byte btCmd, InventoryBuffer curInventoryBuffer) {
		Intent itent = new Intent(BROADCAST_REFRESH_FAST_SWITCH);
		itent.putExtra("cmd", btCmd);
		mLocalBroadcastManager.sendBroadcast(itent);
		if (curInventoryBuffer.nDataCount > 0) {
			MusicPlayer.getInstance().play(MusicPlayer.Type.OK);
			// Tools.playMedia(mContext);
		}
	};

	/**
	 * 存盘标签(快速模式)，标签数据刷新。
	 * 
	 * @param btCmd
	 *            命令类型(用于指定类型的刷新)
	 * @param curOperateTagBuffer
	 *            当前标签数据
	 */
	private void refreshOperateTag(byte btCmd, OperateTagBuffer curOperateTagBuffer) {
		Log.i("ReaderHelper", "refreshOperateTag指令");
		Intent itent = new Intent(BROADCAST_REFRESH_OPERATE_TAG);
		itent.putExtra("cmd", btCmd);
		mLocalBroadcastManager.sendBroadcast(itent);
		// Tools.playMedia(mContext);
	};

	/**
	 * 设置并返回helper中全局的读写器基类。
	 * 
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 * @return helper中全局的读写器基类
	 * @throws Exception
	 *             当in或out为空时会抛出错误
	 */
	public ReaderBase setReader(InputStream in, OutputStream out) throws Exception {

		if (in == null || out == null)
			throw new NullPointerException("in Or out is NULL!");

		if (mReader == null) {
			Log.i("toolsdebug", "new Reader");
			mReader = new ReaderBase(in, out) {

				@Override
				public void onLostConnect() {
					mLocalBroadcastManager.sendBroadcast(new Intent(BROADCAST_ON_LOST_CONNECT));
				}

				@Override
				public void analyData(MessageTran msgTran) {
					mReaderHelper.analyData(msgTran);
				}

				@Override
				public void reciveData(byte[] btAryReceiveData) {
					// String strLog =
					// StringTool.byteArrayToString(btAryReceiveData, 0,
					// btAryReceiveData.length);
					/*
					 * Intent itent = new Intent(BROADCAST_WRITE_DATA); Integer
					 * type = ERROR.SUCCESS & 0xFF; itent.putExtra("type",
					 * type); itent.putExtra("log", strLog);
					 */
					// 接收的数据记录入磁盘
					// Loger.disk_log("Read：", strLog, "M10_U8");

					// mLocalBroadcastManager.sendBroadcast(itent);
				}

				@Override
				public void sendData(byte[] btArySendData) {
					// String strLog =
					// StringTool.byteArrayToString(btArySendData, 0,
					// btArySendData.length);
					/*
					 * Intent itent = new Intent(BROADCAST_WRITE_DATA); Integer
					 * type = ERROR.SUCCESS & 0xFF; itent.putExtra("type",
					 * type); itent.putExtra("log", strLog);
					 */
					// 发送的数据记录入磁盘
					// Loger.disk_log("Write：", strLog, "M10_U8");
					// mLocalBroadcastManager.sendBroadcast(itent);
				}
			};
		}

		return mReader;
	}

	/**
	 * 返回helper中全局的读写器基类。
	 * 
	 * @return helper中全局的读写器基类
	 * @throws Exception
	 *             当helper中全局的读写器基类为空时会抛出错误
	 */
	public ReaderBase getReader() throws Exception {
		if (mReader == null) {
			throw new NullPointerException("mReader is Null!");
		}

		return mReader;
	}

	/**
	 * 可重写函数，解析到一包数据后会调用此函数。
	 * 
	 * @param msgTran
	 *            解析到的包
	 */
	private void analyData(MessageTran msgTran) {
		Log.i("ReaderHelper", "analyData");
		if (msgTran.getPacketType() != HEAD.HEAD) {
			return;
		}

		switch (msgTran.getCmd()) {
		case CMD.RESET:
			processReset(msgTran);
			break;
		case CMD.SET_UART_BAUDRATE:
			processSetUartBaudrate(msgTran);
			break;
		case CMD.GET_FIRMWARE_VERSION:
			processGetFirmwareVersion(msgTran);
			break;
		case CMD.SET_READER_ADDRESS:
			processSetReaderAddress(msgTran);
			break;
		case CMD.SET_WORK_ANTENNA:
			processSetWorkAntenna(msgTran);
			break;
		case CMD.GET_WORK_ANTENNA:
			processGetWorkAntenna(msgTran);
			break;
		case CMD.SET_OUTPUT_POWER:
			processSetOutputPower(msgTran);
			break;
		case CMD.GET_OUTPUT_POWER:
			processGetOutputPower(msgTran);
			break;
		case CMD.SET_FREQUENCY_REGION:
			processSetFrequencyRegion(msgTran);
			break;
		case CMD.GET_FREQUENCY_REGION:
			processGetFrequencyRegion(msgTran);
			break;
		case CMD.SET_BEEPER_MODE:
			processSetBeeperMode(msgTran);
			break;
		case CMD.GET_READER_TEMPERATURE:
			Log.i("toolsdebug", "GET_READER_TEMPERATURE");
			processGetReaderTemperature(msgTran);
			break;
		case CMD.READ_GPIO_VALUE:
			processReadGpioValue(msgTran);
			break;
		case CMD.WRITE_GPIO_VALUE:
			processWriteGpioValue(msgTran);
			break;
		case CMD.SET_ANT_CONNECTION_DETECTOR:
			processSetAntConnectionDetector(msgTran);
			break;
		case CMD.GET_ANT_CONNECTION_DETECTOR:
			processGetAntConnectionDetector(msgTran);
			break;
		case CMD.SET_TEMPORARY_OUTPUT_POWER:
			processSetTemporaryOutputPower(msgTran);
			break;
		case CMD.SET_READER_IDENTIFIER:
			processSetReaderIdentifier(msgTran);
			break;
		case CMD.GET_READER_IDENTIFIER:
			processGetReaderIdentifier(msgTran);
			break;
		case CMD.SET_RF_LINK_PROFILE:
			processSetRfLinkProfile(msgTran);
			break;
		case CMD.GET_RF_LINK_PROFILE:
			processGetRfLinkProfile(msgTran);
			break;
		case CMD.GET_RF_PORT_RETURN_LOSS:
			processGetRfPortReturnLoss(msgTran);
			break;
		case CMD.INVENTORY:
			processInventory(msgTran);
			break;
		case CMD.READ_TAG:
			processReadTag(msgTran);
			break;
		case CMD.WRITE_TAG:
			processWriteTag(msgTran);
			break;
		case CMD.LOCK_TAG:
			processLockTag(msgTran);
			break;
		case CMD.KILL_TAG:
			processKillTag(msgTran);
			break;
		case CMD.SET_ACCESS_EPC_MATCH:
			processSetAccessEpcMatch(msgTran);
			break;
		case CMD.GET_ACCESS_EPC_MATCH:
			processGetAccessEpcMatch(msgTran);
			break;
		case CMD.REAL_TIME_INVENTORY:
			processRealTimeInventory(msgTran);
			break;
		case CMD.FAST_SWITCH_ANT_INVENTORY:
			processFastSwitchInventory(msgTran);
			break;
		case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:
			processCustomizedSessionTargetInventory(msgTran);
			break;
		case CMD.SET_IMPINJ_FAST_TID:
			processSetImpinjFastTid(msgTran);
			break;
		case CMD.SET_AND_SAVE_IMPINJ_FAST_TID:
			processSetAndSaveImpinjFastTid(msgTran);
			break;
		case CMD.GET_IMPINJ_FAST_TID:
			processGetImpinjFastTid(msgTran);
			break;
		case CMD.GET_INVENTORY_BUFFER:
			processGetInventoryBuffer(msgTran);
			break;
		case CMD.GET_AND_RESET_INVENTORY_BUFFER:
			processGetAndResetInventoryBuffer(msgTran);
			break;
		case CMD.GET_INVENTORY_BUFFER_TAG_COUNT:
			processGetInventoryBufferTagCount(msgTran);
			break;
		case CMD.RESET_INVENTORY_BUFFER:
			processResetInventoryBuffer(msgTran);
			break;
		default:
			break;
		}
	}

	/**
	 * 解析所有设置命令的反馈。
	 * 
	 * @param msgTran
	 */
	private void processSet(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if (btAryData[0] == ERROR.SUCCESS) {
				m_curReaderSetting.btReadId = msgTran.getReadId();
				m_curReaderSetting.blnSetResult = true;
				writeLog(strCmd, ERROR.SUCCESS);
				settingNotify();
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
				m_curReaderSetting.blnSetResult = false;
				m_curReaderSetting.strErrorCode = strErrorCode;
			}
		} else {
			strErrorCode = "未知错误";
			m_curReaderSetting.blnSetResult = false;
			m_curReaderSetting.strErrorCode = strErrorCode;
		}
		settingNotify();
		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processReset(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processSetUartBaudrate(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetFirmwareVersion(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x02) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btMajor = btAryData[0];
			m_curReaderSetting.btMinor = btAryData[1];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetReaderAddress(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processSetWorkAntenna(MessageTran msgTran) {
		// Loger.disk_log("AAAA", "processSetWorkAntenna", "M10_U8");
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strErrorCode = "";
		String strCmd = "设置工作天线成功,当前工作天线: 天线" + (m_curReaderSetting.btWorkAntenna + 1);
		if (btAryData.length == 0x01) {
			if (btAryData[0] == ERROR.SUCCESS) {
				m_curReaderSetting.btReadId = msgTran.getReadId();
				writeLog(strCmd, ERROR.SUCCESS);
				// 校验是否盘存操作
				if (m_bInventory) {
					runLoopInventroy();
				}
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);

		if (m_bInventory) {
			m_curInventoryBuffer.nCommond = 1;
			m_curInventoryBuffer.dtEndInventory = new Date();
			runLoopInventroy();
		}
	}

	private void processGetWorkAntenna(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if (btAryData[0] == 0x00 || btAryData[0] == 0x01 || btAryData[0] == 0x02 || btAryData[0] == 0x03) {
				m_curReaderSetting.btReadId = msgTran.getReadId();
				m_curReaderSetting.btWorkAntenna = btAryData[0];

				refreshReaderSetting(btCmd, m_curReaderSetting);
				writeLog(strCmd, ERROR.SUCCESS);
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetOutputPower(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetOutputPower(MessageTran msgTran) {
		Log.i("toolsdebug", "processGetOutputPower");
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x04 || btAryData.length == 0x01) {
			Log.i("toolsdebug", "processGetOutputPower11111111111");
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btAryOutputPower = btAryData.clone();

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else {
			Log.i("toolsdebug", "processGetOutputPower22222222222");
			settingNotify();
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetFrequencyRegion(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetFrequencyRegion(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x03) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btRegion = btAryData[0];
			m_curReaderSetting.btFrequencyStart = btAryData[1];
			m_curReaderSetting.btFrequencyEnd = btAryData[2];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x06) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btRegion = btAryData[0];
			m_curReaderSetting.btUserDefineFrequencyInterval = btAryData[1];
			m_curReaderSetting.btUserDefineChannelQuantity = btAryData[2];
			m_curReaderSetting.nUserDefineStartFrequency = (btAryData[3] & 0xFF) * 256 * 256 + (btAryData[4] & 0xFF) * 256 + (btAryData[5] & 0xFF);
			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetBeeperMode(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetReaderTemperature(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x02) {
			Log.i("toolsdebug", "processGetReaderTemperature11");
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btPlusMinus = btAryData[0];
			m_curReaderSetting.btTemperature = btAryData[1];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			settingNotify();
			return;
		} else if (btAryData.length == 0x01) {
			Log.i("toolsdebug", "processGetReaderTemperature22");
			strErrorCode = ERROR.format(btAryData[0]);
			settingNotify();
		} else {
			Log.i("toolsdebug", "processGetReaderTemperature33");
			strErrorCode = "未知错误";
			settingNotify();
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processReadGpioValue(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x02) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btGpio1Value = btAryData[0];
			m_curReaderSetting.btGpio2Value = btAryData[1];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processWriteGpioValue(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processSetAntConnectionDetector(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetAntConnectionDetector(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btAntDetector = btAryData[0];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetTemporaryOutputPower(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processSetReaderIdentifier(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetReaderIdentifier(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x0C) {
			m_curReaderSetting.btReadId = msgTran.getReadId();

			Arrays.fill(m_curReaderSetting.btAryReaderIdentifier, (byte) 0x00);
			System.arraycopy(btAryData, 0, m_curReaderSetting.btAryReaderIdentifier, 0, btAryData.length);

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processSetRfLinkProfile(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetRfLinkProfile(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if ((btAryData[0] & 0xFF) >= 0xD0 && (btAryData[0] & 0xFF) <= 0xD3) {
				m_curReaderSetting.btReadId = msgTran.getReadId();
				m_curReaderSetting.btRfLinkProfile = btAryData[0];

				refreshReaderSetting(btCmd, m_curReaderSetting);
				writeLog(strCmd, ERROR.SUCCESS);
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processGetRfPortReturnLoss(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			m_curReaderSetting.btReadId = msgTran.getReadId();
			m_curReaderSetting.btReturnLoss = btAryData[0];

			refreshReaderSetting(btCmd, m_curReaderSetting);
			writeLog(strCmd, ERROR.SUCCESS);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processInventory(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x09) {
			m_curInventoryBuffer.nCurrentAnt = btAryData[0];
			m_curInventoryBuffer.nTagCount = (btAryData[1] & 0xFF) * 256 + (btAryData[2] & 0xFF);
			m_curInventoryBuffer.nReadRate = (btAryData[3] & 0xFF) * 256 + (btAryData[4] & 0xFF);
			int nTotalRead = (btAryData[5] & 0xFF) * 256 * 256 * 256 + (btAryData[6] & 0xFF) * 256 * 256 + (btAryData[7] & 0xFF) * 256 + (btAryData[8] & 0xFF);
			m_curInventoryBuffer.nDataCount = nTotalRead;
			m_curInventoryBuffer.nTotalRead += nTotalRead;
			m_curInventoryBuffer.dtEndInventory = new Date();

			refreshInventory(btCmd, m_curInventoryBuffer);
			writeLog(strCmd, ERROR.SUCCESS);

			runLoopInventroy();
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
		refreshInventory(INVENTORY_ERR_END, m_curInventoryBuffer);

		runLoopInventroy();
	}

	private void processReadTag(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";
		Object object = U8Series.readObject;
		Intent itent = new Intent(BROADCAST_REFRESH_OPERATE_TAG);
		if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
			String strLog = strCmd + "失败，失败原因： " + strErrorCode;
			synchronized (object) {
				U8Series.getInstance().readDataString = null;
				U8Series.errorData = strLog;
				object.notifyAll();
			}
			// BROADCAST_REFRESH_OPERATE_TAG
			itent.putExtra("cmd", msgTran.getCmd());
			itent.putExtra("type", ERROR.FAIL);
			itent.putExtra("msg", strLog);
			// 使用新接口后广播依然发送,但接收器内无逻辑
			mLocalBroadcastManager.sendBroadcast(itent);
			// writeLog(strLog, ERROR.FAIL);
		} else {
			int nLen = btAryData.length;
			int nDataLen = (btAryData[nLen - 3] & 0xFF);
			int nEpcLen = (btAryData[2] & 0xFF) - nDataLen - 4;

			String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
			String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
			String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
			String strData = StringTool.byteArrayToString(btAryData, 7 + nEpcLen, nDataLen);

			byte btTemp = btAryData[nLen - 2];
			byte btAntId = (byte) ((btTemp & 0x03) + 1);
			int nReadCount = btAryData[nLen - 1] & 0xFF;

			OperateTagBuffer.OperateTagMap tag = new OperateTagBuffer.OperateTagMap();
			tag.strPC = strPC;
			tag.strCRC = strCRC;
			tag.strEPC = strEPC;
			tag.strData = strData;
			tag.nDataLen = nDataLen;
			tag.btAntId = btAntId;
			tag.nReadCount = nReadCount;
			m_curOperateTagBuffer.lsTagList.add(tag);
			// refreshOperateTag(btCmd, m_curOperateTagBuffer);

			synchronized (object) {
				U8Series.getInstance().readDataString = strData;

				Log.i("see", "notifyAll yes");
				object.notifyAll();
			}
			itent.putExtra("cmd", msgTran.getCmd());
			itent.putExtra("type", ERROR.SUCCESS);
			itent.putExtra("msg", strData);
			mLocalBroadcastManager.sendBroadcast(itent);

			// writeLog(strCmd, ERROR.SUCCESS);
		}
	}

	private void processWriteTag(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		Intent itent = new Intent(BROADCAST_REFRESH_OPERATE_TAG);
		if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
			String strLog = strCmd + "失败，失败原因：" + strErrorCode;

			synchronized (this) {
				U8Series.operationTagResult = 1;
				U8Series.errorData = strLog;
				this.notifyAll();
			}

			itent.putExtra("cmd", msgTran.getCmd());
			itent.putExtra("type", ERROR.FAIL);
			itent.putExtra("msg", strLog);
			mLocalBroadcastManager.sendBroadcast(itent);
			// writeLog(strLog, ERROR.FAIL);
		} else {
			int nLen = btAryData.length;
			int nEpcLen = (btAryData[2] & 0xFF) - 4;

			if (btAryData[nLen - 3] != ERROR.SUCCESS) {
				strErrorCode = ERROR.format(btAryData[nLen - 3]);
				String strLog = strCmd + "失败，失败原因：" + strErrorCode;
				synchronized (this) {
					U8Series.operationTagResult = 1;
					U8Series.errorData = strLog;
					this.notifyAll();
				}

				itent.putExtra("cmd", msgTran.getCmd());
				itent.putExtra("type", ERROR.FAIL);
				itent.putExtra("msg", strLog);
				mLocalBroadcastManager.sendBroadcast(itent);
				// writeLog(strLog, ERROR.FAIL);
				return;
			}
			String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
			String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
			String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
			String strData = "";

			byte btTemp = btAryData[nLen - 2];
			byte btAntId = (byte) ((btTemp & 0x03) + 1);
			int nReadCount = btAryData[nLen - 1] & 0xFF;

			OperateTagBuffer.OperateTagMap tag = new OperateTagBuffer.OperateTagMap();
			tag.strPC = strPC;
			tag.strCRC = strCRC;
			tag.strEPC = strEPC;
			tag.strData = strData;
			tag.nDataLen = 0;
			tag.btAntId = btAntId;
			tag.nReadCount = nReadCount;
			m_curOperateTagBuffer.lsTagList.add(tag);

			synchronized (this) {
				U8Series.operationTagResult = 0;
				U8Series.errorData="success";
				Log.i("toolsdebug", "3333");
				this.notifyAll();
			}

			itent.putExtra("cmd", msgTran.getCmd());
			itent.putExtra("type", ERROR.SUCCESS);
			if (msgTran.getCmd() == CMD.WRITE_TAG) {
				itent.putExtra("msg", "Write label success");
			} else if (msgTran.getCmd() == CMD.KILL_TAG) {
				itent.putExtra("msg", "Destruction of label success");
			} else if (msgTran.getCmd() == CMD.LOCK_TAG) {
				itent.putExtra("msg", "Operation is successful！");
			}
			mLocalBroadcastManager.sendBroadcast(itent);
			// refreshOperateTag(btCmd, m_curOperateTagBuffer);
			// writeLog(strCmd, ERROR.SUCCESS);
		}
	}

	/**
	 * processWriteTag 与 processLockTag 返回一致。
	 * 
	 * @param msgTran
	 *            消息包
	 */
	private void processLockTag(MessageTran msgTran) {
		processWriteTag(msgTran);
	}

	/**
	 * processKillTag 与 processLockTag 返回一致。
	 * 
	 * @param msgTran
	 *            消息包
	 */
	private void processKillTag(MessageTran msgTran) {
		processWriteTag(msgTran);
	}

	private void processSetAccessEpcMatch(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetAccessEpcMatch(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if (btAryData[0] == 0x01) {
				writeLog("无匹配标签", ERROR.FAIL);
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			if (btAryData[0] == 0x00) {
				m_curOperateTagBuffer.strAccessEpcMatch = StringTool.byteArrayToString(btAryData, 2, btAryData[1] & 0xFF);

				refreshOperateTag(btCmd, m_curOperateTagBuffer);
				writeLog(strCmd, ERROR.SUCCESS);
				return;
			} else {
				strErrorCode = "未知错误";
			}
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;

		writeLog(strLog, ERROR.FAIL);
	}

	private void processRealTimeInventory(MessageTran msgTran) {

		// Loger.disk_log("AAAA", "processRealTimeInventory===============",
		// "M10_U8");

		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) { // 本次获取因为错误结束
			strErrorCode = ERROR.format(btAryData[0]);
			String strLog = strCmd + "失败，失败原因： " + strErrorCode;

			writeLog(strLog, ERROR.FAIL);
			refreshInventoryReal(INVENTORY_ERR_END, m_curInventoryBuffer);
			// Loger.disk_log("AAAA",
			// "processRealTimeInventory:1-runLoopInventroy", "M10_U8");
			runLoopInventroy();
		} else if (btAryData.length == 0x07) { // 本次获取正常结束
			m_curInventoryBuffer.nReadRate = (btAryData[1] & 0xFF) * 256 + (btAryData[2] & 0xFF);
			m_curInventoryBuffer.nDataCount = (btAryData[3] & 0xFF) * 256 * 256 * 256 + (btAryData[4] & 0xFF) * 256 * 256 + (btAryData[5] & 0xFF) * 256 + (btAryData[6] & 0xFF);

			writeLog(strCmd, ERROR.SUCCESS);
			refreshInventoryReal(INVENTORY_END, m_curInventoryBuffer);
			// Loger.disk_log("AAAA",
			// "processRealTimeInventory:2-runLoopInventroy", "M10_U8");
			runLoopInventroy();
		} else {

			if (m_curInventoryBuffer.bLoopInventoryReal || m_curInventoryBuffer.bLoopInventory) {
				// Loger.disk_log("AAAA", "processRealTimeInventory:else",
				// "M10_U8");
				m_nTotal++;
				int nLength = btAryData.length;
				int nEpcLength = nLength - 4;

				String strEPC = StringTool.byteArrayToString(btAryData, 3, nEpcLength);
				String strPC = StringTool.byteArrayToString(btAryData, 1, 2);
				String strRSSI = String.valueOf(btAryData[nLength - 1] & 0xFF);
				setMaxMinRSSI(btAryData[nLength - 1] & 0xFF);
				byte btTemp = btAryData[0];
				byte btAntId = (byte) ((btTemp & 0x03) + 1);
				m_curInventoryBuffer.nCurrentAnt = btAntId & 0xFF;

				byte btFreq = (byte) ((btTemp & 0xFF) >> 2);
				String strFreq = getFreqString(btFreq);

				InventoryBuffer.InventoryTagMap tag = null;
				Integer findIndex = m_curInventoryBuffer.dtIndexMap.get(strEPC);
				if (findIndex == null) {
					tag = new InventoryBuffer.InventoryTagMap();
					tag.strPC = strPC;
					tag.strEPC = strEPC;
					tag.strRSSI = strRSSI;
					tag.nReadCount = 1;
					tag.strFreq = strFreq;
					m_curInventoryBuffer.lsTagList.add(tag);
					m_curInventoryBuffer.dtIndexMap.put(strEPC, m_curInventoryBuffer.lsTagList.size() - 1);
				} else {
					tag = m_curInventoryBuffer.lsTagList.get(findIndex);
					tag.strRSSI = strRSSI;
					tag.nReadCount++;
					tag.strFreq = strFreq;
				}
				m_curInventoryBuffer.dtEndInventory = new Date();
				refreshInventoryReal(btCmd, m_curInventoryBuffer);

			}

		}
	}

	private void processFastSwitchInventory(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) { // 轮询因为错误结束
			strErrorCode = ERROR.format(btAryData[0]);
			String strLog = strCmd + "失败，失败原因： " + strErrorCode;

			writeLog(strLog, ERROR.FAIL);
			refreshFastSwitch(INVENTORY_ERR_END, m_curInventoryBuffer);
			runLoopFastSwitch();
		} else if (btAryData.length == 0x02) { // 轮询过程中出现错误，但未结束
			strErrorCode = ERROR.format(btAryData[1]);
			String strLog = strCmd + "失败，失败原因： " + strErrorCode + "--" + "天线" + ((btAryData[0] & 0xFF) + 1);

			writeLog(strLog, ERROR.FAIL);
		} else if (btAryData.length == 0x07) { // 轮询正常结束
			// m_nSwitchTotal, m_nSwitchTime
			int nSwitchTotal = (btAryData[0] & 0xFF) * 255 * 255 + (btAryData[1] & 0xFF) * 255 + (btAryData[2] & 0xFF);
			int nSwitchTime = (btAryData[3] & 0xFF) * 255 * 255 * 255 + (btAryData[4] & 0xFF) * 255 * 255 + (btAryData[5] & 0xFF) * 255 + (btAryData[6] & 0xFF);

			m_curInventoryBuffer.nDataCount = nSwitchTotal;
			m_curInventoryBuffer.nCommandDuration = nSwitchTime;

			writeLog(strCmd, ERROR.SUCCESS);
			refreshFastSwitch(INVENTORY_END, m_curInventoryBuffer);
			runLoopFastSwitch();
		} else {
			m_nTotal++;
			int nLength = btAryData.length;
			int nEpcLength = nLength - 4;

			// 增加盘存明细表
			String strEPC = StringTool.byteArrayToString(btAryData, 3, nEpcLength);
			String strPC = StringTool.byteArrayToString(btAryData, 1, 2);
			String strRSSI = String.valueOf(btAryData[nLength - 1] & 0xFF);
			setMaxMinRSSI(btAryData[nLength - 1] & 0xFF);
			byte btTemp = btAryData[0];
			byte btAntId = (byte) ((btTemp & 0x03) + 1);
			m_curInventoryBuffer.nCurrentAnt = btAntId & 0xFF;
			// String strAntId = String.valueOf(btAntId & 0xFF);

			byte btFreq = (byte) ((btTemp & 0xFF) >> 2);
			String strFreq = getFreqString(btFreq);

			InventoryBuffer.InventoryTagMap tag = null;
			Integer findIndex = m_curInventoryBuffer.dtIndexMap.get(strEPC);
			if (findIndex == null) {
				tag = new InventoryBuffer.InventoryTagMap();
				tag.strPC = strPC;
				//tag.strEPC = tools.covertAscii(strEPC.replace(" ",""));
				tag.strEPC = strEPC;
				tag.strRSSI = strRSSI;
				tag.nReadCount = 1;
				tag.strFreq = strFreq;
				tag.nAnt1 = 0;
				tag.nAnt2 = 0;
				tag.nAnt3 = 0;
				tag.nAnt4 = 0;

				switch (btAntId) {
				case 0x01:
					tag.nAnt1 = 1;
					break;
				case 0x02:
					tag.nAnt2 = 1;
					break;
				case 0x03:
					tag.nAnt3 = 1;
					break;
				case 0x04:
					tag.nAnt4 = 1;
					break;
				default:
					break;
				}
				m_curInventoryBuffer.lsTagList.add(tag);
				m_curInventoryBuffer.dtIndexMap.put(strEPC, m_curInventoryBuffer.lsTagList.size() - 1);
			} else {
				tag = m_curInventoryBuffer.lsTagList.get(findIndex);
				tag.strRSSI = strRSSI;
				tag.nReadCount++;
				;
				tag.strFreq = strFreq;
				switch (btAntId) {
				case 0x01:
					tag.nAnt1++;
					break;
				case 0x02:
					tag.nAnt2++;
					break;
				case 0x03:
					tag.nAnt3++;
					break;
				case 0x04:
					tag.nAnt4++;
					break;
				default:
					break;
				}
			}

			m_curInventoryBuffer.dtEndInventory = new Date();
			refreshFastSwitch(btCmd, m_curInventoryBuffer);
		}

	}

	/**
	 * processCustomizedSessionTargetInventory 与 processRealTimeInventory 返回一致。
	 * 
	 * @param msgTran
	 *            消息包内容
	 */
	private void processCustomizedSessionTargetInventory(MessageTran msgTran) {
		processRealTimeInventory(msgTran);
	}

	private void processSetImpinjFastTid(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processSetAndSaveImpinjFastTid(MessageTran msgTran) {
		processSet(msgTran);
	}

	private void processGetImpinjFastTid(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if (btAryData[0] == 0x00 || (btAryData[0] & 0xFF) == 0x8D) {
				m_curReaderSetting.btReadId = msgTran.getReadId();
				m_curReaderSetting.btMonzaStatus = btAryData[0];

				refreshReaderSetting(btCmd, m_curReaderSetting);
				writeLog(strCmd, ERROR.SUCCESS);
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void processGetInventoryBuffer(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
			String strLog = strCmd + "失败，失败原因： " + strErrorCode;

			writeLog(strLog, ERROR.FAIL);
		} else {
			int nDataLen = btAryData.length;
			int nEpcLen = (btAryData[2] & 0xFF) - 4;

			String strPC = StringTool.byteArrayToString(btAryData, 3, 2);
			String strEPC = StringTool.byteArrayToString(btAryData, 5, nEpcLen);
			String strCRC = StringTool.byteArrayToString(btAryData, 5 + nEpcLen, 2);
			String strRSSI = String.valueOf(btAryData[nDataLen - 3] & 0xFF);
			setMaxMinRSSI(btAryData[nDataLen - 3] & 0xFF);
			byte btTemp = btAryData[nDataLen - 2];
			byte btAntId = (byte) ((btTemp & 0x03) + 1);
			int nReadCount = btAryData[nDataLen - 1] & 0xFF;

			InventoryBuffer.InventoryTagMap tag = new InventoryBuffer.InventoryTagMap();
			tag.strPC = strPC;
			tag.strCRC = strCRC;
			//tag.strEPC = tools.covertAscii(strEPC.replace(" ",""));
			tag.strEPC = strEPC;
			tag.btAntId = btAntId;
			tag.strRSSI = strRSSI;
			tag.nReadCount = nReadCount;
			m_curInventoryBuffer.lsTagList.add(tag);
			m_curInventoryBuffer.dtIndexMap.put(strEPC, m_curInventoryBuffer.lsTagList.size() - 1);

			refreshInventory(btCmd, m_curInventoryBuffer);
			writeLog(strCmd, ERROR.SUCCESS);
		}
	}

	/**
	 * processGetAndResetInventoryBuffer 和 processGetInventoryBuffer 返回一致。
	 * 
	 * @param msgTran
	 *            消息包内容
	 */
	private void processGetAndResetInventoryBuffer(MessageTran msgTran) {
		processGetInventoryBuffer(msgTran);
	}

	private void processGetInventoryBufferTagCount(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x02) {
			m_curInventoryBuffer.nTagCount = (btAryData[0] & 0xFF) * 256 + (btAryData[1] & 0xFF);

			refreshInventory(btCmd, m_curInventoryBuffer);
			String strLog = strCmd + "：" + String.valueOf(m_curInventoryBuffer.nTagCount);
			writeLog(strLog, ERROR.FAIL);
			return;
		} else if (btAryData.length == 0x01) {
			strErrorCode = ERROR.format(btAryData[0]);
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;

		writeLog(strLog, ERROR.FAIL);
	}

	private void processResetInventoryBuffer(MessageTran msgTran) {
		byte btCmd = msgTran.getCmd();
		byte[] btAryData = msgTran.getAryData();
		String strCmd = CMD.format(btCmd);
		String strErrorCode = "";

		if (btAryData.length == 0x01) {
			if (btAryData[0] == ERROR.SUCCESS) {
				refreshInventory(btCmd, m_curInventoryBuffer);
				writeLog(strCmd, ERROR.SUCCESS);
				return;
			} else {
				strErrorCode = ERROR.format(btAryData[0]);
			}
		} else {
			strErrorCode = "未知错误";
		}

		String strLog = strCmd + "失败，失败原因： " + strErrorCode;
		writeLog(strLog, ERROR.FAIL);
	}

	private void setMaxMinRSSI(int nRSSI) {
		if (m_curInventoryBuffer.nMaxRSSI < nRSSI) {
			m_curInventoryBuffer.nMaxRSSI = nRSSI;
		}

		if (m_curInventoryBuffer.nMinRSSI == 0) {
			m_curInventoryBuffer.nMinRSSI = nRSSI;
		} else if (m_curInventoryBuffer.nMinRSSI > nRSSI) {
			m_curInventoryBuffer.nMinRSSI = nRSSI;
		}
	}

	private String getFreqString(byte btFreq) {
		if (m_curReaderSetting.btRegion == 4) {
			float nExtraFrequency = (float) (btFreq & 0xFF) * (m_curReaderSetting.btUserDefineFrequencyInterval & 0xFF) * 10;
			float nstartFrequency = (float) ((float) (m_curReaderSetting.nUserDefineStartFrequency & 0xFF)) / 1000;
			float nStart = (float) (nstartFrequency + nExtraFrequency / 1000);
			String strTemp = String.format("%.3f", nStart);

			return strTemp;
		} else {
			if ((btFreq & 0xFF) < 0x07) {
				float nStart = (float) (865.00f + (float) (btFreq & 0xFF) * 0.5f);
				String strTemp = String.format("%.2f", nStart);

				return strTemp;
			} else {
				float nStart = (float) (902.00f + ((float) (btFreq & 0xFF) - 7) * 0.5f);
				String strTemp = String.format("%.2f", nStart);

				return strTemp;
			}
		}
	}

	/**
	 * 循环盘询
	 */
	private void runLoopInventroy() {
		Log.i("toolsdebug", "runLoopInventroy runLoopInventroy");
		if (m_curInventoryBuffer.bLoopInventoryReal) {
			// m_bLockTab = true;
			// btnInventory.Enabled = false;
			if (m_curInventoryBuffer.bLoopCustomizedSession) { // 自定义Session和Inventoried
				Log.i("toolsdebug", "bLoopCustomizedSession bLoopCustomizedSession"); // Flag
				mReader.customizedSessionTargetInventory(m_curReaderSetting.btReadId, m_curInventoryBuffer.btSession, m_curInventoryBuffer.btTarget, m_curInventoryBuffer.btRepeat);
			} else { // 实时盘存
				Log.i("ReaderHelper", "runLoopInventroy     realTimeInventory");
				mReader.realTimeInventory(m_curReaderSetting.btReadId, m_curInventoryBuffer.btRepeat);

			}
		} else if (m_curInventoryBuffer.bLoopInventory) {
			Log.i("ReaderHelper", "runLoopInventroy     inventory");
			mReader.inventory(m_curReaderSetting.btReadId, m_curInventoryBuffer.btRepeat);
		}
		/*
		 * //校验盘存是否所有天线均完成 if ( m_curInventoryBuffer.nIndexAntenna <
		 * m_curInventoryBuffer.lAntenna.size() - 1 ||
		 * m_curInventoryBuffer.nCommond == 0) {
		 * 
		 * if (m_curInventoryBuffer.nCommond == 0) {
		 * m_curInventoryBuffer.nCommond = 1;
		 * 
		 * if (m_curInventoryBuffer.bLoopInventoryReal) { //m_bLockTab = true;
		 * //btnInventory.Enabled = false; if
		 * (m_curInventoryBuffer.bLoopCustomizedSession) {
		 * //自定义Session和Inventoried Flag
		 * //mReader.customizedSessionTargetInventory
		 * (m_curReaderSetting.btReadId, m_curInventoryBuffer.btSession,
		 * m_curInventoryBuffer.btTarget, m_curInventoryBuffer.btRepeat); } else
		 * { //实时盘存 mReader.realTimeInventory(m_curReaderSetting.btReadId,
		 * m_curInventoryBuffer.btRepeat);
		 * 
		 * } } else if (m_curInventoryBuffer.bLoopInventory) {
		 * mReader.inventory(m_curReaderSetting.btReadId,
		 * m_curInventoryBuffer.btRepeat); } } else {
		 * m_curInventoryBuffer.nCommond = 0;
		 * m_curInventoryBuffer.nIndexAntenna++;
		 * 
		 * byte btWorkAntenna =
		 * m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer
		 * .nIndexAntenna); mReader.setWorkAntenna(m_curReaderSetting.btReadId,
		 * btWorkAntenna); m_curReaderSetting.btWorkAntenna = btWorkAntenna; } }
		 * else if (m_curInventoryBuffer.bLoopInventory ||
		 * m_curInventoryBuffer.bLoopInventoryReal) { //校验是否循环盘存
		 * m_curInventoryBuffer.nIndexAntenna = 0; m_curInventoryBuffer.nCommond
		 * = 0;
		 * 
		 * byte btWorkAntenna =
		 * m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer
		 * .nIndexAntenna); mReader.setWorkAntenna(m_curReaderSetting.btReadId,
		 * btWorkAntenna); m_curReaderSetting.btWorkAntenna = btWorkAntenna; }
		 */

	}

	private void runLoopFastSwitch() {
		if (m_curInventoryBuffer.bLoopInventory) {
			mReader.fastSwitchAntInventory(m_curReaderSetting.btReadId, m_curInventoryBuffer.btA, m_curInventoryBuffer.btStayA, m_curInventoryBuffer.btB, m_curInventoryBuffer.btStayB, m_curInventoryBuffer.btC, m_curInventoryBuffer.btStayC, m_curInventoryBuffer.btD, m_curInventoryBuffer.btStayD, m_curInventoryBuffer.btInterval, m_curInventoryBuffer.btFastRepeat);
		}
	}
}
