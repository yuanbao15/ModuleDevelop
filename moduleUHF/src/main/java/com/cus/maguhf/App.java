package com.cus.maguhf;

import android.app.Application;

import com.olc.uhf.UhfAdapter;
import com.olc.uhf.UhfManager;

public class App extends Application {
	public static UhfManager mService;
	@Override
	public void onCreate() {
		super.onCreate();
		try {
			mService = UhfAdapter.getUhfManager(this.getApplicationContext());
			if (mService != null) {
				mService.open();
			}
		} catch (Exception e) {
 		}
	}
}
