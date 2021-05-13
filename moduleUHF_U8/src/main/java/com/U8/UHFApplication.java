package com.U8;


import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.U8.reader.server.ReaderHelper;
import com.U8.utils.MusicPlayer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UHFApplication extends Application {
	
	private Socket mTcpSocket = null;
	public static Context applicationContext;
	private List<Activity> activities = new ArrayList<Activity>();
	@Override
	public void onCreate() {
		 
		super.onCreate();
		applicationContext=getApplicationContext();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(applicationContext);
		MusicPlayer.getInstance();
		try {
			//实例化ReaderHelper并setContext
			ReaderHelper.setContext(applicationContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());*/
	}
	
	public void addActivity(Activity activity) {
		activities.add(activity);
	}
	@Override
	public void onTerminate() {
		super.onTerminate();
		for (Activity activity : activities) {
			try {
				activity.finish();
			} catch (Exception e) {
				;
			}
		}
		try {
			if (mTcpSocket != null) mTcpSocket.close();
		} catch (IOException e) {
		}
		
		mTcpSocket = null;
		System.exit(0);
	};
	
	public void setTcpSocket(Socket socket) {
		this.mTcpSocket = socket;
	}
	
	static public void saveBeeperState(int state){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putInt("_state", state);
		editor.commit();
	}
	
	static int _SoftSound=2;
	static public int appGetSoftSound(){
		if(_SoftSound==2)
			_SoftSound=getSoftSound();
		return _SoftSound;
	}
	static public int getVeeperState(){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		//SharedPreferences.Editor editor = spf.edit();
		int state = spf.getInt("_state", 0);
		return state;
	}
	
	static public void saveSoftSound(int state){
		_SoftSound=state;
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putInt("_software_sound", state);
		editor.commit();
	}
	
	static public int getSoftSound(){
		SharedPreferences spf =applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		//SharedPreferences.Editor editor = spf.edit();
		int state = spf.getInt("_software_sound", 1);
		return state;
	}
	
	static public void saveSessionState(int state){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putInt("_session", state);
		editor.commit();
	}
	
	static public int getSessionState(){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		//SharedPreferences.Editor editor = spf.edit();
		int state = spf.getInt("_session", 0);
		return state;
	}
	static public void saveFlagState(int state){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = spf.edit();
		editor.putInt("_flag", state);
		editor.commit();
	}
	
	static public int getFlagState(){
		SharedPreferences spf = applicationContext.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		//SharedPreferences.Editor editor = spf.edit();
		int state = spf.getInt("_flag", 0);
		return state;
	}
}
