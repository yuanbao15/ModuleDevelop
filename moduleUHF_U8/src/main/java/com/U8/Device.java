package com.U8;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Device {

	private String model;
	private String serialPort;
	private int baudRate;
	private boolean enable;
	private List<String> gpios = new ArrayList<>();
	private Map<String, String> mPowerOnMap;
	private Map<String, String> mPowerOffMap;

	public Map<String, String> getPowerOnList() {
		return mPowerOnMap;
	}

	public void setPowerOnList(Map<String, String> powerOnList) {
		this.mPowerOnMap = powerOnList;
	}

	public Map<String, String> getPowerOffList() {
		return mPowerOffMap;
	}

	public void setPowerOffList(Map<String, String> powerOffList) {
		this.mPowerOffMap = powerOffList;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}


	public List<String> getGpios() {
		return this.gpios;
	}

	public void setGpios(List<String> gpioList) {
		gpios.addAll(gpioList);

	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	@Override
	public String toString() {
		return "Allocation [model=" + model + ", serialPort=" + serialPort + ", baudRate=" + baudRate + ", btnList=" + gpios + "]";
	}

	public boolean powerOn() {
		try {
			for (Entry<String, String> entry : this.mPowerOnMap.entrySet()) {
				dealGPIO(entry.getKey(), entry.getValue());
				Log.e("see", "powerOn  entry.getKey()=  "+entry.getKey()+" entry.getValue() = "+entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	public boolean powerOff() {
		try {
			for (Entry<String, String> entry : this.mPowerOffMap.entrySet()) {
				dealGPIO(entry.getKey(), entry.getValue());
				Log.e("see", "powerOff  entry.getKey()=  "+entry.getKey()+" entry.getValue() = "+entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	private void dealGPIO(String gpioPort, String gpio) {

		if (gpioPort.equals("ic14443a_enable")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.ic14443a_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.ic14443a_0();
			}
		} else if (gpioPort.equals("ic14443a_sw")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.ic14443a_serial_switch_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.ic14443a_serial_switch_0();
			}
		} else if (gpioPort.equals("Infrared")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.Infrared_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.Infrared_0();
			}
		} else if (gpioPort.equals("rfid_enable")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.rfid_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.rfid_0();
			}
		} else if (gpioPort.equals("rfid_sw")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.rfid_serial_switch_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.rfid_serial_switch_0();
			}
		} else if (gpioPort.equals("r1000_enable")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.r1000_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.r1000_0();
			}
		} else if (gpioPort.equals("r1000_sw")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.r1000_serial_switch_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.r1000_serial_switch_0();
			}
		} else if (gpioPort.equals("bardecoder")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.bardecoder_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.bardecoder_0();
			}
		} else if (gpioPort.equals("ic15693_enable")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.ic15693_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.ic15693_0();
			}
		} else if (gpioPort.equals("ic15693_sw")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.ic15963_serial_switch_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.ic15963_serial_switch_0();
			}
		} else if (gpioPort.equals("gps_enable")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.gps_enable_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.gps_enable_0();
			}
		} else if (gpioPort.equals("gps_sw")) {
			if (gpio.equals("1")) {
				M10_GPIOUtil.gps_serial_switch_1();
			} else if (gpio.equals("0")) {
				M10_GPIOUtil.gps_serial_switch_0();
			}
		}
		/************************ P01  *********************************************/
		if (gpioPort.equals("gpio898")) {
			if (gpio.equals("1")) {
				P01_GPIOUtil.gpio898_1();
			} else if (gpio.equals("0")) {
				P01_GPIOUtil.gpio898_0();
			}
		} else if(gpioPort.equals("gpio0")){
			if (gpio.equals("1")) {
				P01_GPIOUtil.gpio0_1();
			} else if (gpio.equals("0")) {
				P01_GPIOUtil.gpio0_0();
			}
		}else if(gpioPort.equals("gpio899")){
			if (gpio.equals("1")) {
				P01_GPIOUtil.gpio898_1();
			} else if (gpio.equals("0")) {
				P01_GPIOUtil.gpio898_0();
			}
		}else if (gpioPort.equals("gpio909")) {
			if (gpio.equals("1")) {
				P01_GPIOUtil.gpio909_1();
			} else if (gpio.equals("0")) {
				P01_GPIOUtil.gpio909_0();
			}
		} else if (gpioPort.equals("gpio90")) {
			if (gpio.equals("1")) {
				P01_GPIOUtil.gpio90_1();
			} else if (gpio.equals("0")) {
				P01_GPIOUtil.gpio90_0();
			}
		}
		/************** M96 **************************************/
		if (gpioPort.equals("gpio912")) {
			if (gpio.equals("1")) {
				M96_GPIOUtil.gpio912_1();
			} else if (gpio.equals("0")) {
				M96_GPIOUtil.gpio912_0();
			}
		} else if (gpioPort.equals("gpio914")) {
			if (gpio.equals("1")) {
				M96_GPIOUtil.gpio914_1();
			} else if (gpio.equals("0")) {
				M96_GPIOUtil.gpio914_0();
			}
		} else if (gpioPort.equals("gpio918")) {
			if (gpio.equals("1")) {
				M96_GPIOUtil.gpio918_1();
			} else if (gpio.equals("0")) {
				M96_GPIOUtil.gpio918_0();
			}
		} else if (gpioPort.equals("gpio928")) {
		if (gpio.equals("1")) {
			M96_GPIOUtil.gpio928_1();
		} else if (gpio.equals("0")) {
			M96_GPIOUtil.gpio928_0();
		}
	}

	}
}