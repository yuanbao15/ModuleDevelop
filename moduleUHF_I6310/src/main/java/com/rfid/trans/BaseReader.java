package com.rfid.trans;

import android.os.SystemClock;
import android.util.Log;

public class BaseReader {
	private MessageTran msg =new MessageTran();
	private long maxScanTime = 2000;
	private int[] recvLength=new int[1];
	private byte[]recvBuff=new byte[20000];
	private int logswitch=0;
	private TagCallback callback;
	private void getCRC(byte[] data,int Len)
	{
		 try
		 {
			 int i, j;
			 int current_crc_value = 0xFFFF;
			 for (i = 0; i <Len ; i++)
			 {
			    current_crc_value = current_crc_value ^ (data[i] & 0xFF);
			    for (j = 0; j < 8; j++)
			    {
			        if ((current_crc_value & 0x01) != 0)
			            current_crc_value = (current_crc_value >> 1) ^ 0x8408;
			        else
			            current_crc_value = (current_crc_value >> 1);
			    }
			 }
			 data[i++] = (byte) (current_crc_value & 0xFF);
			 data[i] = (byte) ((current_crc_value >> 8) & 0xFF); 
		 }
		 catch(Exception e)
		 {
			 
		 }
	}
	
	private boolean CheckCRC(byte[] data,int len)
	{
		 try
		 {
			 byte[]daw =new byte[256];
			 System.arraycopy(data, 0, daw, 0, len);
			 getCRC(daw,len);
			 if(0==daw[len+1] && 0==daw[len])
			 {
				 return true;
			 }
			 else
			 {
				 return false;
			 }
		 }
		 catch(Exception e)
		 {
			 return false;
		 }
	 }
	 
	public String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
       try{
    	   if (src == null || src.length <= 0) {
               return null;
           }
           for (int i = offset; i < length; i++) {
               int v = src[i] & 0xFF;
               String hv = Integer.toHexString(v);
               if (hv.length() == 1) {
                   stringBuilder.append(0);
               }
               stringBuilder.append(hv);
           }
           return stringBuilder.toString().toUpperCase();  
       }catch(Exception ex)
       { return null;}
    }

	public byte[] hexStringToBytes(String hexString) {
       try{
    	   if (hexString == null || hexString.equals("")) {  
               return null;  
           }  
           hexString = hexString.toUpperCase();  
           int length = hexString.length() / 2;  
           char[] hexChars = hexString.toCharArray();  
           byte[] d = new byte[length];  
           for (int i = 0; i < length; i++) {  
               int pos = i * 2;  
               d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
           }  
           return d;    
       }catch(Exception ex)
       {return null;}
    }   
    private byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
    } 
    
    public void SetCallBack(TagCallback callback)
    {
    	this.callback = callback;
    }
    public int Connect(String ComPort, int BaudRate, int LogFlag)
	{
	    if(msg.isOpen()) msg.close();
    	logswitch = LogFlag; 
		return msg.open(ComPort, BaudRate);
	}
	public int DisConnect()
	{
		return msg.close();
	}

	private int SendCMD(byte[]CMD)
	{
		if(logswitch==1) Log.d("Send", bytesToHexString(CMD,0,(CMD[0]&255)+1));
		return msg.Write(CMD);
	}
	
	private int GetCMDData(byte[]data,int[]Nlen,int cmd,int endTime)
	{
		byte[]buffer;
		int Count=0;
		byte[]btArray = new byte[2000];
		int btLength=0;
		long beginTime = System.currentTimeMillis();
		try{
			while((System.currentTimeMillis()-beginTime)<endTime)
			{
				SystemClock.sleep(5);
				buffer = msg.Read();
				if(buffer!=null)
				{
					Count = buffer.length;
					if(Count==0)continue;
					byte[] daw = new byte[Count+btLength];
	                System.arraycopy(btArray,0,daw,0,btLength);
	                System.arraycopy(buffer,0,daw,btLength,Count);
	                int index=0;
	                while ((daw.length-index) >4)
	                {
	                	if(((daw[index]&255)>=4)&&((daw[index+2]&255)==cmd))
	                	{
	                		int len = (daw[index] &255);
	                		if(daw.length<(index+len+1))break;
	                		byte[] epcArr = new byte[len+1];
	                		System.arraycopy(daw,index,epcArr,0,epcArr.length);
	                		if(CheckCRC(epcArr,epcArr.length))
	                		{
	                			System.arraycopy(epcArr, 0, data, 0, epcArr.length);
	                			Nlen[0] = epcArr.length+1;
	                			return 0;
	                		}
	                		else
	                		{
	                			index++;
	                		}
	                	}
	                	else
	                	{
	                		index++;
	                	}
	                }
	                if(daw.length>index)
	                {
	                	btLength = daw.length-index;
	                	System.arraycopy(daw,index,btArray,0,btLength);
	                }
	                else
	                {
	                	btLength=0;
	                }
				}
			}
		}catch(Exception e)
		{e.toString();}
		return 0x30;
	}
	String resultError="";
	private int GetInventoryData(byte ComAddr,int cmd,byte[]epcdata,int[]epcNum,int[]dlen,int Scantime)
	{
	    boolean crccheck =false;
		epcNum[0]=0;
		dlen[0]=0;
		byte[]buffer;
		int Count=0;
		byte[]btArray = new byte[2000];
		int btLength=0;
		long beginTime = SystemClock.elapsedRealtime();
		try{
			do{
				SystemClock.sleep(5);
				buffer = msg.Read();
				if(buffer!=null)
				{
					Count = buffer.length;
					if(Count==0)continue;
					byte[] daw = new byte[Count+btLength];
	                System.arraycopy(btArray,0,daw,0,btLength);
	                System.arraycopy(buffer,0,daw,btLength,Count);
	                int index=0;
	                while ((daw.length-index) >5)
	                {
	                	if((ComAddr&255)==255) ComAddr=0;
	                	if(((daw[index]&255)>=5)&&(daw[index+1]==ComAddr)&&((daw[index+2]&255)==cmd))
	                	{
	                		int len = (daw[index] &255);
	                		if(daw.length<(index+len+1))break;
	                		byte[] epcArr = new byte[len+1];
	                		System.arraycopy(daw,index,epcArr,0,epcArr.length);
	                		if(CheckCRC(epcArr,epcArr.length))
	                		{
                                crccheck =true;
	                			int nLen = (epcArr[0]&255) +1;
	                			index +=nLen;
	                			int status = (epcArr[3]&255);
	                			if((status==0x01)||(status==0x02)||(status==0x03)||(status==0x04))
	                			{
	                				int num = (epcArr[5]&255);
	                				if(num>0)
	                				{
	                					int m=6;
	                					for(int nm=0;nm<num;nm++)
										{
											int epclen = (epcArr[m]&255);
											System.arraycopy(epcArr, m, epcdata, dlen[0], epclen+2);
											epcNum[0]++;
											dlen[0]+=(epclen+2);
											if((callback!=null)&&(epclen>0))
											{
												ReadTag tag = new ReadTag();
												tag.antId = (epcArr[4]&255);
												epclen = (epcArr[m]&255);
												byte[]btArr = new byte[epclen];
												System.arraycopy(epcArr, m+1, btArr, 0, btArr.length);
												tag.epcId = bytesToHexString(btArr,0,btArr.length);
												tag.rssi = (epcArr[m+1+epclen]&255);
												callback.tagCallback(tag);
											}
											m=m+2+epclen;
										}


	                				}
	                				if((status==0x01)||(status==0x02))
	                				{
	                					if(epcNum[0]>0)return 0;
		                				else return 1;
	                				}
	                			}
	                			else
	                			{
	                				if(callback!=null)
	                				{
	                					callback.tagCallbackFailed(status);
	                				}
	                				return status;
	                			}
	                		}
	                		else
	                		{

	                			index++;
								if(callback!=null)
								{
									if(crccheck)
									{
										callback.tagCallbackFailed(1);
										//crccheck =false;
									}
									else
									{
										callback.tagCallbackFailed(2);
									}

								}
                                Log.d("errorrinfo","1111");
								crccheck =false;
	                		}
	                	}
	                	else
	                	{
	                		index++;
                            if(callback!=null)
                            {
                                if(crccheck)
                                {
                                    callback.tagCallbackFailed(1);
                                    //crccheck =false;
                                }
                                else
                                {
                                    callback.tagCallbackFailed(2);
                                }
								Log.d("errorrinfo","0000");
                            }
                            crccheck =false;
	                	}
	                }
	                if(daw.length>index)
	                {
	                	btLength = daw.length-index;
	                	System.arraycopy(daw,index,btArray,0,btLength);
	                }
	                else
	                {
	                	btLength=0;
	                }
				}
				else
				{
					SystemClock.sleep(10);
				}
			}while((SystemClock.elapsedRealtime()-beginTime)<(Scantime*100+1000*3));
		}
		catch(Exception e)
		{resultError = e.toString();
			Log.d("error",resultError);
		}
		if(callback!=null)
		{
			callback.tagCallbackFailed(0x30);
		}
		return 0x30;
	}
	
	public int GetReaderInformation(byte[] ComAddr,byte TVersionInfo[],byte ReaderType[],byte TrType[],
			byte band[],byte dmaxfre[],byte dminfre[],byte powerdBm[],byte ScanTime[],byte Ant[],byte BeepEn[],
			byte OutputRep[],byte CheckAnt[])
	{
		byte[]buffer = new byte[5];
		buffer[0]=(byte)(0x04);
		buffer[1]=(byte)ComAddr[0];
		buffer[2]=(byte)(0x21);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x21,1000);
		if(result==0)
		{
			ComAddr[0] = recvBuff[1];
			TVersionInfo[0] = recvBuff[4];
			TVersionInfo[1] = recvBuff[5];
			ReaderType[0] = recvBuff[6];
			TrType[0] = recvBuff[7];
			dmaxfre[0] = (byte)(recvBuff[8]&0x3F);
			dminfre[0] = (byte)(recvBuff[9]&0x3F);
			band[0]=(byte) (((recvBuff[8]&0xC0)>>4) | ((recvBuff[9]&0xC0)>>6));
			powerdBm[0] = recvBuff[10];
			ScanTime[0] = recvBuff[11];
			maxScanTime = (ScanTime[0]&255)*100;
			Ant[0] = recvBuff[12];
			BeepEn[0] = recvBuff[13];
			OutputRep[0] = recvBuff[14];
			CheckAnt[0] = recvBuff[15];
			return 0;
		}
		return 0x30;
	}
	
	public int SetInventoryScanTime(byte ComAddr,byte ScanTime)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=ComAddr;
		buffer[2]=(byte)(0x25);
		buffer[3]=(byte)ScanTime;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x25,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int SetPowerMode(byte ComAddr,byte[] powermode)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=ComAddr;
		buffer[2]=(byte)(0x6B);
		buffer[3]=powermode[0];
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x6B,500);
		if(result==0)
		{
			if(powermode[0]==0)
			{
				powermode[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	
	public int Inventory_G2(byte ComAddr,byte QValue,byte Session,byte AdrTID,byte LenTID,byte Target,
			byte Ant,int Scantime,byte pOUcharIDList[],int pOUcharTagNum[],int pListLen[])
	{
		byte[]buffer=null;
		if(LenTID==0)
		{
			buffer = new byte[10];
			buffer[0] =(byte)0x09;
			buffer[1] =(byte)ComAddr;
			buffer[2] =(byte)0x01;
			buffer[3] =(byte)QValue;
			buffer[4] =(byte)Session;
			buffer[5] =(byte)Target;
			buffer[6] =(byte)Ant;
			buffer[7] =(byte)Scantime;
		}
		else
		{
			buffer = new byte[12];
			buffer[0] =(byte)0x0B;
			buffer[1] =(byte)ComAddr;
			buffer[2] =(byte)0x01;
			buffer[3] =(byte)QValue;
			buffer[4] =(byte)Session;
			buffer[5] =(byte)AdrTID;
			buffer[6] =(byte)LenTID;
			buffer[7] =(byte)Target;
			buffer[8] =(byte)Ant;
			buffer[9] =(byte)Scantime;
		}
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		return GetInventoryData(ComAddr,0x01,pOUcharIDList,pOUcharTagNum,pListLen,Scantime);
	}

	public String SingleInventory_G2(byte ComAddr)
	{
		byte[]buffer = new byte[5];
		buffer[0]=(byte)(0x04);
		buffer[1]=ComAddr;
		buffer[2]=(byte)(0x0F);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x0F,1000);
		if(result==0)
		{
			if(recvBuff[3]==1)
			{
				if(recvBuff[5]==1)
				{
					int epclen = recvBuff[6];
					ReadTag tag = new ReadTag();
					tag.antId = (recvBuff[4]&255);
					byte[]btArr = new byte[epclen];
					System.arraycopy(recvBuff, 7, btArr, 0, btArr.length);
					tag.epcId = bytesToHexString(btArr,0,btArr.length);
					tag.rssi = (recvBuff[7+epclen]&255);
					if((callback!=null)&&(epclen>0))
					{
						callback.tagCallback(tag);
					}
					return tag.epcId;
				}

			}
			return null;
		}
		return null;
	}
	public int SetRfPower(byte ComAddr,byte power)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x2F);
		buffer[3]=(byte)power;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x2F,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int RfOutput(byte ComAddr,byte OnOff)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x30);
		buffer[3]=(byte)OnOff;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x30,1000);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}




	public int SetAddress(byte ComAddr,byte newAddr)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x24);
		buffer[3]=(byte)newAddr;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x24,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	public int SetRegion (byte ComAddr,int band,int maxfre, int minfre)
	{
		byte[]buffer = new byte[7];
		buffer[0]=(byte)(0x06);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x22);
		buffer[3]=(byte)(((band & 0x0C) << 4) | (maxfre&0x3F));
		buffer[4]=(byte)(((band & 3) << 6) | (minfre&0x3F));
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x22,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	public int SetAntennaMultiplexing(byte ComAddr, byte SetOnce, byte AntCfg1, byte AntCfg2)
	{
		byte[]buffer = new byte[8];
		buffer[0]=(byte)(0x07);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x3F);
		buffer[3]=(byte)SetOnce;
		buffer[4]=(byte)AntCfg1;
		buffer[5]=(byte)AntCfg2;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x3F,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int ConfigDRM(byte ComAddr, byte DRM[])
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x90);
		buffer[3]=(byte)DRM[0];
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x90,400);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				DRM[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	public int SetBeepNotification(byte ComAddr,byte BeepEn)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x40);
		buffer[3]=(byte)BeepEn;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X40,400);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int SetProfile(byte ComAddr,byte Profile[])
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x7F);
		buffer[3]=Profile[0];
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x7F,500);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Profile[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int SetWritePower(byte ComAddr,byte Power)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x79);
		buffer[3]=Power;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x79,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int GetWritePower(byte ComAddr,byte Power[])
	{
		byte[]buffer = new byte[5];
		buffer[0]=(byte)(0x04);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x7A);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x7A,500);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Power[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int ReadData_G2(byte ComAddr, byte ENum, byte EPC[], byte Mem,
			byte WordPtr, byte Num, byte Password[],byte MaskMem,byte MaskAdr[],byte MaskLen,byte[]MaskData,byte MaskFlag,byte Data[],byte Errorcode[])
	{
		if(MaskFlag==0)
		{
			byte[]buffer = new byte[13 + ENum* 2];
			buffer[0] = (byte)(12 + ENum* 2);
			buffer[1] = ComAddr;
			buffer[2] = 0x02;
			buffer[3]= ENum;
			if(ENum>0)
				System.arraycopy(EPC, 0, buffer, 4, ENum*2);
			buffer[ENum*2+4]=Mem;
			buffer[ENum*2+5]=WordPtr;
			buffer[ENum*2+6]=Num;
			System.arraycopy(Password, 0, buffer, ENum*2+7, 4);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x02,3000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
					System.arraycopy(recvBuff, 4, Data, 0, Num*2);
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
			return 0x30;
		}
		else
		{
			if(MaskLen==0) return 255;
			int maskbyte =0;
			int mLen = MaskLen &255;
			if(mLen%8 ==0)
			{
				maskbyte = mLen/8;
			}
			else
			{
				maskbyte = mLen/8 + 1;
			}
			byte[]buffer = new byte[17 + maskbyte];
			buffer[0] = (byte)(16 + maskbyte);
			buffer[1] = ComAddr;
			buffer[2] = 0x02;
			buffer[3]= ENum;
			if((ENum & 255)==255)
			{
				ENum=0;
			}
			System.arraycopy(EPC, 0, buffer, 4, ENum*2);
			buffer[ENum*2+4]=Mem;
			buffer[ENum*2+5]=WordPtr;
			buffer[ENum*2+6]=Num;
			System.arraycopy(Password, 0, buffer, ENum*2+7, 4);
			buffer[ENum*2+11]=MaskMem;
			buffer[ENum*2+12]=MaskAdr[0];
			buffer[ENum*2+13]=MaskAdr[1];
			buffer[ENum*2+14]=MaskLen;
			System.arraycopy(MaskData, 0, buffer, ENum*2+15, maskbyte);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x02,3000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
					System.arraycopy(recvBuff, 4, Data, 0, Num*2);
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
			return 0x30;
		}
	}
	
	public int ExtReadData_G2(byte ComAddr, byte ENum, byte EPC[], byte Mem,
			byte[] WordPtr, byte Num, byte Password[], byte Data[],byte Errorcode[])
	{
		byte[]buffer = new byte[14 + ENum* 2];
		buffer[0] = (byte)(13 + ENum* 2);
		buffer[1] = ComAddr;
		buffer[2] = 0x15;
		buffer[3]= ENum;
		if((ENum & 255)==255)
		{
			ENum=0;
		}
		System.arraycopy(EPC, 0, buffer, 4, ENum*2);
		buffer[ENum*2+4]=Mem;
		buffer[ENum*2+5]=WordPtr[0];
		buffer[ENum*2+6]=WordPtr[1];
		buffer[ENum*2+7]=Num;
		System.arraycopy(Password, 0, buffer, ENum*2+8, 4);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x15,3000);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Errorcode[0]=0;
				System.arraycopy(recvBuff, 4, Data, 0, Num*2);
			}
			else if((recvBuff[3]&255)==0xFC)
			{
				Errorcode[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int WriteData_G2(byte ComAddr, byte WNum, byte ENum, byte EPC[],
			byte Mem, byte WordPtr, byte Writedata[], byte Password[],byte MaskMem,byte MaskAdr[],byte MaskLen,byte[]MaskData,byte MaskFlag,
			byte Errorcode[])
	{
		if(MaskFlag==0)
		{
			byte[]buffer = new byte[13 + (ENum + WNum) * 2];
			buffer[0] = (byte)(12 + (ENum + WNum) * 2);
			buffer[1] = ComAddr;
			buffer[2] = 0X03;
			buffer[3] = WNum;
			buffer[4] = ENum;
			if(ENum>0)
				System.arraycopy(EPC, 0, buffer, 5, ENum*2);
			buffer[ENum*2+5]=Mem;
			buffer[ENum*2+6]=WordPtr;
			System.arraycopy(Writedata, 0, buffer, ENum*2+7, WNum*2);
			System.arraycopy(Password, 0, buffer, ENum*2+WNum*2+7, 4);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x03,3000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
			return 0x30;
		}
		else
		{
			if(MaskLen==0) return 255;
			int maskbyte =0;
			int mLen = MaskLen &255;
			if(mLen%8 ==0)
			{
				maskbyte = mLen/8;
			}
			else
			{
				maskbyte = mLen/8 + 1;
			}
			byte[]buffer = new byte[17 + (WNum) * 2+ maskbyte];
			buffer[0] = (byte)(16 + (WNum) * 2 + maskbyte);
			buffer[1] = ComAddr;
			buffer[2] = 0X03;
			buffer[3] = WNum;
			buffer[4] = ENum;
			if((ENum & 255)==255)
			{
				ENum=0;
			}
			System.arraycopy(EPC, 0, buffer, 5, ENum*2);
			buffer[ENum*2+5]=Mem;
			buffer[ENum*2+6]=WordPtr;
			System.arraycopy(Writedata, 0, buffer, ENum*2+7, WNum*2);
			System.arraycopy(Password, 0, buffer, ENum*2+WNum*2+7, 4);
			buffer[ENum*2+WNum*2+11]=MaskMem;
			buffer[ENum*2+WNum*2+12]=MaskAdr[0];
			buffer[ENum*2+WNum*2+13]=MaskAdr[1];
			buffer[ENum*2+WNum*2+14]=MaskLen;
			System.arraycopy(MaskData, 0, buffer, ENum*2+WNum*2+15, maskbyte);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x03,3000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
			return 0x30;
		}
		
	}
	
	public int ExtWriteData_G2(byte ComAddr, byte WNum, byte ENum, byte EPC[],
			byte Mem, byte[] WordPtr, byte Writedata[], byte Password[],
			byte Errorcode[])
	{
		byte[]buffer = new byte[14 + (ENum + WNum) * 2];
		buffer[0] = (byte)(13 + (ENum + WNum) * 2);
		buffer[1] = ComAddr;
		buffer[2] = 0x16;
		buffer[3] = WNum;
		buffer[4] = ENum;
		if((ENum & 255)==255)
		{
			ENum=0;
		}
		System.arraycopy(EPC, 0, buffer, 5, ENum*2);
		buffer[ENum*2+5]=Mem;
		buffer[ENum*2+6]=WordPtr[0];
		buffer[ENum*2+7]=WordPtr[1];
		System.arraycopy(Writedata, 0, buffer, ENum*2+8, WNum*2);
		System.arraycopy(Password, 0, buffer, ENum*2+WNum*2+9, 4);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x16,3000);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Errorcode[0]=0;
			}
			else if((recvBuff[3]&255)==0xFC)
			{
				Errorcode[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
		
	}
	public int WriteEPC_G2(byte ComAddr, byte ENum, byte Password[],
			byte WriteEPC[], byte Errorcode[])
	{
		byte[]buffer = new byte[ 10 + ENum*2];
		buffer[0] = (byte)( 9 + ENum*2);
		buffer[1] = ComAddr;
		buffer[2] = 0X04;
		buffer[3]=ENum;
		System.arraycopy(Password, 0, buffer, 4, 4);
		System.arraycopy(WriteEPC, 0, buffer, 8, ENum*2);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x04,2000);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Errorcode[0]=0;
			}
			else if((recvBuff[3]&255)==0xFC)
			{
				Errorcode[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int Lock_G2(byte ComAddr, byte ENum, byte[]EPC,byte select,byte setprotect,byte Password[],byte MaskMem,byte MaskAdr[],byte MaskLen,byte[]MaskData,byte MaskFlag,
			 byte Errorcode[])
	{
		if(MaskFlag==0)
		{
			byte[]buffer = new byte[12 + ENum*2];
			buffer[0] = (byte)(11 + ENum*2);
			buffer[1] = ComAddr;
			buffer[2] = 0X06;
			buffer[3]=ENum;
			if(ENum>0)
				System.arraycopy(EPC, 0, buffer, 4, ENum*2);
			buffer[ENum*2+4]=select;
			buffer[ENum*2+5]=setprotect;
			System.arraycopy(Password, 0, buffer, ENum*2+6, 4);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x06,1000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
		}
		else
		{
			if(MaskLen==0) return 255;
			int maskbyte =0;
			int mLen = MaskLen &255;
			if(mLen%8 ==0)
			{
				maskbyte = mLen/8;
			}
			else
			{
				maskbyte = mLen/8 + 1;
			}
			byte[]buffer = new byte[16 + maskbyte];
			buffer[0] = (byte)(15 + maskbyte);
			buffer[1] = ComAddr;
			buffer[2] = 0x06;
			buffer[3] = ENum;
			buffer[4]=select;
			buffer[5]=setprotect;
			System.arraycopy(Password, 0, buffer, 6, 4);
			buffer[10]=MaskMem;
			buffer[11]=MaskAdr[0];
			buffer[12]=MaskAdr[1];
			buffer[13]=MaskLen;
			System.arraycopy(MaskData, 0, buffer, 14, maskbyte);
			getCRC(buffer,buffer[0]-1);
			SendCMD(buffer);
			int result = GetCMDData(recvBuff,recvLength,0x06,3000);
			if(result==0)
			{
				if(recvBuff[3]==0)
				{
					Errorcode[0]=0;
				}
				else if((recvBuff[3]&255)==0xFC)
				{
					Errorcode[0] = recvBuff[4];
				}
				return recvBuff[3]&255;
			}
			return 0x30;
		}
		return 0x30;
	}
	
	public int Kill_G2(byte ComAddr, byte ENum,byte[] EPC,byte Password[],
			 byte Errorcode[])
	{
		byte[]buffer = new byte[10 + ENum*2];
		buffer[0] = (byte)(9 + ENum*2);
		buffer[1] = ComAddr;
		buffer[2] = 0X05;
		buffer[3]=ENum;
		if(ENum>0)
		System.arraycopy(EPC, 0, buffer, 4, ENum*2);
		System.arraycopy(Password, 0, buffer, ENum*2+4, 4);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x05,1000);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Errorcode[0]=0;
			}
			else if((recvBuff[3]&255)==0xFC)
			{
				Errorcode[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int MeasureReturnLoss(byte ComAddr,byte[]TestFreq,byte Ant,byte[]ReturnLoss)
	{
		byte[]buffer = new byte[10];
		buffer[0]=(byte)(0x09);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x91);
		System.arraycopy(TestFreq, 0, buffer, 3, 4);
		buffer[7] = Ant;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X91,600);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				ReturnLoss[0] = recvBuff[4];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}

	public int MeasureTemperature(byte ComAddr,byte[]Temp)
	{
		byte[]buffer = new byte[5];
		buffer[0]=(byte)(0x04);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x92);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X92,600);
		if(result==0)
		{
			if(recvBuff[3]==0)
			{
				Temp[0] = recvBuff[4];
                Temp[1] = recvBuff[5];
			}
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int SetCheckAnt(byte ComAddr,byte CheckAnt)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x66);
		buffer[3]=(byte)(CheckAnt);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X66,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int SetReadParameter(byte ComAddr,byte[] Parameter)
	{
		byte[]buffer = new byte[10];
		buffer[0]=(byte)(0x09);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x75);
		buffer[3] = Parameter[0];
		buffer[4] = Parameter[1];
		buffer[5] = Parameter[2];
		buffer[6] = Parameter[3];
		buffer[7] = Parameter[4];
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X75,500);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int GetReadParameter(byte ComAddr,byte[] Parameter)
	{
		byte[]buffer = new byte[5];
		buffer[0]=(byte)(0x04);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x77);
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0X77,300);
		if(result==0)
		{
			System.arraycopy(recvBuff, 4, Parameter, 0, 6);
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	public int SetWorkMode(byte ComAddr,byte ReadMode)
	{
		byte[]buffer = new byte[6];
		buffer[0]=(byte)(0x05);
		buffer[1]=(byte)ComAddr;
		buffer[2]=(byte)(0x76);
		buffer[3]=ReadMode;
		getCRC(buffer,buffer[0]-1);
		SendCMD(buffer);
		int result = GetCMDData(recvBuff,recvLength,0x76,1000);
		if(result==0)
		{
			return recvBuff[3]&255;
		}
		return 0x30;
	}
	
	
	
}
