package com.olc.nfcmanager.CPU;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.olc.nfcmanager.ParseListener;
import com.olc.nfcmanager.Utils;

import java.io.IOException;

public class CPUUtils {
	public static final String TAG= "cpu";
	private String mType;
	private String mInfo;
	private static CPUUtils sDesfireEv1;
	private byte[] mExtraId;
	private int mNumberApplication;
	private int oneBlockSize;
	private IsoDep mIsoDep;

	private CPUUtils(){
	}

	public static synchronized CPUUtils getInstance(){
		if (sDesfireEv1 == null){
			sDesfireEv1 = new CPUUtils();
		}
		return sDesfireEv1;
	}

	public  String getType(){
		return mType;
	}
	public  String getInfo(){
		return mInfo;
	}

	public void parseCpuTag(final  Tag tag,final ParseListener parseListener){
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (tag != null){
					mIsoDep = IsoDep.get(tag);
					try {
						mIsoDep.connect();
						mType = "CPU";
						mInfo = getVersion(mIsoDep);
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						if (mIsoDep.isConnected()){
							try {
								mIsoDep.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				parseListener.onParseComplete("");
			}
		}).start();

	}

	public  String getVersion(IsoDep isoDep) throws IOException {
		String info= "";
		byte[] fullResp = new byte[7 + 7 + 14 + 8 + 2];
		int index = 0;

		// 1st frame
		byte[] apdu = new byte[] {
				(byte) 0x90,
				(byte) EV1CardManager.Command.GET_VERSION.getCode(),
				0x00,
				0x00,
				0x00
		};
		byte[] response = isoDep.transceive(apdu);

		String result = Utils.bytesToHexString(response);
		Log.d(TAG," getVersion = "+result);


		System.arraycopy(response, 0, fullResp, 0, response.length);

		index += response.length;

		byte code = response[response.length-1];
		if (Integer.toHexString(code&0xFF).equals("af")) {
			if(response[0] == 4){
				info+="NXP MF3, ";
			}
			int memoryFlag = response[5]/2;
			info+=""+(int)(Math.pow(2,memoryFlag));
			// second frame
			apdu[1] = (byte) EV1CardManager.Command.MORE.getCode();
			response = isoDep.transceive(apdu);
			System.arraycopy(response, 0, fullResp, 7, response.length);
			index += response.length;
			result = Utils.bytesToHexString(response);
			Log.d(TAG," getVersion MORE = "+result);

			code = response[response.length-1];
			if (Integer.toHexString(code&0xFF).equals("af")) {
				// third frame
				response = isoDep.transceive(apdu);
				System.arraycopy(response, 0, fullResp, 14, response.length);
				index += response.length;
				result = Utils.bytesToHexString(response);
				Log.d(TAG," getVersion MORE2 = "+result);
				// postprocessing: doesn't always have a CMAC attached

				byte[] ret = new byte[index];
				System.arraycopy(fullResp, 0, ret, 0, index);
				Log.d(TAG," getVersion = "+ Utils.bytesToHexString(fullResp));
			}
		}

		return info;
	}

	public String[] getApplicationIds(){
		String[] idArray = null;
		byte[] resultByte = null;
		try {
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					resultByte = EV1CardManager.getApplicationIds(mIsoDep);
					if (resultByte.length > 0) {
						byte[] idsByte = new byte[resultByte.length];
						System.arraycopy(resultByte,0,idsByte,0,idsByte.length);
						String str = Utils.bytesToHexString(idsByte);
						return new String[]{str};
					}

				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mIsoDep.isConnected()){
				try {
					mIsoDep.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public String[] getFileIdsOfApplication(byte[] appId){
		String[] idArray = null;
		byte[] resultByte = null;
		try {
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						resultByte = EV1CardManager.getFilesIds(mIsoDep);
						if (resultByte.length > 0) {
							byte[] idsByte = new byte[resultByte.length];
							System.arraycopy(resultByte,0,idsByte,0,idsByte.length);
							String str = Utils.bytesToHexString(idsByte);
							idArray = new String[]{str};
						}

					}

				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mIsoDep.isConnected()){
				try {
					mIsoDep.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return idArray;
	}

	public boolean createFileInApplication(byte[] appId,byte fileId){
		boolean result = false;
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						result =  createStdFile(fileId);
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public boolean deleteFileOfApplication(byte[] appId,byte fileId){
		boolean result = false;
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						return EV1CardManager.deleteFile(mIsoDep,fileId);
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public String readDataFromFileOfApplication(byte[] appId,byte fileId){
		String result = "";
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						if (EV1CardManager.authenticateApplicationByDES(mIsoDep)){
							byte[] response = EV1CardManager.readData(mIsoDep,fileId,new byte[]{0x00,0x00,0x00},new byte[]{0x00,0x00,0x00});
							result = Utils.bytesToHexString(response);
						}
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public String writeDataToFileOfApplication(byte[] appId,byte fileId,byte[] data){
		String result = "";
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						if(EV1CardManager.authenticateApplicationByDES(mIsoDep)){
							String lengthStr = "0"+Integer.toHexString(data.length);
							byte lbyte = Utils.hexString2Bytes(lengthStr)[0];
							result = EV1CardManager.writeData(mIsoDep,fileId,new byte[]{0x00,0x00,0x00},new byte[]{lbyte,0x00,0x00},data);
						}else {
							result = "authenticate App fail";
						}
					} else {
						result = "select App fail";
					}
				}else {
					result = "authenticate PICC fail";
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public boolean createApplicationAndFile(byte[] appId,byte fileId){
		boolean result = false;
//		byte appMasterKeySetting = (byte) 0xEF;
//		byte numOfKeys = (byte) 0x41;
		byte appMasterKeySetting = (byte) 0x0F;
		byte numOfKeys = (byte) 0x01;
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.createApplication(mIsoDep,appId,appMasterKeySetting,numOfKeys)){
						if(EV1CardManager.selectApplication(mIsoDep,appId)){
							EV1CardManager.createStdFile(mIsoDep,fileId);
						}
					} else {
						if(EV1CardManager.selectApplication(mIsoDep,appId)){
							result = EV1CardManager.createStdFile(mIsoDep,fileId);
						}
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}

//		result = EV1CardManager.createApplication(mIsoDep,new byte[]{0x11,0x00,0x00},appMasterKeySetting,numOfKeys);
		return result;
	}
	public String createApplication(byte[] appId){
		String result = "";
//		byte appMasterKeySetting = (byte) 0xEF;
//		byte numOfKeys = (byte) 0x41;
		byte appMasterKeySetting = (byte) 0x0F;
		byte numOfKeys = (byte) 0x01;
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.createApplication(mIsoDep,appId,appMasterKeySetting,numOfKeys)){
						result = "success";
					} else {
						result = "create App fail";
					}
				} else {
					result = "authenticate PICC fail";
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public String getFileSetting(byte[] appId,byte fileId){
		String result = "";
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						byte[] fileSettingByte = EV1CardManager.getFileSettings(mIsoDep,fileId);
						result = Utils.bytesToHexString(fileSettingByte);
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public String getApplicationKeySetting(byte[] appId){
		String result = "";
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					if(EV1CardManager.selectApplication(mIsoDep,appId)){
						byte[] appSettingByte = EV1CardManager.getKeySettings(mIsoDep);
						result = Utils.bytesToHexString(appSettingByte);
					}
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public boolean selectApplication(byte[] appId){
		boolean success = false;

		try {
			if(!EV1CardManager.isHasConnect()){
				EV1CardManager.connectIsoDep(mIsoDep);
			}
			success = EV1CardManager.selectApplication(mIsoDep,appId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	public boolean createStdFile(byte fileId){
		boolean success = false;
		success = EV1CardManager.createStdFile(mIsoDep,fileId);
		return success;
	}


	 public String getType(Intent intent)
	{
		String strret="<UID> :";
		if(intent!=null)
		{
			strret+= Utils.bytesToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
			strret+="\ntype:\n";
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String []strlist= tag.getTechList();
			for(String x:strlist)
			{
				strret+=x+"\n";
			}
			return strret;
		}
		else
			return null;
	}

	 public byte[] getRandomness(Intent intent)
	{
		if(intent!=null)
		{
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if(tag!=null)
			{
				IsoDep nfc = IsoDep.get(tag);
				if(nfc!=null)
				{
					try {
						nfc.connect();
						byte[]data={0x00,(byte)0x84,0x00,0x00,0x04};
						byte []bufread=nfc.transceive(data);
						nfc.close();
						return bufread;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							nfc.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
			}
		}
		return null;

	}

	public String deleteApplication(byte[] appId) {
		String result = "";
		try{
			EV1CardManager.connectIsoDep(mIsoDep);
			if(EV1CardManager.selectPICC(mIsoDep)){
				if (EV1CardManager.authenticatePICC(mIsoDep)){
					result = EV1CardManager.deleteApplication(mIsoDep,appId);
				} else {
					result = "authenticate PICC fail";
				}
			}
			EV1CardManager.closeIsoDep(mIsoDep);
		}catch (Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
