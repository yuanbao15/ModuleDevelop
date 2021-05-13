package com.U8.utils;

import android.util.Log;

import java.io.FileOutputStream;

public class M10_GPIO {

	public static void _14443A_PowerOn() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_enable", "1");
		writeFile(
				"/sys/bus/platform/devices/odroid-sysfs/ic14443a_serial_switch",
				"0");
	}

	public static void _14443A_PowerOFF() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/ic14443a_enable", "0");
		writeFile(
				"/sys/bus/platform/devices/odroid-sysfs/ic14443a_serial_switch",
				"0");
	}

	public static void Infrared_PowerOn() {
		writeFile("/syss/platform/devices/odroid-sysfs/infrared_enable_switch",
				"0");
	}

	public static void Infrared_PowerOFF() {
		writeFile("/syss/platform/devices/odroid-sysfs/rfid_serial_switch", "0");
	}

	public static void R1000_PowerOn() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_enable", "1");
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_serial_switch",
				"1");
	}

	public static void R1000_PowerOFF() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_enable", "0");
		writeFile("/sys/bus/platform/devices/odroid-sysfs/r1000_serial_switch",
				"0");
	}

	public static void W433_PowerOn() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/rfid_enable", "1");
		writeFile("/sys/ bus/platform/devices/odroid-sysfs/rfid_serial_switch",
				"0");
	}

	public static void W433_PowerOff() {
		writeFile("/sys/bus/platform/devices/odroid-sysfs/rfid_enable", "0");
		writeFile("/sys/ bus/platform/devices/odroid-sysfs/rfid_serial_switch",
				"0");
	}

	private static void writeF() {

		/*
		 * String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" +
		 * "exit\n"; su.getOutputStream().write(cmd.getBytes());
		 */
	}

	// 写数据
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
