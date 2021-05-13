package com.U8.utils;

import android.os.Environment;

import com.U8.reader.model.InventoryBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtil {
	public static boolean saveInventoryData(List<InventoryBuffer.InventoryTagMap> data) { //保存盘询数据
		FileOutputStream fos = null;
		try {
			Date time = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss",Locale.CHINA);
			String dateString = formatter.format(time);
			File path = Environment.getExternalStorageDirectory();
			File file = new File(path + "/fn_log/U8/" + dateString + System.currentTimeMillis() + ".csv");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			String header="ID,EPC,PC,Count,RSSI,RISS\r\n";
			fos.write(header.getBytes());
			for(int i=0;i<data.size();i++){
				String ID=String.valueOf(i+1);
				String epc=data.get(i).strEPC;
				String PC=data.get(i).strPC;
				int Count=data.get(i).nReadCount;
				String RSSI=(Integer.parseInt(data.get(i).strRSSI) - 129) +"";
				String RISS=data.get(i).strFreq;
				String datas=ID+","+epc+","+PC+","+Count+","+RSSI+","+RISS+"\r\n";
				fos.write(datas.getBytes());
				fos.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return true;

	}
}
