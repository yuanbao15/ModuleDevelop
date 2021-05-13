package com.U8.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.U8.UHFApplication;

import java.util.Map;
import java.util.TreeMap;

public class MusicPlayer {
	private Context mContext;
	private static MusicPlayer sInstance;

	public static class Type {
		public final static int OK = 1;
		public final static int MUSIC_ERROR = 2;
	}

	private SoundPool mSp;
	private Map<Integer, Integer> sSpMap;

	private MusicPlayer(Context context) {
		mContext = context;
		sSpMap = new TreeMap<Integer, Integer>();
		mSp = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		// sSpMap.put(Type.MUSIC_FOCUSED, mSp.load(mContext, R.raw.focused, 1))
		// ;
	}

	static {
		sInstance = new MusicPlayer(UHFApplication.applicationContext);
	}

	public static MusicPlayer getInstance() {
			return sInstance;
	}

	public void play(int type) {
		if (UHFApplication.appGetSoftSound() == 0)
			return;
		if (sSpMap.get(type) == null)
			return;
		mSp.play(sSpMap.get(type), 1, 1, 0, 0, 1);
	}
}