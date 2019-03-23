/**
 * APICloud Modules
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the The MIT License (MIT).
 * Please see the license.html included with this distribution for details.
 */
package com.ybUtils;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;

public class JsParamsUtil {
	private static JsParamsUtil instance;

	public static JsParamsUtil getInstance() {
		if (instance == null) {
			instance = new JsParamsUtil();
		}
		return instance;
	}

	public int x(UZModuleContext moduleContext) {
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("x", 0);
		}
		return 0;
	}

	public int y(UZModuleContext moduleContext) {
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("y", 0);
		}
		return 0;
	}

	public int w(UZModuleContext moduleContext, Context context) {
		int defaultValue = getScreenWidth((Activity) context);
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("w", defaultValue);
		}
		return defaultValue;
	}

	public int h(UZModuleContext moduleContext, Context context, UZModule module) {
		int defaultValue = getScreenHeight((Activity) context, module);
		JSONObject rect = moduleContext.optJSONObject("rect");
		if (!moduleContext.isNull("rect")) {
			return rect.optInt("h", defaultValue);
		}
		return defaultValue;
	}

	public String sound(UZModuleContext moduleContext) {
		return moduleContext.optString("sound");
	}

	public boolean saveToAlbum(UZModuleContext moduleContext) {
		return moduleContext.optBoolean("saveToAlbum", false);
	}

	public String saveImgPath(UZModuleContext moduleContext) {
		JSONObject saveImg = moduleContext.optJSONObject("saveImg");
		if (!moduleContext.isNull("saveImg")) {
			return saveImg.optString("path");
		}
		return null;
	}

	public int saveImgW(UZModuleContext moduleContext) {
		int defaultValue = 200;
		JSONObject saveImg = moduleContext.optJSONObject("saveImg");
		if (!moduleContext.isNull("saveImg")) {
			return saveImg.optInt("w", defaultValue);
		}
		return defaultValue;
	}

	public int saveImgH(UZModuleContext moduleContext) {
		int defaultValue = 200;
		JSONObject saveImg = moduleContext.optJSONObject("saveImg");
		if (!moduleContext.isNull("saveImg")) {
			return saveImg.optInt("h", defaultValue);
		}
		return defaultValue;
	}

	public String decodePath(UZModuleContext moduleContext) {
		return moduleContext.optString("path");
	}

	public String encodeContent(UZModuleContext moduleContext) {
		return moduleContext.optString("content");
	}

	public boolean isBar(UZModuleContext moduleContext) {
		String type = moduleContext.optString("type", "qr_image");
		if (type.equals("qr_image")) {
			return false;
		}
		return true;
	}

	private int getScreenWidth(Activity act) {
		DisplayMetrics metric = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metric);
		return UZCoreUtil.pixToDip(metric.widthPixels);
		//====
	}

	private int getScreenHeight(Activity act, UZModule module) {
		DisplayMetrics metric = new DisplayMetrics();
		
		act.getWindowManager().getDefaultDisplay().getMetrics(metric);
		Rect frame = new Rect();
		act.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		return UZCoreUtil.pixToDip(metric.heightPixels);
		
//		WindowManager manager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
//		Display display = manager.getDefaultDisplay();
//		return display.getHeight();
	}
	
	public Bitmap getBitmap(String path) {
		Bitmap bitmap = null;
		InputStream input = null;
		try {
			input = UZUtility.guessInputStream(path);
			bitmap = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}
}
