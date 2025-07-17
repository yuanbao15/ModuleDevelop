package com.android.hdhe.uhf.reader;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class ReaderPower {

  public void uhfPowerOn()
  {
      openPwr("1");
  }

  public void uhfPowerOff()
  {
      openPwr("0");
  }


    //打开电源
    private void openPwr(String pwr){
        writeFile("/dev/l550_card", pwr.toCharArray());
    }

    private int writeFile(String filename, char[] data) {
        File file;
        BufferedWriter writer = null;
        try {
            file = new File(filename);
            if (file.exists()) {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(data);
                writer.close();
                return 0;
            } else {
                Log.e("log", "file not exist: " + filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }
        }
        return 1;
    }

}
