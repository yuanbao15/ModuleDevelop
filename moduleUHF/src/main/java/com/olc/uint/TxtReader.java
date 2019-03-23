package com.olc.uint;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class TxtReader {
	/**
	 * @param inputStream
	 * @return
	 */
	public static String getString(InputStream inputStream) {
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(inputStream, "GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(inputStreamReader);
		StringBuffer sb = new StringBuffer("");
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	public static String getString(String filepath) {
		File file = new File(filepath);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return getString(fileInputStream);
	}
	public static void saveData2File(String scandata, String filepath,
			String FOLDER_PATH) {
		File file = new File(FOLDER_PATH);
		if (!file.exists()) {
			try {
				file.mkdir();
			} catch (Exception e) {
			}
		}
		File dir = new File(filepath);
		try {
			dir.createNewFile();
		} catch (Exception e) {
		}
		byte[] data = scandata.getBytes();
		try {
			RandomAccessFile rf = new RandomAccessFile(filepath, "rw");
			Log.e("FILE:", filepath);
			rf.seek(rf.length());
			rf.write(data);
			rf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
