package com.epichust.nfc;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.olc.nfcmanager.R;

public class DevBeep 
{
	private static SoundPool soundPool=null;
	private static int sound_errID=0;
	private static int sound_okID=0;
	private static int stream_beepID=0;
	private static boolean bRunning;
	public static  void init(Context ct)
	{
		bRunning = false; 
		//if(soundPool == null)
		{
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
			sound_okID = soundPool.load(ct, R.raw.beep_ok,1);
		   //sound_okID = soundPool.load("/sdcard/beep_ok.wav", 1);
			
			sound_errID = soundPool.load(ct, R.raw.beep_err, 1);
		}
	}
	public static void release()
	{
		if(stream_beepID >0)
			soundPool.stop(stream_beepID);
		soundPool.unload(sound_errID);
		soundPool.unload(sound_okID);
		soundPool.release();
	}
	
	public static void PlayOK()
	{
		if(bRunning)return;
		if(soundPool==null)return;
		bRunning = true;
		if(stream_beepID >0)soundPool.stop(stream_beepID);
		stream_beepID = soundPool.play(sound_okID , 1.0f, 1.0f, 0, 0, 1.0f);
		bRunning = false;
	}	
	public static void PlayErr()
	{
		if(bRunning)return;
		if(soundPool==null)return;
		bRunning = true;
		if(stream_beepID >0)soundPool.stop(stream_beepID);
		stream_beepID =soundPool.play(sound_errID , 1.0f, 1.0f, 0, 0, 1.0f);
		bRunning =false;
	}
}
