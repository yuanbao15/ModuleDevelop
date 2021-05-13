package com.U8.model;

/**
 * 保存读到的设置参数,各个模块不同可加参数
 * 
 * @author 07
 * 
 */
public class SettingParams {
	private int TXPower;
	private int detectionTemperature;// 模块温度
	private byte btMode;// 蜂鸣器状态

	public int getTXPower() {
		return TXPower;
	}

	public void setTXPower(int tXPower) {
		TXPower = tXPower;
	}

	public int getDetectionTemperature() {
		return detectionTemperature;
	}

	public void setDetectionTemperature(int detectionTemperature) {
		this.detectionTemperature = detectionTemperature;
	}

	public byte getBtMode() {
		return btMode;
	}

	public void setBtMode(byte btMode) {
		this.btMode = btMode;
	}
}
