package com.example.uhf.activity;

import android.widget.TextView;

import com.rfid.trans.UHFLib;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Reader {
	public static UHFLib rrlib = new UHFLib();
	public  static void writelog(String log, TextView tvResult)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
		Date date = new Date(System.currentTimeMillis());
		String textlog = simpleDateFormat.format(date)+" "+log;
		tvResult.setText(textlog);
	}
}
