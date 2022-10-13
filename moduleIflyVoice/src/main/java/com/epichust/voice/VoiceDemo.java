package com.epichust.voice;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.iflytek.cloud.SpeechRecognizer;
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
    private final static String TAG = "DEMO";
    public static UZModuleContext mModuleContext;
    public String mGrammarId = "";
    public ExGrammarListener mGrammarListener; // 语法构建的监听
    public ExWakeuperListener mWakeuperListener; // 语音唤醒的监听器
    public ExRecognizerListener mRecognizerListener; // 也是听写监听器-早期ASR命令词识别的，弃用，后给手动用
    public ExDictateRecognizerListener mDictateRecognizerListener; // 语音听写的监听器
    // 唤醒结果内容
    private String resultString;
    // 识别结果内容
    private String recoString;
    // 是否震动
    private boolean isEnabledVibrate = true;


    // 返回数据给模块调用处
    JSONObject ret = new JSONObject();

    // 手动识别停止标识
    private boolean manualStopFlag = false;
    // 手动识别结果内容
    private String manualRecoString = "";

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
                showTips("语音唤醒初始化成功，唤醒词：元宝哥哥");
            } else {
                Log.e(TAG,"语法构建失败,错误码：" + error.getErrorCode() + "，错误信息：" + error.toString());
                showTips("语音唤醒初始化失败，错误码：" + error.getErrorCode() + "，错误信息：" + error.toString());
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
            // 听写开始时屏蔽唤醒监听
            manager.mIvw.stopListening();
        }
        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.w("IAT","听写-结束讲话");
            showTips("讲话结束，正在识别...");
            // 听写结束后恢复唤醒监听
            manager.startOneshot(mGrammarId, mWakeuperListener);
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
                doLockScreen(recoString);

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
        public void onError(SpeechError speechError) {
            Log.e("IAT", "识别错误："+speechError.getErrorCode()+"，描述："+speechError.getErrorDescription());
            showTips("识别错误："+speechError.getErrorCode()+"，描述："+speechError.getErrorDescription());
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
              if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                  String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                  Log.d(TAG, "session id =" + sid);
              }
        }
    };

    // 语音识别监听器-弃用，后给手动触发用
    class ExRecognizerListener implements RecognizerListener {
        private VoiceManager manager;
        public ExRecognizerListener(VoiceManager manager){
            this.manager = manager;
        }
        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {
//             showTips("当前正在说话，音量大小：" + volume);
//             Log.w("IAT", "当前正在说话，音量大小："+volume);
        }
        @Override
        public void onBeginOfSpeech() {
            // 听写开始时屏蔽唤醒监听
            manager.mIvw.stopListening();
        }
        @Override
        public void onEndOfSpeech() {
            // 听写结束后恢复唤醒监听
            manager.startOneshot(mGrammarId, mWakeuperListener);
        }
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.w("IAT", results.getResultString());
            String resultString = manager.printResult(results);
            Log.w("IAT", resultString);

            if (isLast) {
                manualRecoString += resultString;
                Log.w("IAT", manualRecoString);

                manager.mIatResults.clear(); // 清空这个结果集
                doLockScreen(manualRecoString);

                // 返回接口结果
                try {
                    ret.put("status", true);
                    ret.put("data", manualRecoString);
                    // 模块给个提示有没读到信息
                    showTips(manualRecoString);
                } catch (Exception e) {
                    try {
                        ret.put("status", false);
                        ret.put("errmsg", "模块调用失败："+e.getMessage());
                    }catch (Exception e2){
                    }
                    e.printStackTrace();
                    Log.w("IAT", "模块调用失败："+e.getMessage());
                }finally {
                    mModuleContext.success(ret, false);
                }

                if (!manualStopFlag)
                {
                    // 继续录音识别
                    manager.initIatParam();
                    manager.mIat.startListening(mRecognizerListener);
                }

            }
        }
        @Override
        public void onError(SpeechError speechError) {
            Log.e("IAT", "识别错误："+speechError.getErrorCode()+"，描述："+speechError.getErrorDescription());
            showTips("识别错误："+speechError.getErrorCode()+"，描述："+speechError.getErrorDescription());
            if (!manualStopFlag)
            {
                // 继续录音识别
                manager.initIatParam();
                manager.mIat.startListening(mRecognizerListener);
            }
        }
        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            // 继续录音识别
//            manager.initIatParam();
//            manager.mIat.startListening(mRecognizerListener);
        }
    };

    // 锁屏操作
    private void doLockScreen(String voiceText){
        Log.w("IAT", "YB语音:" + voiceText);
        if (voiceText.contains("锁屏")) {
            if (mDPM.isAdminActive(mDeviceAdminSample)) {// 判断设备管理器是否已经激活
                mDPM.lockNow();// 立即锁屏
                mDPM.resetPassword("123456", 0);
            } else {
                // 跳转设备管理服务进行激活
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        mDeviceAdminSample);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "YB用语音控制设备，请完成授权：");
                startActivity(intent);
            }
        }
    }

    // 点亮屏幕+解锁
    PowerManager mPowerManager; // 电源管理器对象
    KeyguardManager mKeyguardManager; // 键盘锁管理器对象
    KeyguardManager.KeyguardLock mKeyguardLock;
    DevicePolicyManager mDPM; // 获取设备策略服务
    private ComponentName mDeviceAdminSample; // 设备管理组件
    private void initKeyguardAndPower(){
        mPowerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        mKeyguardManager = (KeyguardManager)getContext().getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock"); // 只能禁用滑动锁，不能操作指纹、密码
        mDPM = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminSample = new ComponentName(this.getContext(), AdminReceiver.class);
//        mKeyguardLock.reenableKeyguard(); // 锁键盘

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.w("IAT", "YB唤醒");
                PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
                mWakeLock.acquire(10 * 1000); // 点亮屏幕
                mWakeLock.release(); // 释放
            }
        };
        Thread thread = new Thread(runnable);
//        thread.start();

    }
    private void acquireWakeLock() {
        mKeyguardLock.disableKeyguard(); // 解锁键盘

        // 先亮屏，再解锁
        boolean isScreenOn = mPowerManager.isScreenOn();
        if (!isScreenOn) {
            // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
            PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.FULL_WAKE_LOCK, this.getClass().getName()); // 后边的tag原来是"bright"
            mWakeLock.acquire(10*1000); // 点亮屏幕
            mWakeLock.release(); // 释放
        }

//		setKeyguardEnable(false);

        // 解锁未实现
//        mKeyguardLock.reenableKeyguard(); // 锁键盘
//        mKeyguardLock.disableKeyguard(); // 解锁键盘
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

        // 锁屏管理器初始化
//        initKeyguardAndPower();
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


    /**
     * @method    手动触发语音识别-start
     * @param    
     *
     * @author  yuanbao
     * @date    2019/8/22 
     */
    public void jsmethod_startRecognize(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        VoiceManager manager = VoiceManager.getInstance();

        SpeechRecognizer iat = manager.mIat;
        // 初始化控制结束信号和返回值
        manualStopFlag = false;
        manualRecoString = "";
        ret = new JSONObject();
        // 开启听写功能
        manager.initIatParam();
        int errorNum = manager.mIat.startListening(mRecognizerListener);
        if (errorNum != ErrorCode.SUCCESS) {
            showTips("听写失败,错误码：" + errorNum);
        } else {
            showTips("请开始讲话...");
        }
    }
    /**
     * @method    手动触发语音识别-stop
     * @param
     *
     * @author  yuanbao
     * @date    2019/8/22
     */
    public void jsmethod_stopRecognize(UZModuleContext moduleContext){
        mModuleContext = moduleContext;
        VoiceManager manager = VoiceManager.getInstance();
        showTips("已停止讲话");
        manualStopFlag = true;

        // 停止识别
        manager.mIat.stopListening();
    }
}
