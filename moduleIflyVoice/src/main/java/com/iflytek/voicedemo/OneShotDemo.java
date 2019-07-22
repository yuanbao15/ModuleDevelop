package com.iflytek.voicedemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.epichust.voice.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.iflytek.speech.util.JsonParser;
import com.zxing.qrcode.CheckPermissionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OneShotDemo extends Activity implements OnClickListener {
	private String TAG = "ivw";
	private Toast mToast;
	private TextView textView;
	// 语音唤醒对象
	private VoiceWakeuper mIvw;
	// 语音识别对象
	private SpeechRecognizer mAsr;
	// 唤醒结果内容
	private String resultString;
	// 识别结果内容
	private String recoString;
	// 设置门限值 ： 门限值越低越容易被唤醒
	private TextView tvThresh;
	private SeekBar seekbarThresh;
	private final static int MAX = 3000;
	private final static int MIN = 0;
	private int curThresh = 1450;
	private String threshStr = "门限值：";
	// 云端语法文件
	private String mCloudGrammar = null;
	// 云端语法id
	private String mCloudGrammarID;
	// 本地语法id
	private String mLocalGrammarID;
	// 本地语法文件
	private String mLocalGrammar = null;
	// 本地语法构建路径	
	private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/msc/test";
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.oneshot_activity);
		
		initUI();
        //初始化权限
        initPermission();

		// 初始化唤醒对象
		mIvw = VoiceWakeuper.createWakeuper(this, null);
		// 初始化识别对象---唤醒+识别,用来构建语法
		mAsr = SpeechRecognizer.createRecognizer(this, null);
		// 初始化语法文件
		mCloudGrammar = readFile(this, "wake_grammar_cloud.abnf", "utf-8");
		mLocalGrammar = readFile(this, "wake_grammar_local.bnf", "utf-8");
	}
	
	private void initUI() {
		findViewById(R.id.btn_oneshot).setOnClickListener(OneShotDemo.this);
		findViewById(R.id.btn_stop).setOnClickListener(OneShotDemo.this);
		findViewById(R.id.btn_grammar).setOnClickListener(OneShotDemo.this);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		textView = (TextView) findViewById(R.id.txt_show_msg);
		resultString = "";
		textView.setText(resultString);
		tvThresh = (TextView)findViewById(R.id.txt_thresh);
		
		seekbarThresh = (SeekBar)findViewById(R.id.seekBar_thresh);
		seekbarThresh.setMax(MAX - MIN);
		seekbarThresh.setProgress(curThresh);
		tvThresh.setText(threshStr + curThresh);
		
		seekbarThresh.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				curThresh = seekbarThresh.getProgress() + MIN;
				tvThresh.setText(threshStr + curThresh);
			}
		});
		//选择云端or本地
		RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioCloud) {
					mEngineType = SpeechConstant.TYPE_CLOUD;
				} else if (checkedId == R.id.radioLocal) {
					mEngineType = SpeechConstant.TYPE_LOCAL;
				}
			}
		});
	}
	
	GrammarListener grammarListener = new GrammarListener() {
		@Override
		public void onBuildFinish(String grammarId, SpeechError error) {
			if (error == null) {
				if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
					mCloudGrammarID = grammarId;
				} else {
					mLocalGrammarID = grammarId;
				}
				showTip("语法构建成功：" + grammarId);
			} else {
				showTip("语法构建失败,错误码：" + error.getErrorCode());
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_oneshot) {
			oneshot();
		}else if (v.getId() == R.id.btn_grammar) {
			int ret = 0;
			if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
				// 设置参数
				mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
				// 开始构建语法
				ret = mAsr.buildGrammar("abnf", mCloudGrammar, grammarListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("语法构建失败,错误码：" + ret);
				}
			} else {
				mAsr.setParameter(SpeechConstant.PARAMS, null);
				mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
				// 设置引擎类型
				mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
				// 设置语法构建路径
				mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
				// 设置资源路径
				mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
				ret = mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("语法构建失败,错误码：" + ret);
				}
			}
		}else if (v.getId() == R.id.btn_stop) {
			mIvw = VoiceWakeuper.getWakeuper();
			if (mIvw != null) {
				mIvw.stopListening();
				showTip("唤醒停止监听YB");
			} else {
				showTip("唤醒未初始化");
			}
		}
	}

	private void oneshot() {
		// 非空判断，防止因空指针使程序崩溃
		mIvw = VoiceWakeuper.getWakeuper();
		if (mIvw != null) {
			resultString = "";
			recoString = "";


			// 清空参数
			mIvw.setParameter(SpeechConstant.PARAMS, null);
			// 设置识别引擎
			mIvw.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
			// 设置唤醒资源路径
			final String resPath = ResourceUtil.generateResourcePath(this, RESOURCE_TYPE.assets, "ivw/" +getString(R.string.app_id)+".jet");
			mIvw.setParameter(ResourceUtil.IVW_RES_PATH, resPath);

			// 设置持续进行唤醒(一般不要设置持续唤醒，配合UI 唤醒一次 停止一次)
			mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "0");
			/**
			 * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
			 * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
			 */
			mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"	+ curThresh);
			// 设置唤醒+识别模式
			mIvw.setParameter(SpeechConstant.IVW_SST, "oneshot");
			// 设置返回结果格式
			mIvw.setParameter(SpeechConstant.RESULT_TYPE, "json");
			// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
			mAsr.setParameter(SpeechConstant.VAD_BOS,  "3000");
			// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
			mAsr.setParameter(SpeechConstant.VAD_EOS,  "1500");

			// 设置闭环优化网络模式
			//mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");  //开了后识别更不准确了

			//	mIvw.setParameter(SpeechConstant.IVW_SHOT_WORD, "0");
			// 设置唤醒录音保存路径，保存最近一分钟的音频
			mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
			mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );

			if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
				if (!TextUtils.isEmpty(mCloudGrammarID)) {
					// 设置云端识别使用的语法id
					mIvw.setParameter(SpeechConstant.CLOUD_GRAMMAR,
							mCloudGrammarID);
					mIvw.startListening(mWakeuperListener);
				} else {
					showTip("请先构建语法");
				}
			} else {
				if (!TextUtils.isEmpty(mLocalGrammarID)) {
					// 设置本地识别资源
					mIvw.setParameter(ResourceUtil.ASR_RES_PATH,
							getResourcePath());
					// 设置语法构建路径
					mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
					// 设置本地识别使用语法id
					mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR,
							mLocalGrammarID);
					mIvw.startListening(mWakeuperListener);
				} else {
					showTip("请先构建语法");
				}
			}

		} else {
			showTip("唤醒未初始化");
		}
	}

	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			try {
				String text = result.getResultString();
				JSONObject object;
				object = new JSONObject(text);
				StringBuffer buffer = new StringBuffer();
				buffer.append("【RAW】 "+text);
				buffer.append("\n");
				buffer.append("【操作类型】"+ object.optString("sst"));
				buffer.append("\n");
				buffer.append("【唤醒词id】"+ object.optString("id"));
				buffer.append("\n");
				buffer.append("【得分】" + object.optString("score"));
				buffer.append("\n");
				buffer.append("【前端点】" + object.optString("bos"));
				buffer.append("\n");
				buffer.append("【尾端点】" + object.optString("eos"));
				resultString =buffer.toString();

				// yb-test
				String wakeupWord = object.optString("keyword");
				showTip(wakeupWord + "被唤醒了");

				//yb-唤醒后再开启识别
				mAsr.startListening(mRecognizerListener);
			} catch (JSONException e) {
				resultString = "结果解析出错";
				e.printStackTrace();
			}
			textView.setText(resultString);
		}

		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
			// 继续唤醒监听
			oneshot();
		}

		@Override
		public void onBeginOfSpeech() {
//			showTip("开始说话");
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
			Log.d(TAG, "eventType:"+eventType+ "arg1:"+isLast + "arg2:" + arg2);
			// 识别结果
			if (SpeechEvent.EVENT_IVW_RESULT == eventType) {
				RecognizerResult reslut = ((RecognizerResult)obj.get(SpeechEvent.KEY_EVENT_IVW_RESULT));
				recoString += JsonParser.parseGrammarResult(reslut.getResultString());
				textView.setText(recoString);

				// 弄点命令功能
				String command = getCommandFromRec(recoString);
				Activity currentActivity = SpeechApp.getActivity();
				if(command.contains("扫码")){
					Log.w("oneshot","命令-打开了扫码窗口");
					//初始化权限
					initPermission();
				}else if(command.contains("放歌")){
					Log.w("oneshot","命令-开始放歌");
				}else if(command.contains("返回")){
					Log.w("oneshot","命令-返回");
					currentActivity.finish();
				}
			}

			// 继续唤醒监听
			oneshot();
		}

		@Override
		public void onVolumeChanged(int volume) {

		}
	};

	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onVolumeChanged(int i, byte[] bytes) {

		}

		@Override
		public void onBeginOfSpeech() {

		}

		@Override
		public void onEndOfSpeech() {

		}

		@Override
		public void onResult(RecognizerResult recognizerResult, boolean b) {

		}

		@Override
		public void onError(SpeechError speechError) {

		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {


		}
	};


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy OneShotDemo");
		mIvw = VoiceWakeuper.getWakeuper();
		if (mIvw != null) {
			mIvw.destroy();
		} else {
			showTip("唤醒未初始化");
		}
	}
	
	/**
	 * 读取asset目录下文件。
	 * 
	 * @return content
	 */
	public static String readFile(Context mContext, String file, String code) {
		int len = 0;
		byte[] buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);
			len = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);

			result = new String(buf, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// 获取识别资源路径
	private String getResourcePath() {
		StringBuffer tempBuffer = new StringBuffer();
		// 识别通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(this,
				RESOURCE_TYPE.assets, "asr/common.jet"));
		return tempBuffer.toString();
	}
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}


	/**
	 * @method    字符串处理，提取出关键命令，需要根据置信数找到最大的那个
	 * 		eg:【结果】呼叫【置信度】32\n 【结果】元宝【置信度】93\n
	 * @param
	 *
	 * @author  yuanbao
	 * @date    2019/4/12
	 */
	public String getCommandFromRec(String str){
		// 命令词集合
		List<String> commandList = new ArrayList<>();
		String regex1 = "(?<=【结果】).*?(?=【置信度】)";
		Pattern pattern1 = Pattern.compile(regex1);
		Matcher matcher1 = pattern1.matcher(str);
		while (matcher1.find())
		{
			commandList.add(matcher1.group(0).trim());
		}
		// 置信度集合
		List<Integer> scoreList = new ArrayList<>();
		String regex2 = "(?<=【置信度】).*?(?=[\\n])";
		Pattern pattern2 = Pattern.compile(regex2);
		Matcher matcher2 = pattern2.matcher(str);
		while (matcher2.find())
		{
			String scoreStr = matcher2.group(0).trim();
			scoreList.add(Integer.parseInt(scoreStr));
		}

		//取出置信度最高的命令词
		if(scoreList.size() == 0){ // 无匹配时
			return str;
		}
		Integer scoreMax = Collections.max(scoreList);
		int index = scoreList.indexOf(scoreMax);
		String command = commandList.get(index);
		showTip("最大置信度的命令为：" + command + ",置信度为："+scoreMax);

		return command;
	}


	/**
	 * 初始化权限事件
	 */
	private void initPermission() {
		//检查权限
		String[] permissions = CheckPermissionUtils.checkPermission(this);
		if (permissions.length == 0) {
			//权限都申请了
			//是否登录
		} else {
			//申请权限
			ActivityCompat.requestPermissions(this, permissions, 100);
		}
	}

}
