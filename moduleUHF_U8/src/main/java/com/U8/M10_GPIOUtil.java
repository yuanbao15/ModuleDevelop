package com.U8;

import android.util.Log;

import java.io.FileOutputStream;

public class M10_GPIOUtil {

	public static void ic14443a_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_enable", "1");

	}

	public static void ic14443a_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_enable", "0");

	}

	public static void ic14443a_serial_switch_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_serial_switch", "1");
	}

	public static void ic14443a_serial_switch_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_serial_switch", "0");
	}

	public static void ic15693_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic15693_enable", "0");
	}

	public static void ic15693_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic15693_enable", "1");
	}

	public static void ic15963_serial_switch_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic15693_serial_switch", "0");
	}

	public static void ic15963_serial_switch_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic15693_serial_switch", "1F");
	}

	public static void Infrared_1() {
		writeFile("/syss/platform/devices/odroid-sysfs/infrared_enable_switch", "0");
	}

	public static void Infrared_0() {
		writeFile("/syss/platform/devices/odroid-sysfs/rfid_serial_switch", "0");
	}

	public static void rfid_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/rfid_enable", "1");

	}

	public static void rfid_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/rfid_enable", "0");

	}

	public static void rfid_serial_switch_1() {
		writeFile("/sys/ bus/platform/devices/odroid-sysfs/rfid_serial_switch", "1");
	}

	public static void rfid_serial_switch_0() {
		writeFile("/sys/ bus/platform/devices/odroid-sysfs/rfid_serial_switch", "0");
	}

	public static void r1000_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_enable", "1");

	}

	public static void r1000_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_enable", "0");

	}

	public static void r1000_serial_switch_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_serial_switch", "1");
	}

	public static void r1000_serial_switch_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_serial_switch", "0");
	}

	public static void bardecoder_1() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/bardecoder_enable_switch", "1");
	}

	public static void bardecoder_0() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/bardecoder_enable_switch", "0");
	}

	public static void gps_enable_1() {
		writeFile("/sys/devices/platform/odroid-sysfs/gps_enable", "1");
	}

	public static void gps_enable_0() {
		writeFile("/sys/devices/platform/odroid-sysfs/gps_enable", "0");
	}

	public static void gps_serial_switch_1() {
		writeFile("/sys/devices/platform/odroid-sysfs/gps_enable_switch", "1");
	}

	public static void gps_serial_switch_0() {
		writeFile("/sys/devices/platform/odroid-sysfs/gps_enable_switch", "0");
	}

	private static boolean writeFile(String fileName, String writestr) {
		try {
			// Process su = Runtime.getRuntime().exec("su"); // /system/bin/
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = writestr.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("M10_GPIO", e.getMessage());
			return false;
		}
		return true;
	}
}
