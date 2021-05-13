package com.U8.reader.model;

public class ReaderSetting {

	public byte btReadId;
	public byte btMajor;
	public byte btMinor;
	public byte btIndexBaudrate;
	public byte btPlusMinus;
	public byte btTemperature;
	/**1-4字节*/
	public byte []btAryOutputPower;
	public byte btWorkAntenna;
	public byte btDrmMode;
	public byte btRegion;
	public byte btFrequencyStart;
	public byte btFrequencyEnd;
	public byte btBeeperMode;
	public byte btGpio1Value;
	public byte btGpio2Value;
	public byte btGpio3Value;
	public byte btGpio4Value;
	public byte btAntDetector;
	public byte btMonzaStatus;
	public boolean blnMonzaStore;
	/**固定12字节*/
	public byte []btAryReaderIdentifier;
	public byte btReturnLoss;
	public byte btImpedanceFrequency;
	
	public int nUserDefineStartFrequency;
	public byte btUserDefineFrequencyInterval;
	public byte btUserDefineChannelQuantity;
	public byte btRfLinkProfile;
	public boolean blnSetResult;
	public String strErrorCode;
	
	public ReaderSetting() {
		btReadId = (byte) 0xFF;
		btMajor = 0x00;
		btMinor = 0x00;
		btIndexBaudrate = 0x00;
		btPlusMinus = 0x00;
		btTemperature = 0x00;
		btAryOutputPower = null;
		btWorkAntenna = 0x00;
		btDrmMode = 0x00;
		btRegion = 0x00;
		btFrequencyStart = 0x00;
		btFrequencyEnd = 0x00;
		btBeeperMode = 0x00;
		blnMonzaStore = false;
		btGpio1Value = 0x00;
		btGpio2Value = 0x00;
		btGpio3Value = 0x00;
		btGpio4Value = 0x00;
		btAntDetector = 0x00;
		btMonzaStatus = 0x00;
		btAryReaderIdentifier = new byte[12];
		btReturnLoss = 0x00;
		btImpedanceFrequency = 0x00;
		nUserDefineStartFrequency = 0x00;
		btUserDefineFrequencyInterval = 0x00;
		btUserDefineChannelQuantity = 0x00;
		btRfLinkProfile = 0x00;
	}

}
