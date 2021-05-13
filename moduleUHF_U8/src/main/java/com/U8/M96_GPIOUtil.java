package com.U8;

import java.io.FileOutputStream;

public class M96_GPIOUtil {

	public static void gpio912_1() {
		writeFile("/sys/class/gpio/gpio912/value", "1");
	}

	public static void gpio912_0() {
		writeFile("/sys/class/gpio/gpio912/value", "0");
	}

	public static void gpio914_1() {
		writeFile("/sys/class/gpio/gpio914/value", "1");
	}

	public static void gpio914_0() {
		writeFile("/sys/class/gpio/gpio914/value", "0");
	}

	public static void gpio918_1() {
		writeFile("/sys/class/gpio/gpio918/value", "1");
	}

	public static void gpio918_0() {
		writeFile("/sys/class/gpio/gpio918/value", "0");
	}

	public static void gpio928_1() {
		writeFile("/sys/class/gpio/gpio928/value", "1");
	}

	public static void gpio928_0() {
		writeFile("/sys/class/gpio/gpio928/value", "0");
	}

	private static void writeFile(String fileName, String writestr) {
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			byte[] bytes = writestr.getBytes();
			fout.write(bytes);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
