package com.handheld.uhfdemo1;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.yuanbao.rfid.R;

import java.util.HashMap;
import java.util.Map;


public class Util {

	
	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;
	
	//init sound pool
	public static void initSoundPool(Context context){
		Util.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(1, sp.load(context, R.raw.msg, 1));
	}
	
	//play sound
	public static  void play(int sound, int number){
		AudioManager am = (AudioManager) Util.context.getSystemService(Util.context.AUDIO_SERVICE);
		   //return AlarmManager The largest volume at present
	    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        
		   //return AlarmManager The largest volume at present
	        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        float volumnRatio = audioCurrentVolume/audioMaxVolume;
	        sp.play(1,1,1,0,0,1);//0.5-2.0 speed
	    }
	
}
