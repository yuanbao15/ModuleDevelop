package com.U8;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;

public class Loger {

	public static void disk_log(String caption, String log,String moduleName) {
		_disk_log(caption, log.getBytes(),moduleName);
	}

	public static void disk_log(String caption, byte[] buffer,String moduleName) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < buffer.length; i++) {
			sb.append(String.format("%02X ", buffer[i]));
			if ((i + 1) % 8 == 0 && (i + 1) % 16 != 0) {
				sb.append("\t\t");
			}
			if ((i + 1) % 16 == 0) {
				sb.append("\r\n");
			}
		}
		sb.append("\r\n");
		disk_log(caption, sb.toString(), moduleName);
	}


	public static void disk_log(String caption, byte[] buffer,int buffer_len,String moduleName) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < buffer_len; i++) {
			sb.append(String.format("%02X ", buffer[i]));
			if ((i + 1) % 8 == 0 && (i + 1) % 16 != 0) {
				sb.append("\t\t");
			}
			if ((i + 1) % 16 == 0) {
				sb.append("\r\n");
			}
		}
		sb.append("\r\n");
		disk_log(caption, sb.toString(), moduleName);
	}

	@SuppressLint("SdCardPath")
	private static void _disk_log(String caption, byte[] buffer,String moduleName) {
		try {
			String path = "/sdcard/fn_log/"+moduleName;
			java.io.File file = new java.io.File(path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					return;
				}
			}
			/*
			 * path = path+"/u7"; file = new java.io.File(path); if
			 * (!file.exists()) { if (!file.mkdirs()) { return; } }
			 */
			String filename = path
					+ "/"
					+ (new java.text.SimpleDateFormat("yyyy-MM-dd"))
							.format(new java.util.Date()) + ".txt";
			/*
			 * while(true) { filename = path + "/" + (new
			 * java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")) .format((new
			 * java.util.Date()) ) + ".txt"; break; }
			 */
			file = new java.io.File(filename);
			if (!file.exists()) {
				if (!file.createNewFile()) {
					return;
				}
			}

			if (!file.canWrite()) {
				if (!file.setWritable(true)) {
					return;
				}
			}
			java.io.FileOutputStream fos = new java.io.FileOutputStream(file,true);
			fos.write(((new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
					.format(new java.util.Date()) + "==>\r\n").getBytes());
			fos.write((caption + "\r\n").getBytes());
			fos.write(buffer);
			fos.write(("\r\n").getBytes());
			fos.flush();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void out_log(String caption, String log) {

		Log.d(caption, log);
	}

	public static void out_log(String caption, byte[] buffer,int buffer_len) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (i = 0; i < buffer_len; i++) {
			sb.append(String.format("%02X ", buffer[i]));
			/*if ((i + 1) % 8 == 0 && (i + 1) % 16 != 0) {
				sb.append("\t\t");
			}
			if ((i + 1) % 16 == 0) {
				sb.append("\r\n");
			}
			*/
		}
		//sb.append("\r\n");
		Log.d(caption,sb.toString());
	}
	
}
