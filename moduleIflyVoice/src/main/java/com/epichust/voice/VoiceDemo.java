package com.epichust.voice;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by yuanbao on 2019/4/18
 */
public class VoiceDemo extends UZModule {
    private String TAG = "DEMO";
    public static UZModuleContext mModuleContext;
    public String mGrammarId = "";
    public ExGrammarListener mGrammarListener; // 语法构建的监听
    public ExWakeuperListener mWakeuperListener; // 语音唤醒的监听器
    public ExRecognizerListener mRecognizerListener; // 也是听写监听器-早期ASR命令词识别的，弃用
    public ExDictateRecognizerListener mDictateRecognizerListener; // 语音听写的监听器
    // 唤醒结果内容
    private String resultString;
    // 识别结果内容
    private String recoString;
    // 是否震动
    private boolean isEnabledVibrate = true;
    // 返回数据给模块调用处
    JSONObject ret = new JSONObject();

    public VoiceDemo(UZWebView webView) {
        super(webView);
    }

    // 构建语法监听器
    class ExGrammarListener implements GrammarListener{
        private VoiceManager manager;
        public ExGrammarListener(VoiceManager manager){
            this.manager = manager;
        }

        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mGrammarId = grammarId;
                Log.w(TAG,"语法构建成功：" + grammarId);
                // 语法构建完成后开始监听唤醒+识别
                manager.startOneshot(mGrammarId, mWakeuperListener);
                showTips("语音唤醒初始化成功，唤醒词：一汽锡柴");
            } else {
                Log.e(TAG,"语法构建失败,错误码：" + error.getErrorCode());
                showTips("语音唤醒初始化失败，错误码：" + error.getErrorCode());
            }
        }
    }
    // 语音唤醒监听器
    class ExWakeuperListener implements WakeuperListener{
        private VoiceManager manager;
        public ExWakeuperListener(VoiceManager manager){
            this.manager = manager;
        }
        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object = new JSONObject(text);
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
                resultString = buffer.toString();
                ret.put("wakeupWordInfo",resultString);
                // yb-test
                String wakeupWord = object.optString("keyword");
                Log.w(TAG,"唤醒成功：" + wakeupWord);
                Log.w(TAG,"唤醒词详情：" + resultString);
                if (isEnabledVibrate) {
                    // 震动一下
                    TipHelper.vibrate(getContext(), new long[]{100, 200, 100, 200}, false);
                    // 屏幕若是熄的需要亮屏
                    acquireWakeLock();
                }

                // yb-唤醒后再开启识别
                // 开启听写功能
                manager.initIatParam();
                int errorNum = manager.mIat.startListening(mDictateRecognizerListener);
                if (errorNum != ErrorCode.SUCCESS) {
                    Log.e(TAG,"听写失败,错误码：" + errorNum);
                }
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
        }

        @Override
        public void onError(SpeechError error) {
            Log.e(TAG, error.getPlainDescription(true));
            // 继续唤醒监听
            manager.startOneshot(mGrammarId, mWakeuperListener);
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            Log.w(TAG, "识别返回1 "+"eventType:"+eventType+ " arg1:"+isLast + " arg2:" + arg2);
            // 识别结果...
            // 继续唤醒监听
            manager.startOneshot(mGrammarId, mWakeuperListener);
        }

        @Override
        public void onVolumeChanged(int i) {
        }
    }

    class ExDictateRecognizerListener implements RecognizerListener {
        private VoiceManager manager;
        public ExDictateRecognizerListener(VoiceManager manager){
            this.manager = manager;
        }
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.w("IAT","听写-开始讲话");
            showTips("已唤醒，请讲话...");
        }
        @Override
        public void onError(SpeechError error) {
            Log.w(TAG,error.getPlainDescription(true));
        }
        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.w("IAT","听写-结束讲话");
            showTips("讲话结束，正在识别...");
        }
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
			Log.w("IAT", results.getResultString());
			// 展示听写结果
			recoString = manager.printResult(results);

			if (isLast) {
				Log.w("IAT", recoString);
                // 模块给个提示有没读到信息
                showTips(recoString);
                manager.mIatResults.clear(); // 清空这个结果集

                // 返回接口结果
                try {
                    ret.put("status", true);
                    ret.put("data", recoString);
                } catch (Exception e) {
                    try {
                        ret.put("status", false);
                        ret.put("errmsg", "模块调用失败："+e.getMessage());
                    }catch (Exception e2){
                    }
                    e.printStackTrace();
                }finally {
                    mModuleContext.success(ret, false);
                }
			}
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            // showTip("当前正在说话，音量大小：" + volume);
            // Log.w("IAT", "当前正在说话，音量大小："+volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
              if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                  String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                  Log.d(TAG, "session id =" + sid);
              }
        }
    };

    // 语音识别监听器-弃用
    class ExRecognizerListener implements RecognizerListener {
        private VoiceManager manager;
        public ExRecognizerListener(VoiceManager manager){
            this.manager = manager;
        }
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

    // 点亮屏幕+解锁
    private void acquireWakeLock() {
//        // 解锁未实现
//        // 键盘锁管理器对象
//        KeyguardManager mKeyguardManager= (KeyguardManager)getContext().getSystemService(Context.KEYGUARD_SERVICE);
//        KeyguardManager.KeyguardLock mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock");
//        mKeyguardLock.disableKeyguard();

        // 亮屏
        // 电源管理器对象
        PowerManager mPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getName());
        mWakeLock.acquire(10*1000);
        mWakeLock.release();

		setKeyguardEnable(false);
    }

    private void setKeyguardEnable(boolean enable) {
        if (!enable) {
            getContext().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            return;
        }
        getContext().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    /**
     * @method    语音唤醒+听写返回
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/4/18 
     */
    public void jsmethod_startOneshot(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        VoiceManager manager = VoiceManager.getInstance();
        mGrammarListener = new ExGrammarListener(manager);
        mWakeuperListener = new ExWakeuperListener(manager);
        mRecognizerListener = new ExRecognizerListener(manager);
        mDictateRecognizerListener = new ExDictateRecognizerListener(manager);
        // 初始化
        manager.initVoiceModule(this.getContext());
        // 构建语法
        manager.buildGrammar(mGrammarListener);

        // 构建语法开启监听之后的回调中去返回数据
    }

    /**
     * @method    停止语音唤醒的监听
     * @param
     *
     * @author  yuanbao
     * @date    2019/7/9
     */
    public void jsmethod_stopOneshot(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        VoiceManager manager = VoiceManager.getInstance();
        manager.stopOneshot();
        manager = null;
    }

    // toast弹窗提示信息
    private void showTips(String str){
        Toast toast = Toast.makeText(mModuleContext.getContext(), str, Toast.LENGTH_SHORT);
        toast.show();
    }
}
