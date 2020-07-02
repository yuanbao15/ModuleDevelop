package com.rfid.trans;

import android.media.SoundPool;
import android.os.SystemClock;

public class UHFLib{
	private BaseReader reader = new BaseReader();
	private ReaderParameter param = new ReaderParameter();
	private volatile boolean mWorking = true; 
	private volatile Thread mThread=null;
	private volatile boolean soundworking = true;
	private volatile boolean isSound = false;
	private volatile Thread sThread=null;
	private byte[]pOUcharIDList=new byte[25600];
	private volatile int NoCardCOunt=0;
	private Integer soundid=null;
	private SoundPool soundPool=null;
	private TagCallback callback;
	public UHFLib()
	{
		param.ComAddr =(byte)255;
		param.ScanTime =10;
		param.Session = 1;
		param.QValue = 4;
		param.TidLen = 0;
		param.TidPtr =0;
		param.Antenna =0x80;
		param.Target = 0;
	}


	public int Connect(String ComPort, int BaudRate)
	{
		int result = reader.Connect(ComPort, BaudRate, 1);
		if(result ==0)
		{
			byte[] Version=new byte[2];
			byte[] Power=new byte[1];
			byte[] band = new byte[1];
			byte[] MaxFre= new byte[1];
			byte[] MinFre = new byte[1];
			byte[] BeepEn = new byte[1];
			byte[] Ant =new byte[1];
			result = GetUHFInformation(Version,Power,band,MaxFre,MinFre,BeepEn,Ant);
			if(result!=0)
			{
				reader.DisConnect();
			}
		}
		return result;
	}
	
	public int DisConnect()
	{
		StopRead();
		return reader.DisConnect();
	}
	
	public void SetInventoryPatameter(ReaderParameter param)
	{
		this.param = param;
	}
	
	public ReaderParameter GetInventoryPatameter()
	{
		return this.param;
	}
	
	public int GetUHFInformation(byte Version[],byte Power[],byte band[],byte MaxFre[],byte MinFre[],byte BeepEn[],byte Ant[])
	{
		byte[]ReaderType=new byte[1];
		byte[]TrType=new byte[1];
		byte[]ScanTime=new byte[1];
		byte[]OutputRep=new byte[1];
		byte[]CheckAnt=new byte[1];
		byte[]ComAddr =new byte[1];
		ComAddr[0]=(byte)255;
		int result = reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, Ant, BeepEn, OutputRep, CheckAnt);
		if(result==0)
		{
			param.ComAddr = ComAddr[0];
			param.Antenna = Ant[0];
		}
		return result;
	}

	public String GetVersion()
    {
        byte[]ReaderType=new byte[1];
        byte[]TrType=new byte[1];
        byte[]ScanTime=new byte[1];
        byte[]OutputRep=new byte[1];
        byte[]CheckAnt=new byte[1];
        byte[]ComAddr =new byte[1];
        byte Version[]=new byte[2];
        byte Power[]=new byte[1];
        byte band[] =new byte[1];
        byte MaxFre[]=new byte[1];
        byte MinFre[]=new byte[1];
        byte BeepEn[]=new byte[1];
        byte Ant[]=new byte[1];
        ComAddr[0]=(byte)255;
        String servion = null;
        int result = reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, Ant, BeepEn, OutputRep, CheckAnt);
        if(result==0)
        {
            param.ComAddr = ComAddr[0];
            param.Antenna = Ant[0];
            String temp = String.valueOf(Version[1]&255);
            if(temp.length()==1)
                temp="0"+temp;
            servion =String.valueOf(Version[0]&255)+"."+temp;
        }
        return servion;
    }

    public String GetModuleType()
    {
        byte[]ReaderType=new byte[1];
        byte[]TrType=new byte[1];
        byte[]ScanTime=new byte[1];
        byte[]OutputRep=new byte[1];
        byte[]CheckAnt=new byte[1];
        byte[]ComAddr =new byte[1];
        byte Version[]=new byte[2];
        byte Power[]=new byte[1];
        byte band[] =new byte[1];
        byte MaxFre[]=new byte[1];
        byte MinFre[]=new byte[1];
        byte BeepEn[]=new byte[1];
        byte Ant[]=new byte[1];
        ComAddr[0]=(byte)255;
        String sTpye = null;
        int result = reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, Ant, BeepEn, OutputRep, CheckAnt);
        if(result==0)
        {
           if((ReaderType[0]&255)==0x0F)
           {
               sTpye="9813";
           }
           else if((ReaderType[0]&255)==0x10)
           {
               sTpye="9810";
           }
           else
           {
               sTpye="R2000";
           }
        }
        return sTpye;
    }

    public int setFrequencyMode(int mode)
	{
		int  maxfre, minfre;
		maxfre=0;
		minfre=0;
		switch (mode)
		{
			case 1://Chinese band2
				maxfre = 19;
				break;
			case 2://US Band
				maxfre = 49;
			case 3://Korean band
				maxfre = 31;
			case 4://EU band
				maxfre = 14;
			case 8://Chinese band1
				maxfre = 19;
				break;
			default:
				maxfre = 0;
		}
		int result = reader.SetRegion(param.ComAddr, mode, maxfre, minfre);
		return result;
	}

    public int getFrequencyMode()
    {
        byte[]ReaderType=new byte[1];
        byte[]TrType=new byte[1];
        byte[]ScanTime=new byte[1];
        byte[]OutputRep=new byte[1];
        byte[]CheckAnt=new byte[1];
        byte[]ComAddr =new byte[1];
        byte Version[]=new byte[2];
        byte Power[]=new byte[1];
        byte band[] =new byte[1];
        byte MaxFre[]=new byte[1];
        byte MinFre[]=new byte[1];
        byte BeepEn[]=new byte[1];
        byte Ant[]=new byte[1];
        ComAddr[0]=(byte)255;
        String sTpye = null;
        int result = reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, Ant, BeepEn, OutputRep, CheckAnt);
        if(result==0)
        {
            return band[0];
        }
        else
        {
            return -1;
        }
    }


	public int SetRfPower(int Power)
	{
		return reader.SetRfPower(param.ComAddr, (byte)Power);
	}

	public int GetRfPower()
	{
		byte[]ReaderType=new byte[1];
		byte[]TrType=new byte[1];
		byte[]ScanTime=new byte[1];
		byte[]OutputRep=new byte[1];
		byte[]CheckAnt=new byte[1];
		byte[]ComAddr =new byte[1];
		byte Version[]=new byte[2];
		byte Power[]=new byte[1];
		byte band[] =new byte[1];
		byte MaxFre[]=new byte[1];
		byte MinFre[]=new byte[1];
		byte BeepEn[]=new byte[1];
		byte Ant[]=new byte[1];
		ComAddr[0]=(byte)255;
		String sTpye = null;
		int result = reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, Ant, BeepEn, OutputRep, CheckAnt);
		if(result==0)
		{
			return Power[0];
		}
		else
		{
			return -1;
		}
	}


	public int SetRegion(int band,int maxfre,int minfre)
	{
		return reader.SetRegion(param.ComAddr, band, maxfre, minfre);
	}


	public int RfOutput(byte OnOff)
	{
		 return  reader.RfOutput(param.ComAddr,OnOff);
	}

    public String MeasureTemperature()
    {
		byte[]Temp =new byte[2];
    	String strTemp=null;
    	int result =  reader.MeasureTemperature(param.ComAddr,Temp);
    	if(result==0)
		{
			if(Temp[0]==1)
			{
				strTemp = String.valueOf(Temp[1])+"℃";
			}
			else
			{
				strTemp = "-"+String.valueOf(Temp[1])+"℃";
			}
		}
		return strTemp;
    }

	public int MeasureReturnLoss()
	{
		byte[]TestFreq =new byte[4];
		TestFreq[0]=(byte)0x00;
		TestFreq[1]=(byte)0x0D;
		TestFreq[2]=(byte)0xF7;
		TestFreq[3]=(byte)0x32;
		byte Ant =(byte)0x00;
        byte[]ReturnLoss =new byte[1];
        int result =  reader.MeasureReturnLoss(param.ComAddr,TestFreq,Ant,ReturnLoss);
        if(result==0)
        {
            return ReturnLoss[0]&255;
        }
        else
        {
            return -1;
        }
	}

	public int setRFLink(byte rflink)
	{
		byte[]profile=new byte[1];
		profile[0] =(byte)(0x80|rflink);
		return reader.SetProfile(param.ComAddr,profile);
	}


	public int getRFLink()
	{
        byte profile[] =new byte[1];
		profile[0] =0;
		int result = reader.SetProfile(param.ComAddr,profile);
		if(result==0)
        {
            return profile[0]&255;
        }
        else
        {
            return -1;
        }
	}

	public int getWritePower()
	{
		byte Power[] =new byte[1];
		int result = reader.GetWritePower(param.ComAddr,Power);
		if(result==0)
			return Power[0]&255;
		else
			return -1;
	}

	public int setWritePower(int Power)
	{
		return reader.SetWritePower(param.ComAddr,(byte)Power);
	}

	public int SetAntenna(byte AntCfg)
	{
		byte SetOnce=1;
		byte AntCfg1=0;
		int result= reader.SetAntennaMultiplexing(param.ComAddr, SetOnce, AntCfg1, AntCfg);
		if(result==0)
		{
			param.Antenna =AntCfg;
		}
		return result;
	}

    public int SetPowerMode(byte[] powermode)
    {
        return reader.SetPowerMode(param.ComAddr,powermode);
    }

	public int SetBeepNotification(int BeepEn)
	{
		return reader.SetBeepNotification(param.ComAddr, (byte)BeepEn);
	}

    public int SetWorkMode(byte ReadMode)
    {
        return reader.SetWorkMode(param.ComAddr,ReadMode);
    }
	
	public String ReadDataByEPC(String EPCStr, byte Mem, byte WordPtr, byte Num, String HexPassword)
	{
		if(EPCStr.length() % 4 !=0)
			return null;
        if(HexPassword.length()!=8)
        	return null;
		byte ENum = (byte)(EPCStr.length()/4);
		byte[] EPC = reader.hexStringToBytes(EPCStr);
		byte[]Password = reader.hexStringToBytes(HexPassword);
		byte MaskMem =0;
		byte[]MaskAdr=new byte[2];
		byte MaskLen=0;
		byte[]MaskData=new byte[12];
		byte MaskFlag=0;
		byte[]Data=new byte[Num*2];
		byte[]Errorcode=new byte[1];
		int result = reader.ReadData_G2(param.ComAddr, ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Data, Errorcode);
		if(result==0)
		{
			return reader.bytesToHexString(Data, 0, Data.length);
		}
		else
		{
			return null;
		}
	}

	public String ReadDataByMask(int imaskmem,int imaskaddr,int imasklen,String imaskdata,byte Mem, byte WordPtr, byte Num, String HexPassword)
	{
	    if(imaskdata.length()*4>255)return null;
        if(HexPassword.length()!=8) return null;
		byte ENum = (byte)255;
		byte[] EPC = new byte[20];
		byte[]Password = reader.hexStringToBytes(HexPassword);
		byte MaskMem =(byte)imaskmem;
		byte[]MaskAdr=new byte[2];
        MaskAdr[0] = (byte)(imaskaddr >> 8);
        MaskAdr[1] = (byte)(imaskaddr & 0x00FF);
		byte MaskLen=(byte)(imaskdata.length()*4);
		byte[]MaskData=new byte[256];
        if(imaskdata.length()%2 !=0)
            imaskdata=imaskdata+"0";
        MaskData =  reader.hexStringToBytes(imaskdata);
		byte MaskFlag=1;
		byte[]Data=new byte[Num*2];
		byte[]Errorcode=new byte[1];
		int result = reader.ReadData_G2(param.ComAddr, ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Data, Errorcode);
		if(result==0)
		{
			return reader.bytesToHexString(Data, 0, Data.length);
		}
		else
		{
			return null;
		}
	}

	public String ReadDataByTID(String TIDStr, byte Mem, byte WordPtr, byte Num, String HexPassword)
	{
        if(HexPassword.length()!=8) return null;
		if(TIDStr.length() % 4 !=0) return "FF";
        byte[]Password = reader.hexStringToBytes(HexPassword);
		byte ENum = (byte)255;
		byte[] EPC = new byte[12];
		byte[]TID = reader.hexStringToBytes(TIDStr);
		byte MaskMem =2;
		byte[]MaskAdr=new byte[2];
		MaskAdr[0]=MaskAdr[1]=0;
		byte MaskLen=(byte)(TIDStr.length()*4);
		byte[]MaskData=new byte[TIDStr.length()];
		System.arraycopy(TID, 0, MaskData, 0, TID.length);
		byte MaskFlag=1;
		byte[]Data=new byte[Num*2];
		byte[]Errorcode=new byte[1];
		int result = reader.ReadData_G2(param.ComAddr, ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Data, Errorcode);
		if(result==0)
		{
			return reader.bytesToHexString(Data, 0, Data.length);
		}
		else
		{
		    return null;
			//return String.format("%2X", result);
		}
	}
	
	public int WriteDataByEPC(String EPCStr, byte Mem, byte WordPtr, String HexPassword, String wdata)
	{
		if(EPCStr.length() % 4 !=0)
			return 255;
		if(wdata.length() % 4 !=0)
			return 255;
        if(HexPassword.length()!=8)
        	return 255;
        byte[]Password = reader.hexStringToBytes(HexPassword);
		byte ENum = (byte)(EPCStr.length()/4);
		byte WNum = (byte)(wdata.length()/4);
		byte[] EPC = reader.hexStringToBytes(EPCStr);
		byte[] data = reader.hexStringToBytes(wdata); 
		byte MaskMem =0;
		byte[]MaskAdr=new byte[2];
		byte MaskLen=0;
		byte[]MaskData=new byte[12];
		byte MaskFlag=0;
		byte[]Errorcode=new byte[1];
		return reader.WriteData_G2(param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
	}

    public int WriteDataByMask(int imaskmem,int imaskaddr,int imasklen,String imaskdata, byte Mem, byte WordPtr, String HexPassword, String wdata)
    {
        if(wdata.length() % 4 !=0) return 255;
        if(HexPassword.length()!=8) return 255;
        byte[]Password = reader.hexStringToBytes(HexPassword);
        byte ENum = (byte)255;
        byte WNum = (byte)(wdata.length()/4);
        byte[] EPC = new byte[32];
        byte[] data = reader.hexStringToBytes(wdata);
        byte MaskMem =(byte)imaskmem;
        byte[]MaskAdr=new byte[2];
        MaskAdr[0] = (byte)(imaskaddr >> 8);
        MaskAdr[1] = (byte)(imaskaddr & 0x00FF);
        byte MaskLen=(byte)(imaskdata.length()*4);
        byte[]MaskData=new byte[256];
        if(imaskdata.length()%2 !=0)
            imaskdata=imaskdata+"0";
        MaskData =  reader.hexStringToBytes(imaskdata);
        byte MaskFlag=1;
        byte[]Errorcode=new byte[1];
        return reader.WriteData_G2(param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
    }

	
	public int WriteDataByTID(String TIDStr, byte Mem, byte WordPtr,String HexPassword, String wdata)
	{
		if(TIDStr.length() % 4 !=0) return 255;
		if(wdata.length() % 4 !=0) return 255;
        if(HexPassword.length()!=8) return 255;
		byte ENum = (byte)255;
		byte WNum = (byte)(wdata.length()/4);
		byte[] EPC = new byte[12];
		byte[] data = reader.hexStringToBytes(wdata); 
		byte[]TID = reader.hexStringToBytes(TIDStr);
        byte[]Password = reader.hexStringToBytes(HexPassword);
		byte MaskMem =2;
		byte[]MaskAdr=new byte[2];
		MaskAdr[0]=MaskAdr[1]=0;
		byte MaskLen=(byte)(TIDStr.length()*4);
		byte[]MaskData=new byte[TIDStr.length()];
		System.arraycopy(TID, 0, MaskData, 0, TID.length);
		byte MaskFlag=1;
		byte[]Errorcode=new byte[1];
		return reader.WriteData_G2(param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
	}
	
	public int WriteEPCByTID(String TIDStr, String EPCStr, byte Password[])
	{
		if(TIDStr.length() % 4 !=0) return 255;
		if(EPCStr.length() % 4 !=0) return 255;
		byte ENum = (byte)255;
		byte WNum = (byte)(EPCStr.length()/4);
		byte[] EPC = new byte[12];
		String PCStr="";
		switch(WNum)
		{
		case 1:
			PCStr="0800"; break;
		case 2:
			PCStr="1000"; break;
		case 3:
			PCStr="1800"; break;
		case 4:
			PCStr="2000"; break;
		case 5:
			PCStr="2800"; break;
		case 6:
			PCStr="3000"; break;
		case 7:
			PCStr="3800"; break;
		case 8:
			PCStr="4000"; break;
		case 9:
			PCStr="4800"; break;
		case 10:
			PCStr="5000"; break;
		case 11:
			PCStr="5800"; break;
		case 12:
			PCStr="6000"; break;
		case 13:
			PCStr="6800"; break;
		case 14:
			PCStr="7000"; break;
		case 15:
			PCStr="7800"; break;
		case 16:
			PCStr="8000"; break;
		}
		String wdata=PCStr +EPCStr;
        WNum+=1;
		byte[] data = reader.hexStringToBytes(wdata); 
		byte[]TID = reader.hexStringToBytes(TIDStr);
		
		byte MaskMem =2;
		byte[]MaskAdr=new byte[2];
		MaskAdr[0]=MaskAdr[1]=0;
		byte MaskLen=(byte)(TIDStr.length()*4);
		byte[]MaskData=new byte[TIDStr.length()];
		System.arraycopy(TID, 0, MaskData, 0,TID.length);
		byte MaskFlag=1;
		byte[]Errorcode=new byte[1];
		byte Mem=1;
		byte WordPtr=1;
		return reader.WriteData_G2(param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
	}
	
	public int LockByEPC(String EPCStr, byte select, byte setprotect, String PasswordStr)
	{
		if(EPCStr.length() % 4 !=0) return 255;
		if(PasswordStr.length() !=8) return 255;
		byte ENum = (byte)(EPCStr.length()/4);
		byte[] EPC = reader.hexStringToBytes(EPCStr);
		byte[] Password = reader.hexStringToBytes(PasswordStr);
		byte[]Errorcode=new byte[1];
		byte MaskMem =0;
		byte[]MaskAdr=new byte[2];
		byte MaskLen=0;
		byte[]MaskData=new byte[12];
		byte MaskFlag=0;
		return reader.Lock_G2(param.ComAddr, ENum, EPC, select, setprotect, Password,MaskMem,MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
	}

	public int LockByMask(int imaskmem,int imaskaddr,int imasklen,String imaskdata, byte select, byte setprotect, String PasswordStr)
	{
		if(PasswordStr.length() !=8) return 255;
		byte ENum = (byte)255;
		byte[] EPC = new byte[32];
		byte[] Password = reader.hexStringToBytes(PasswordStr);
		byte[]Errorcode=new byte[1];
		byte MaskMem =(byte)imaskmem;
		byte[]MaskAdr=new byte[2];
		MaskAdr[0] = (byte)(imaskaddr >> 8);
		MaskAdr[1] = (byte)(imaskaddr & 0x00FF);
		byte MaskLen=(byte)(imaskdata.length()*4);
		byte[]MaskData=new byte[256];
		if(imaskdata.length()%2 !=0)
			imaskdata=imaskdata+"0";
		MaskData =  reader.hexStringToBytes(imaskdata);
		byte MaskFlag=1;
		return reader.Lock_G2(param.ComAddr, ENum, EPC, select, setprotect, Password,MaskMem,MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
	}
	
	public int Kill(String EPCStr, String PasswordStr)
	{
		if(EPCStr.length() % 4 !=0) return 255;
		if(PasswordStr.length() !=8) return 255;
		byte ENum = (byte)(EPCStr.length()/4);
		byte[] EPC = reader.hexStringToBytes(EPCStr);
		byte[] Password = reader.hexStringToBytes(PasswordStr);
		byte[]Errorcode=new byte[1];
		return reader.Kill_G2(param.ComAddr, ENum, EPC, Password, Errorcode);
	}
	
	public void SetCallBack(TagCallback callback)
    {
    	this.callback = callback;
		reader.SetCallBack(callback);
    }
    public String SingleInventory()
	{
		return reader.SingleInventory_G2(param.ComAddr);
	}
    long beginTime,endtime,ttbegintime;
	int CurrentNum=0;
	public int StartRead()
	{
		if(mThread==null)
		{
			mWorking=true;
			mThread = new Thread(new Runnable() {
	        @Override
	        public void run() {
	        	byte Target=(byte)param.Target;
				CurrentNum=0;
	            while(mWorking)
	            {
						byte Ant=(byte)0x80;
						int[]pOUcharTagNum=new int[1];
						int[]pListLen=new int[1];
						pOUcharTagNum[0]=pListLen[0]=0;
						if((param.Session==0)||(param.Session==1))
						{Target=0;NoCardCOunt=0;}
						beginTime = SystemClock.elapsedRealtime();
						pOUcharTagNum[0]=0;
						int result = reader.Inventory_G2(param.ComAddr, (byte)param.QValue, (byte)param.Session, (byte)param.TidPtr, (byte)param.TidLen, Target, Ant,param.ScanTime, pOUcharIDList, pOUcharTagNum, pListLen);
						if(pOUcharTagNum[0]==0)
						{
							isSound=false;
							if(param.Session>1)
							{
								NoCardCOunt++;
								if(NoCardCOunt>4)
								{
									Target=(byte)(1-Target);
									NoCardCOunt=0;
								}
							}
						}
						else
						{
							NoCardCOunt=0;
						}
						SystemClock.sleep(40);
				}
				mThread=null;
				if(callback!=null)
				{
					callback.FinishCallBack();
				}
	        }
	        });
			mThread.start();
			return 0;
		}
		else
		{
			return 1;
		}
	}

    public int Inventory_G2(byte pOUcharIDList[],int pOUcharTagNum[],int pListLen[])
    {
        byte Ant=(byte)0x80;
		byte Target=(byte)param.Target;
        return reader.Inventory_G2(param.ComAddr, (byte)param.QValue, (byte)param.Session, (byte)param.TidPtr, (byte)param.TidLen, Target, Ant,param.ScanTime, pOUcharIDList, pOUcharTagNum, pListLen);
    }
	
	public void StopRead()
	{
		mWorking=false;
	}
}

