package com.magicrf.uhfreader;

import com.android.hdhe.uhf.reader.ReaderPower;

public class UhfReaderDevice {
	
  private static ReaderPower devPower = null;
  
  public static void getInstance()
  {
    if (devPower == null)
    {
      try
      {
        devPower = new ReaderPower();
      }
      catch (Exception e)
      {
      }
      devPower.uhfPowerOn();
    }
  }

  public static void powerOn()
  {
	if (devPower != null)
	{
      devPower.uhfPowerOn();
	}
  }
  
  public static void powerOff() {
    if (devPower != null) {
      devPower.uhfPowerOff();
      devPower = null;
    }
  }
}
