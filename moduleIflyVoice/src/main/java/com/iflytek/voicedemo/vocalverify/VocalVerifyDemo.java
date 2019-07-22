package com.iflytek.voicedemo.vocalverify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.record.PcmRecorder;
import com.iflytek.cloud.util.VerifierUtil;
import com.iflytek.voicedemo.IdentifyGroup.GroupManagerActivity;
import com.epichust.voice.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 声纹密码示例
 *
 * @author hjyu
 * @date 2017/9/28.
 * @see <a href="http://www.xfyun.cn">讯飞开放平台</a>
 */
public class VocalVerifyDemo extends Activity implements OnClickListener,View.OnTouchListener {
	private static final String TAG = VocalVerifyDemo.class.getSimpleName();
	// 密码类型
	// 默认为数字密码
	private int mPwdType = 3;
	// 数字密码类型为3，其他类型暂未开放
	private static final int PWD_TYPE_NUM = 3;

	// 会话类型
	private int mSST = 0;
	// 注册
	private static final int SST_ENROLL = 0;
	// 验证
	private static final int SST_VERIFY = 1;

	// 模型操作类型
	private int mModelCmd;
	// 查询模型
	private static final int MODEL_QUE = 0;
	// 删除模型
	private static final int MODEL_DEL = 1;

	// 用户id，唯一标识
	private String authid;
	// 身份验证对象
	private IdentityVerifier mIdVerifier;
	// 数字声纹密码
	private String mNumPwd = "";
	// 数字声纹密码段，默认有5段
	private String[] mNumPwdSegs;
	// 用于验证的数字密码
	private String mVerifyNumPwd = "";

	private EditText mResultEditText;
	private Button btn_start_record;
	private EditText mAuthidEditText;
	private RadioGroup mSstTypeGroup;
	private Toast mToast;

	// 是否可以录音
	private boolean mCanStartRecord = false;
	// 是否可以录音
	private boolean isStartWork = false;
	// 录音采样率
	private final int SAMPLE_RATE = 16000;
	// pcm录音机
	private PcmRecorder mPcmRecorder;
	// 进度对话框
	private ProgressDialog mProDialog;

	/**
	 * 下载密码监听器
	 */
	private IdentityListener mDownloadPwdListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());
			mAuthidEditText.setEnabled(false);//密码获取成功后，authid不可再变动

			mProDialog.dismiss();
			btn_start_record.setClickable(true);
			switch (mPwdType) {
				case PWD_TYPE_NUM:
					StringBuffer numberString = new StringBuffer();
					try {
						JSONObject object = new JSONObject(result.getResultString());
						if (!object.has("num_pwd")) {
							mNumPwd = null;
							return;
						}

						JSONArray pwdArray = object.optJSONArray("num_pwd");
						numberString.append(pwdArray.get(0));
						for (int i = 1; i < pwdArray.length(); i++) {
							numberString.append("-" + pwdArray.get(i));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mNumPwd = numberString.toString();
					mNumPwdSegs = mNumPwd.split("-");

					mResultEditText.setText("您的注册密码：\n" + mNumPwd + "\n请长按“按住说话”按钮进行注册\n");
					break;
				default:
					break;
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}

		@Override
		public void onError(SpeechError error) {
			mProDialog.dismiss();
			mAuthidEditText.setEnabled(true);
			// 下载密码时，恢复按住说话触摸
			// 下载密码时，恢复按住说话触摸
			btn_start_record.setClickable(true);
			mResultEditText.setText("密码下载失败！" + error.getPlainDescription(true));
		}
	};

	/**
	 * 声纹注册监听器
	 */
	private IdentityListener mEnrollListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, result.getResultString());

			JSONObject jsonResult = null;
			try {
				jsonResult = new JSONObject(result.getResultString());
				int ret = jsonResult.getInt("ret");

				if (ErrorCode.SUCCESS == ret) {

					final int suc = Integer.parseInt(jsonResult.optString("suc"));
					final int rgn = Integer.parseInt(jsonResult.optString("rgn"));

					if (suc == rgn) {
						mResultEditText.setText("注册成功");

						mCanStartRecord = false;
						isStartWork = false;
						if (mPcmRecorder != null) {
							mPcmRecorder.stopRecord(true);
						}
					} else {
						int nowTimes = suc + 1;
						int leftTimes = 5 - nowTimes;

						StringBuffer strBuffer = new StringBuffer();
						strBuffer.append("请长按“按住说话”按钮！\n");
						strBuffer.append("请读出：" + mNumPwdSegs[nowTimes - 1] + "\n");
						strBuffer.append("训练 第" + nowTimes + "遍，剩余" + leftTimes + "遍");
						mResultEditText.setText(strBuffer.toString());
					}

				} else {
					showTip(new SpeechError(ret).getPlainDescription(true));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {
			if (SpeechEvent.EVENT_VOLUME == eventType) {
				showTip("音量：" + arg1);
			} else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
				showTip("录音结束");
			}

		}

		@Override
		public void onError(SpeechError error) {
			isStartWork = false;

			StringBuffer errorResult = new StringBuffer();
			errorResult.append("注册失败！\n");
			errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
			errorResult.append("请长按“按住说话”重新注册!");
			mResultEditText.setText(errorResult.toString());
		}

	};

	/**
	 * 声纹验证监听器
	 */
	private IdentityListener mVerifyListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, "verify:" + result.getResultString());

			try {
				JSONObject object = new JSONObject(result.getResultString());
				String decision = object.getString("decision");

				if ("accepted".equalsIgnoreCase(decision)) {
					mResultEditText.setText("验证通过");
				} else {
					mResultEditText.setText("验证失败");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			isStartWork = false;
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			if (SpeechEvent.EVENT_VOLUME == eventType) {
				showTip("音量：" + arg1);
			} else if (SpeechEvent.EVENT_VAD_EOS == eventType) {
				showTip("录音结束");
			}
		}

		@Override
		public void onError(SpeechError error) {
			isStartWork = false;
			mCanStartRecord = false;

			StringBuffer errorResult = new StringBuffer();
			errorResult.append("验证失败！\n");
			errorResult.append("错误信息：" + error.getPlainDescription(true) + "\n");
			errorResult.append("请长按“按住说话”重新验证!");
			mResultEditText.setText(errorResult.toString());
		}
	};

	/**
	 * 声纹模型操作监听器
	 */
	private IdentityListener mModelListener = new IdentityListener() {

		@Override
		public void onResult(IdentityResult result, boolean islast) {
			Log.d(TAG, "model operation:" + result.getResultString());

			mProDialog.dismiss();

			JSONObject jsonResult = null;
			int ret = ErrorCode.SUCCESS;
			try {
				jsonResult = new JSONObject(result.getResultString());
				ret = jsonResult.getInt("ret");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			switch (mModelCmd) {
				case MODEL_QUE:
					if (ErrorCode.SUCCESS == ret) {
						showTip("模型存在");
					} else {
						showTip("模型不存在");
					}
					break;
				case MODEL_DEL:
					if (ErrorCode.SUCCESS == ret) {
						mAuthidEditText.setEnabled(true);//模型删除后，可重新配置authid
						showTip("模型已删除");
					} else {
						showTip("模型删除失败");
					}
					break;

				default:
					break;
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}

		@Override
		public void onError(SpeechError error) {
			mProDialog.dismiss();
			showTip(error.getPlainDescription(true));
		}
	};

	/**
	 * 录音机监听器
	 */
	private PcmRecorder.PcmRecordListener mPcmRecordListener = new PcmRecorder.PcmRecordListener() {

		@Override
		public void onRecordStarted(boolean success) {
		}

		@Override
		public void onRecordReleased() {
		}

		@Override
		public void onRecordBuffer(byte[] data, int offset, int length) {
			StringBuffer params = new StringBuffer();

			switch (mSST) {
				case SST_ENROLL:
					params.append("rgn=5,");
					params.append("ptxt=" + mNumPwd + ",");
					params.append("pwdt=" + mPwdType + ",");
					mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
					break;
				case SST_VERIFY:
					params.append("ptxt=" + mVerifyNumPwd + ",");
					params.append("pwdt=" + mPwdType + ",");
					mIdVerifier.writeData("ivp", params.toString(), data, 0, length);
					break;
				default:
					break;
			}
		}

		@Override
		public void onError(SpeechError e) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.isvdemo);

		initUi();

		mIdVerifier = IdentityVerifier.createVerifier(VocalVerifyDemo.this, new InitListener() {

			@Override
			public void onInit(int errorCode) {
				if (ErrorCode.SUCCESS == errorCode) {
					showTip("引擎初始化成功");
				} else {
					showTip("引擎初始化失败，错误码：" + errorCode);
				}
			}
		});
	}

	@SuppressLint("ShowToast")
	private void initUi() {
		mResultEditText = (EditText) findViewById(R.id.edt_result);
		mAuthidEditText = (EditText) findViewById(R.id.set_authId);
		btn_start_record = (Button) findViewById(R.id.isv_reocrd);
		btn_start_record.setOnTouchListener(VocalVerifyDemo.this);
		findViewById(R.id.isv_getpassword).setOnClickListener(VocalVerifyDemo.this);
		findViewById(R.id.isv_search).setOnClickListener(VocalVerifyDemo.this);
		findViewById(R.id.isv_delete).setOnClickListener(VocalVerifyDemo.this);
		findViewById(R.id.isv_identity).setOnClickListener(VocalVerifyDemo.this);


		mToast = Toast.makeText(VocalVerifyDemo.this, "", Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

		mProDialog = new ProgressDialog(VocalVerifyDemo.this);
		mProDialog.setCancelable(true);
		mProDialog.setTitle("请稍候");
		// cancel进度框时，取消正在进行的操作
		mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (null != mIdVerifier) {
					mIdVerifier.cancel();
				}
			}
		});

		// 密码选择RadioGroup初始化
		mSstTypeGroup = (RadioGroup) findViewById(R.id.vocal_radioGroup1);
		mSstTypeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			if( null == mIdVerifier ){
				// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
				showTip( "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
				return;
			}

			// 取消之前操作
			if (mIdVerifier.isWorking()) {
				mIdVerifier.cancel();
			}
			cancelOperation();

			if (checkedId == R.id.vocal_radioVerify) {
				// 设置会话类型为验证
				mSST = SST_VERIFY;
				mVerifyNumPwd = VerifierUtil.generateNumberPassword(8);

				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append("您的验证密码：" + mVerifyNumPwd + "\n");
				strBuffer.append("请长按“按住说话”按钮进行验证！\n");
				mResultEditText.setText(strBuffer.toString());

			}else if(checkedId == R.id.vocal_radioEnroll){
				// 设置会话类型为验证
				mSST = SST_ENROLL;

				if (null == mNumPwdSegs) {
					// 首次注册密码为空时，调用下载密码
					downloadPwd();
				} else {
					mResultEditText.setText("请长按“按住说话”按钮进行注册\n");
				}
			}
			}
		});
	}

	private void cancelOperation() {
		isStartWork = false;
		mIdVerifier.cancel();

		if (null != mPcmRecorder) {
			mPcmRecorder.stopRecord(true);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if( !checkInstance() ){
			return false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				authid = getAuthid();
				if(TextUtils.isEmpty(authid)) {
					showTip("请输入authid");
					break;
				}
				if (!isStartWork) {
					// 根据业务类型调用服务
					if (mSST == SST_ENROLL) {
						if (null == mNumPwdSegs) {
							// 启动录音机时密码为空，中断此次操作，下载密码
							downloadPwd();
							break;
						}
						vocalEnroll();
					} else if (mSST == SST_VERIFY) {
						vocalVerify();
					} else {
						showTip("请先选择相应业务！");
						break;
					}
					isStartWork = true;
					mCanStartRecord = true;
				}
				if (mCanStartRecord) {
					try {
						mPcmRecorder = new PcmRecorder(SAMPLE_RATE, 40);
						mPcmRecorder.startRecording(mPcmRecordListener);
					} catch (SpeechError e) {
						e.printStackTrace();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				v.performClick();

				mIdVerifier.stopWrite("ivp");
				if (null != mPcmRecorder) {

					mPcmRecorder.stopRecord(true);
				}
				break;

			default:
				break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if( !checkInstance() ){
			return;
		}
		authid = getAuthid();
		if(TextUtils.isEmpty(authid)){
			showTip("用户id为空，请重新输入");
			return;
		}

		// 取消先前操作
		cancelOperation();
		if (v.getId() == R.id.isv_getpassword) {
			if (null == mNumPwdSegs) {
				// 首次注册密码为空时，调用下载密码
				downloadPwd();
			} else {
				showTip("数字密码已存在");
			}
		}else if (v.getId() == R.id.isv_search){
			// 执行查询模型
			mModelCmd = MODEL_QUE;
			executeModelCommand("query");
		}else if (v.getId() == R.id.isv_delete){
			// 执行删除模型
			mModelCmd = MODEL_DEL;
			executeModelCommand("delete");
		}else if (v.getId() == R.id.isv_identity){
			Intent init  = new Intent(VocalVerifyDemo.this, GroupManagerActivity.class);
			init.putExtra("auth_id",authid);
			init.putExtra("mfv_scenes","ivp");
			startActivity(init);
		}
	}

	/**
	 * 注册
	 */
	private void vocalEnroll() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("请长按“按住说话”按钮！\n");
		strBuffer.append("请读出：" + mNumPwdSegs[0] + "\n");
		strBuffer.append("训练 第" + 1 + "遍，剩余4遍\n");
		mResultEditText.setText(strBuffer.toString());

		// 设置声纹注册参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
		// 设置会话类型
		mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
		// 设置监听器，开始会话
		mIdVerifier.startWorking(mEnrollListener);
	}

	private void vocalVerify() {

		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("您的验证密码：" + mVerifyNumPwd + "\n");
		strBuffer.append("请长按“按住说话”按钮进行验证！\n");
		mResultEditText.setText(strBuffer.toString());
		// 设置声纹验证参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
		// 设置会话类型
		mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
		// 验证模式，单一验证模式：sin
		mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
		// 用户的唯一标识，在声纹业务获取注册、验证、查询和删除模型时都要填写，不能为空
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);
		// 设置监听器，开始会话
		mIdVerifier.startWorking(mVerifyListener);
	}

	/**
	 * 下载密码
	 */
	private void downloadPwd() {
		authid = getAuthid();
		if(TextUtils.isEmpty(authid)) {
			showTip("请输入authid");
			return;
		}
		// 获取密码之前先终止之前的操作
		mIdVerifier.cancel();
		mNumPwd = null;
		// 下载密码时，按住说话触摸无效
		btn_start_record.setClickable(false);

		mProDialog.setMessage("下载中...");
		mProDialog.show();

		// 设置下载密码参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");

		// 子业务执行参数，若无可以传空字符传
		StringBuffer params = new StringBuffer();
		// 设置模型操作的密码类型
		params.append("pwdt=" + mPwdType + ",");
		// 执行密码下载操作
		mIdVerifier.execute("ivp", "download", params.toString(), mDownloadPwdListener);
	}



	/**
	 * 模型操作
	 * @param cmd 命令
	 */
	private void executeModelCommand(String cmd) {
		if ("query".equals(cmd)) {
			mProDialog.setMessage("查询中...");
		} else if ("delete".equals(cmd)) {
			mProDialog.setMessage("删除中...");
		}
		mProDialog.show();
		// 设置声纹模型参数
		// 清空参数
		mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
		// 设置会话场景
		mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ivp");
		// 用户id
		mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authid);

		// 子业务执行参数，若无可以传空字符传
		StringBuffer params3 = new StringBuffer();
		// 设置模型操作的密码类型
		params3.append("pwdt=" + mPwdType + ",");
		// 执行模型操作
		mIdVerifier.execute("ivp", cmd, params3.toString(), mModelListener);
	}

	@Override
	protected void onDestroy() {
		if (null != mIdVerifier) {
			mIdVerifier.destroy();
			mIdVerifier = null;
		}
		super.onDestroy();
	}

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	private boolean checkInstance(){
		if( null == mIdVerifier ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化" );
			return false;
		}else{
			return true;
		}
	}

	private String getAuthid(){
		String id = mAuthidEditText.getText()==null?null:mAuthidEditText.getText().toString();
		return id;
	}
}
