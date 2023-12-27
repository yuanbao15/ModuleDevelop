package com.olc.nfcmanager2;

import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.util.Log;
import com.olc.nfcmanager2.ParseListener;
import com.olc.nfcmanager2.Utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class I15693Utils
{
	public static final String TAG= "15693";
	private String mType;
	private String mInfo;
	private static I15693Utils sI15693Utils;
	private NfcV mNfcv;
	private byte[] mExtraId; // UID的byte数组形式
	private int mNumberBlocks;
	private String mAFI;
	private String mDSFID;
	private int oneBlockSize;
	private final ExecutorService mPool;

	private I15693Utils(){
		mPool = Executors.newFixedThreadPool(2);
	}

	public static synchronized I15693Utils getInstance(){
		if (sI15693Utils == null){
			sI15693Utils = new I15693Utils();
		}
		return sI15693Utils;
	}

	public  String getType(){
		return mType;
	}
	public  String getInfo(){
		return mInfo;
	}
	public int getBlockCount() {
		return mNumberBlocks;
	}
	public String getAFI(){
		return  mAFI;
	}
	boolean isParsing = false;
	public void parse15693Tag(final Tag tag, final byte[] extraId, final ParseListener listener){
		mPool.execute(new Runnable() {
			@Override
			public void run() {
				if (tag != null){
					try{
						mNfcv = NfcV.get(tag);
						mExtraId = extraId;
						getSystemInformation(listener);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void getSystemInformation( final ParseListener listener){
		String result = "";
		if(mNfcv!=null && !isParsing){
			try {
				isParsing = true;
				mNfcv.connect();
				byte[]bufwrite=new byte[10];
				bufwrite[0]=0x22;
				bufwrite[1]=0x2B;
				System.arraycopy(mExtraId,0,bufwrite,2,8);
				byte[] bufread=mNfcv.transceive(bufwrite);
				result = Utils.bytesToHexString(bufread);
				Log.d(TAG," info legth="+result);
				try{
					byte manuID = mExtraId[6];
					if (manuID == 4) {
						Log.d(TAG," info length="+bufread.length);
						mNumberBlocks = bufread[12]+1;
						oneBlockSize = bufread[13]+1;
						mDSFID = Utils.bytesToHexString(new byte[]{bufread[10]});
						mAFI = Utils.bytesToHexString(new byte[]{bufread[11]});
						if (mExtraId[5] == 1) {
							mType = "NXP SL2ICS20";
						} else {
							mType = "NXP 15693";
						}
						listener.onParseComplete( mNumberBlocks+","+oneBlockSize);
					} else if (manuID == 2) {
						mNumberBlocks = 2048;
						oneBlockSize = 4;
						mDSFID = Utils.bytesToHexString(new byte[]{bufread[10]});
						mAFI = Utils.bytesToHexString(new byte[]{bufread[11]});
						mType = "Apresys M24LR64E-R";
						listener.onParseComplete( mNumberBlocks+","+oneBlockSize);
					}else if (manuID == 8) {
						mNumberBlocks = 250;
						oneBlockSize = 8;
						mDSFID = Utils.bytesToHexString(new byte[]{bufread[10]});
						mAFI = Utils.bytesToHexString(new byte[]{bufread[11]});
						mType = "MB89R118C";
						listener.onParseComplete( mNumberBlocks+","+oneBlockSize);
					} else {
						mNumberBlocks = 64;
						oneBlockSize = 4;
						mDSFID = Utils.bytesToHexString(new byte[]{bufread[10]});
						mAFI = Utils.bytesToHexString(new byte[]{bufread[11]});
						mType = "UnKnow";
						listener.onParseComplete( mNumberBlocks+","+oneBlockSize);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				/* 15693  nxp   000f 30b89f41000104e0 00001b0301
				   uid = 30b89f41000104e0
				   00  reponse flag
				   04 : NXp ; 01  IC NO
				   0f  00001111  ,support DSFID,AFI,store memmory,IC NO
					byte[12] = 1b    27+1=28 blocks
					byte[13] = 03    one block is 4 byte.

				   Apresys
				    000b 6682c49ceb5c02e0 ff005e
				    02 5c
				    uid = 6682c49ceb5c02e0
				    0b  00001011  dont support store memmory, but support DSFID,AFI,IC NO.so can not get memory info.
					02 Apresys;  IC-NO 5e

					see ISO/IEC 7816-6

					Code 0x01: Motorola
					Code 0x02: ST Microelectronics
					Code 0x03: Hitachi
					Code 0x04: NXP Semiconductors
					Code 0x05: Infineon Technologies
					Code 0x06: Cylinc
					Code 0x07: Texas Instruments Tag-it™
					Code 0x08: Fujitsu Limited
					Code 0x09: Matsushita Electric Industrial
					Code 0x0A: NEC
					Code 0x0B: Oki Electric
					Code 0x0C: Toshiba
					Code 0x0D: Mitsubishi Electric
					Code 0x0E: Samsung Electronics
					Code 0x0F: Hyundai Electronics
					Code 0x10: LG Semiconductors
					Code 0x16: EM Microelectronic-Marin
					Code 0x1F: Melexis
					Code 0x2B: Maxim
					Code 0x33: AMIC
				*/

			} catch (IOException e) {
				try {
					mNfcv.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}finally {
				isParsing = false;
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	 public byte[] getUID(Intent intent){
		if(intent!=null)
		return mExtraId;
		else
			return null;
	}
	 public String readSingleBlock(int address){

		 String result = "";
		if((mNfcv!=null)&&(address>=0)){
			try {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mNfcv.connect();
				byte[] bufwrite=new byte[11];
				bufwrite[0]=0x22;
				bufwrite[1]=0x20;
				System.arraycopy(mExtraId,0,bufwrite,2,8);
				bufwrite[10]=(byte)address;
				byte[] bufread = mNfcv.transceive(bufwrite);
				byte[] bytes = new byte[4];
				System.arraycopy(bufread,1,bytes,0,4);
				// YB-默认传递16进制回去，如果需要转换再自行转换UTF8
				result+= " block "+address+"       -hex:  "+ convertBytes2HexStr(bytes)+"\n";
			} catch (IOException e) {
				try {
					mNfcv.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return null;
			}catch (Exception e){
				try {
					mNfcv.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return null;
			}finally {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		   return result;
	}
	 public boolean writeSingleBlock(int address,byte[]data){
		if((data!=null)&&(mNfcv!=null)&&(address>=0)){

			try {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mNfcv.connect();
				byte[] bufwrite =new byte[15];
				bufwrite[0]=0x22;
				bufwrite[1]=0x21;
				System.arraycopy(mExtraId,0,bufwrite,2,8);
				bufwrite[10]=(byte)address;
				System.arraycopy(data,0,bufwrite,11,data.length>4?4:data.length);
				if (bufwrite.length < mNfcv.getMaxTransceiveLength()){
					byte[] bufread = mNfcv.transceive(bufwrite);
					if(bufread.length==1)
						return true;
					else
						return false;
				} else {
					Log.d(TAG,"bufwrite.length ="+bufwrite.length+" mNfcv.getMaxTransceiveLength()="+mNfcv.getMaxTransceiveLength());
				}

			} catch (IOException e) {
				try {
					mNfcv.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return false;
			}finally {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	 public String readMultipleBlocks(int start,int blocks){
		if((mNfcv!=null)&&(start>=0)&&(blocks>=0)){
			try {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mNfcv.connect();
				byte[] bufwrite = new byte[12];
				bufwrite[0]=0x22; // flga
				bufwrite[1]=0x23; // command
				System.arraycopy(mExtraId,0,bufwrite,2,8);
				bufwrite[10]=(byte)start; // 起始位
				bufwrite[11]=(byte)(blocks-1); // 块数,因为是从0开始，所以减1
				//				bufwrite[11]=(byte)mNfcv.getMaxTransceiveLength(); // 原有的不成功
				byte[] bufread = mNfcv.transceive(bufwrite);
				String result = "";
				int index = start;
				for (int i = 1; i<bufread.length; i=i+4){
					byte[] bytes = new byte[4];
					System.arraycopy(bufread,i,bytes,0,4);
					result+= " block "+index+"       hex:  "+ convertBytes2HexStr(bytes)+"\n";
					index++;
				}
				return result;
			} catch (IOException e) {
				try {
					mNfcv.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return null;
			}finally {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public boolean writeMultipleBlocks(int start,int blocks,byte[]data){
		if((data!=null)&&(mNfcv!=null)&&(start>=0)&&(blocks>=0))
		{
			try {
				// 先关闭、再连接。解决报错：Only one TagTechnology can be connected at a time.
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				mNfcv.connect();
				int nlen=(blocks)*4; // 块内存放byte的长度，一个块可以放4个byte，即8个16进制字符
				byte[] bufwrite=new byte[12+data.length+2];
				bufwrite[0]=0x22;
				bufwrite[1]=0x24;
				System.arraycopy(mExtraId,0,bufwrite,2,8);
				bufwrite[10]=(byte)start;
				bufwrite[11]=(byte)(blocks-1); // 块数,因为这儿是从0开始，所以减1
				System.arraycopy(data,0,bufwrite,12,data.length>nlen?nlen:data.length);

				if (bufwrite.length <= mNfcv.getMaxTransceiveLength()){
					byte[] bufread = mNfcv.transceive(bufwrite);
					Log.d(TAG,"writeMultipleBlocks "+Utils.bytesToHexString(bufread));
					if(bufread.length==1)
						return true;
					else
						return false;
				} else {
					Log.d(TAG,"writeMultipleBlocks bufwrite.length ="+bufwrite.length+" is not less than mNfcv.getMaxTransceiveLength()="+mNfcv.getMaxTransceiveLength());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					mNfcv.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
				return false;
			}finally {
				if(mNfcv.isConnected()){
					try {
						mNfcv.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}
	 public boolean lockSingleBlock(int address){
		if((mNfcv!=null)&&(address>=0)){
			if(mNfcv!=null)
			{
				try {
					mNfcv.connect();
					byte[]bufwrite=new byte[11];
					bufwrite[0]=0x22;
					bufwrite[1]=0x22;
					System.arraycopy(mExtraId
							,0,bufwrite,2,8);
					bufwrite[10]=(byte)address;
					byte[]bufread=mNfcv.transceive(bufwrite);
					Log.d(TAG,"lockSingleBlock "+ Utils.bytesToHexString(bufread));
					return bufread.length==1?true:false;
				} catch (IOException e) {
					try {
						mNfcv.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					return false;
				}finally {
					if(mNfcv.isConnected()){
						try {
							mNfcv.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		   return false;
	}
	 public String readDSFID(){
		 if(mNfcv!=null){
			 return Utils.bytesToHexString(new byte[]{mNfcv.getDsfId()});
		 }
		return "";
	}

	 public boolean writeDSFID(String  dsfid){
		 if(mNfcv!=null){
			 try {
				 mNfcv.connect();
				 byte[] bufwrite = new byte[11];
				 bufwrite[0]=0x22;
				 bufwrite[1]=0x29;
				 System.arraycopy(mExtraId,0,bufwrite,2,8);
				 bufwrite[10] = Utils.hexString2Bytes(dsfid)[0];;
				 byte[] bufread = mNfcv.transceive(bufwrite);
				 Log.d(TAG,"writeDSFID "+ Utils.bytesToHexString(bufread));
				 return bufread.length == 1 ? true:false;
			 } catch (IOException e) {
				 try {
					 mNfcv.close();
				 } catch (IOException e1) {
					 e1.printStackTrace();
				 }
				 e.printStackTrace();
				 return false;
			 }finally {
				 if(mNfcv.isConnected()){
					 try {
						 mNfcv.close();
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }
			 }
		 }
		   return false;
	}
	 public boolean lockDSFID(){
		 if(mNfcv!=null){
			 try {
				 mNfcv.connect();
				 byte[]bufwrite=new byte[10];
				 bufwrite[0]=0x22;
				 bufwrite[1]=0x2A;
				 System.arraycopy(mExtraId
						 ,0,bufwrite,2,8);
				 byte[]bufread=mNfcv.transceive(bufwrite);
				 Log.d(TAG,"lockDSFID "+ Utils.bytesToHexString(bufread));
				 return bufread.length==1?true:false;
			 } catch (IOException e) {
				 // TODO Auto-generated catch block
				 try {
					 mNfcv.close();
				 } catch (IOException e1) {
					 // TODO Auto-generated catch block
					 e1.printStackTrace();
				 }
				 e.printStackTrace();
				 return false;
			 }finally {
				 if(mNfcv.isConnected()){
					 try {
						 mNfcv.close();
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }
			 }
		 }
		   return false;
	}

	public String readAFI(){
		if(mNfcv!=null){
			return mAFI;
		}
		return "";
	}

	 public boolean writeAFI(String afi){
		 if(mNfcv!=null){
			 try {
				 mNfcv.connect();
				 byte[] bufwrite = new byte[11];
				 bufwrite[0] = 0x22;
				 bufwrite[1] = 0x27;
				 System.arraycopy(mExtraId,0,bufwrite,2,8);
				 bufwrite[10] = Utils.hexString2Bytes(afi)[0];
				 byte[]bufread=mNfcv.transceive(bufwrite);
				 Log.d(TAG,"writeAFI "+ Utils.bytesToHexString(bufread));
				 return bufread.length==1?true:false;
			 } catch (IOException e) {
				 try {
					 mNfcv.close();
				 } catch (IOException e1) {
					 e1.printStackTrace();
				 }
				 return false;
			 } finally {
				 if(mNfcv.isConnected()){
					 try {
						 mNfcv.close();
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }
			 }
		 }
		 return false;
	}
	 public boolean lockAFI(){
		 if(mNfcv!=null){
			 try {
				 mNfcv.connect();
				 byte[]bufwrite=new byte[10];
				 bufwrite[0]=0x22;
				 bufwrite[1]=0x28;
				 System.arraycopy(mExtraId,0,bufwrite,2,8);
				 byte[]bufread = mNfcv.transceive(bufwrite);
				 Log.d(TAG,"lockAFI "+ Utils.bytesToHexString(bufread));
				 return bufread.length==1? true:false;
			 } catch (IOException e) {
				 try {
					 mNfcv.close();
				 } catch (IOException e1) {
					 e1.printStackTrace();
				 }
				 return false;
			 }finally {
				 if(mNfcv.isConnected()){
					 try {
						 mNfcv.close();
					 } catch (IOException e) {
						 e.printStackTrace();
					 }
				 }
			 }
		 }
		 return false;
	}

	/**
	 * @MethodName: convertStr2Hex
	 * @Description: 字符串转化为16进制
	 * @Param str 一般字符串
	 * @Return String 16进制字符串
	 * @Author: yuanbao
	 * @Date: 2023/12/22
	 **/
	public static String convertStr2Hex(String str)
	{
		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;
		for (int i = 0; i < bs.length; i++)
		{
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
			// sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * @MethodName: convertHexStr2Str
	 * @Description: 16进制转化为字符串
	 * @Param hexStr 16进制字符串
	 * @Return String 一般字符串
	 * @Author: yuanbao
	 * @Date: 2023/12/22
	 **/
	public static String convertHexStr2Str(String hexStr)
	{
		hexStr = hexStr.toUpperCase(); // 增加先转换为大写后再去按utf8转字符串
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
		for (int i = 0; i < bytes.length; i++)
		{
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}

	/**
	 * @MethodName: convertBytes2HexStr
	 * @Description: 字节数组转化为16进制
	 * @Param src 字节数组
	 * @Return String 16进制字符串
	 * @Author: yuanbao
	 * @Date: 2023/12/26
	 **/
	public static String convertBytes2HexStr(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 * @MethodName: convertBytes2HexStr
	 * @Description: 16进制转化为字节数组
	 * @Param hexStr 16进制
	 * @Return byte[] 字节数组
	 * @Author: yuanbao
	 * @Date: 2023/12/26
	 **/
	public static byte[] converHexStr2Bytes(String hexStr) {
		if (hexStr == null || hexStr.equals("")) {
			return null;
		}
		hexStr = hexStr.toUpperCase();
		int length = hexStr.length() / 2;
		char[] hexChars = hexStr.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
}
