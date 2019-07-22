package com.epichust.voice;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.voicedemo.SpeechApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 讯飞语音唤醒识别的类，包含初始化和监听方法
 * Created by yuanbao on 2019/4/18
 */
public class VoiceManager {

    // 弄个单例模式
    private static VoiceManager singleton = null;
    private VoiceManager() {
    }
    public static VoiceManager getInstance() {
        if (singleton == null) {
            singleton = new VoiceManager();
        }
        return singleton;
    }

    public static SpeechApp mApp;
    private Context mContext;

    private String TAG = "IVW";
    // 语音唤醒对象
    public VoiceWakeuper mIvw;
    // 语音识别对象
    public SpeechRecognizer mAsr;
    // 语音听写对象
    public SpeechRecognizer mIat;
    // 设置门限值 ： 门限值越低越容易被唤醒
    private final static int MAX = 3000;
    private final static int MIN = 0;
    private int curThresh = 1650;
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
    private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/msc/grammar";
    // 引擎类型，在此处选择
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private GrammarListener mGrammarListener;
    private WakeuperListener mWakeuperListener;
    private RecognizerListener mRecognizerListener;
    // 用HashMap存储听写结果
    public HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    /**
     * @method    0.初始化
     * @param    
     * 
     * @author  yuanbao
     * @date    2019/4/18 
     */
    public void initVoiceModule(Context context){
        this.mContext = context;
        if(SpeechApp.mApp == null)
        {
            SpeechUtility.createUtility(context, SpeechConstant.APPID+ "=5d11f8d5");
        }
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        // 初始化识别对象--唤醒+识别
        mAsr = SpeechRecognizer.createRecognizer(context, null);
        // 初始化听写对象
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
        // 初始化语法文件
        mCloudGrammar = readFile(context, "wake_grammar_cloud.abnf", "utf-8");
        mLocalGrammar = readFile(context, "wake_grammar_local.bnf", "utf-8");

    }


    /**
     * @method    1.构建语法
     * @param
     *
     * @author  yuanbao
     * @date    2019/4/18
     */
    public void buildGrammar(GrammarListener grammarListener) {
        int ret = 0;
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            // 设置参数
            mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
            mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            // 开始构建语法
            ret = mAsr.buildGrammar("abnf", mCloudGrammar, grammarListener);
            if (ret != ErrorCode.SUCCESS) {
                Log.e(TAG,"语法构建失败,错误码：" + ret);
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
            ret = mAsr.buildGrammar("bnf", mLocalGrammar, grammarListener); // 构建语法，注意这个listener的回调
            if (ret != ErrorCode.SUCCESS) {
                Log.e(TAG,"语法构建失败,错误码：" + ret);
            }
        }

    }

    public void startOneshot(String grammarId, WakeuperListener wakeuperListener) {
        // 记录grammarID
        mLocalGrammarID = grammarId;
        mCloudGrammarID = grammarId;

        mWakeuperListener = wakeuperListener;
        oneshot(); // 开始唤醒监听
    }

    /**
     * @method    2.开启唤醒监听
     * @param
     *
     * @author  yuanbao
     * @date    2019/4/18
     */
    private void oneshot() {
        // 非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
             // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 设置识别引擎
            mIvw.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
            // 设置唤醒资源路径
            final String resPath = ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, "ivw/5d11f8d5.jet");
            mIvw.setParameter(ResourceUtil.IVW_RES_PATH, resPath);

            // 设置持续进行唤醒(一般不要设置持续唤醒，配合UI 唤醒一次 停止一次)
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "0"); // 0,1
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
            mAsr.setParameter(SpeechConstant.VAD_EOS,  "1000");

            // 设置闭环优化网络模式
            // mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");  // 0是关闭，1是开启。

            // mIvw.setParameter(SpeechConstant.IVW_SHOT_WORD, "0");
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
            mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );

            if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
                if (!TextUtils.isEmpty(mCloudGrammarID)) {
                    // 设置云端识别使用的语法id
                    mIvw.setParameter(SpeechConstant.CLOUD_GRAMMAR, mCloudGrammarID);
                    mIvw.startListening(mWakeuperListener);
                } else {
                    Log.e(TAG,"请先构建语法");
                }
            } else {
                if (!TextUtils.isEmpty(mLocalGrammarID)) {
                    // 设置本地识别资源
                    mIvw.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
                    // 设置语法构建路径
                    mIvw.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
                    // 设置本地识别使用语法id
                    mIvw.setParameter(SpeechConstant.LOCAL_GRAMMAR, mLocalGrammarID);
                    mIvw.startListening(mWakeuperListener);
                } else {
                    Log.e(TAG,"请先构建语法");
                }
            }

        } else {
            Log.e(TAG,"唤醒未初始化");
        }
    }
    /**
     * @method    3.停止唤醒监听
     * @param
     *
     * @author  yuanbao
     * @date    2019/4/18
     */
    public void stopOneshot() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.stopListening();
            Log.w(TAG,"YB已停止唤醒监听");
            Toast toast = Toast.makeText(this.mContext, "已停止唤醒监听", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Log.e(TAG,"唤醒未初始化");
        }
    }
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "IatSpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.e(TAG,"初始化失败，错误码：" + code);
            }
        }
    };
    // 听写器的参数初始化
    public void initIatParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");

        // 设置语法构建路径
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置资源路径
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言为中文普通话
        String lag = "mandarin";
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, lag);

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "2500");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS,  "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }
    public String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
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
        Log.e(TAG,"最大置信度的命令为：" + command + ",置信度为："+scoreMax);

        return command;
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
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext,
                RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }


}
