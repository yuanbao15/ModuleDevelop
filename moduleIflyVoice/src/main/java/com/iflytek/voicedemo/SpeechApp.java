package com.iflytek.voicedemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.epichust.voice.R;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * @fileName SpeechApp 整个app项目的application，管理所有的activity和service
 * @description 
 * Created by yuanbao on 2019/4/13
 */
public class SpeechApp extends Application {

	public static SpeechApp mApp;
	public static Activity mActivity;


	@Override
	public void onCreate() {
		// 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用半角“,”分隔。
		// 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
		
		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

		SpeechUtility.createUtility(SpeechApp.this, SpeechConstant.APPID+ "=" + getString(R.string.app_id));
			
		// 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
		// Setting.setShowLog(false);
		super.onCreate();
		mApp = this;
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){

			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				Log.d("TEST",activity+"onActivityCreated");
			}

			@Override
			public void onActivityStarted(Activity activity) {
				Log.d("TEST",activity+"onActivityStarted");
			}

			@Override
			public void onActivityResumed(Activity activity) {
				mActivity = activity;
			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		});

	}


	public static Context getAppContext() {
		return mApp;
	}

	public static Resources getAppResources() {
		return mApp.getResources();
	}

	public static Activity getActivity(){
		return mActivity;
	}

}
