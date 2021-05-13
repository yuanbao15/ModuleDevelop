package com.U8.utils;

import android.util.Log;

import com.U8.reader.model.InventoryBuffer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Tools {

	// byte 转十六进制
	public static String Bytes2HexString(byte[] b, int size) {
		String ret = "";
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	public static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 })).byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 })).byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

	// 十六进制转byte
	public static byte[] HexString2Bytes(String src) {
		int len = src.length() / 2;
		byte[] ret = new byte[len];
		byte[] tmp = src.getBytes();

		for (int i = 0; i < len; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}

	/* byte[]转Int */
	public static int bytesToInt(byte[] bytes) {
		int addr = bytes[0] & 0xFF;
		addr |= ((bytes[1] << 8) & 0xFF00);
		addr |= ((bytes[2] << 16) & 0xFF0000);
		addr |= ((bytes[3] << 25) & 0xFF000000);
		return addr;

	}

	/* Int转byte[] */
	public static byte[] intToByte(int i) {
		byte[] abyte0 = new byte[4];
		abyte0[0] = (byte) (0xff & i);
		abyte0[1] = (byte) ((0xff00 & i) >> 8);
		abyte0[2] = (byte) ((0xff0000 & i) >> 16);
		abyte0[3] = (byte) ((0xff000000 & i) >> 24);
		return abyte0;
	}

	/**
	 * 获取系统时间，时间格式为： 年-月-日 时：分 秒
	 * 
	 * @return
	 */
	public static String getTime() {
		String model = "yyyy-MM-dd HH:mm:ss";
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(model);
		String dateTime = format.format(date);
		return dateTime;
	}

	
	public static byte getCRC(byte[] cmd, int pos, int len) {
		byte crc = (byte) 0x00;
		for (int i = pos; i < len - 1; i++) {
			crc += cmd[i];
		}
		return crc;
	}

	/**
	 * 字符串转16进制数组，字符串以空格分割。
	 * 
	 * @param strHexValue
	 *            16进制字符串
	 * @return 数组
	 */
	public static byte[] stringToByteArray(String strHexValue) {
		String[] strAryHex = strHexValue.split(" ");
		byte[] btAryHex = new byte[strAryHex.length];

		try {
			int nIndex = 0;
			for (String strTemp : strAryHex) {
				btAryHex[nIndex] = (byte) Integer.parseInt(strTemp, 16);
				nIndex++;
			}
		} catch (NumberFormatException e) {

		}

		return btAryHex;
	}

	/**
	 * 字符数组转为16进制数组。
	 * 
	 * @param strAryHex
	 *            要转换的字符串数组
	 * @param nLen
	 *            长度
	 * @return 数组
	 */
	public static byte[] stringArrayToByteArray(String[] strAryHex, int nLen) {
		if (strAryHex == null)
			return null;

		if (strAryHex.length < nLen) {
			nLen = strAryHex.length;
		}

		byte[] btAryHex = new byte[nLen];

		try {
			for (int i = 0; i < nLen; i++) {
				btAryHex[i] = (byte) Integer.parseInt(strAryHex[i], 16);
			}
		} catch (NumberFormatException e) {

		}

		return btAryHex;
	}

	/**
	 * 16进制字符数组转成字符串。
	 * 
	 * @param btAryHex
	 *            要转换的字符串数组
	 * @param nIndex
	 *            起始位置
	 * @param nLen
	 *            长度
	 * @return 字符串
	 */
	public static String byteArrayToString(byte[] btAryHex, int nIndex, int nLen) {
		if (nIndex + nLen > btAryHex.length) {
			nLen = btAryHex.length - nIndex;
		}

		String strResult = String.format("%02X", btAryHex[nIndex]);
		for (int nloop = nIndex + 1; nloop < nIndex + nLen; nloop++) {
			String strTemp = String.format(" %02X", btAryHex[nloop]);

			strResult += strTemp;
		}

		return strResult;
	}

	/**
	 * 将字符串按照指定长度截取并转存为字符数组，空格忽略。
	 * 
	 * @param strValue
	 *            输入字符串
	 * @return 数组
	 */
	public static String[] stringToStringArray(String strValue, int nLen) {
		String[] strAryResult = null;

		if (strValue != null && !strValue.equals("")) {
			ArrayList<String> strListResult = new ArrayList<String>();
			String strTemp = "";
			int nTemp = 0;

			for (int nloop = 0; nloop < strValue.length(); nloop++) {
				if (strValue.charAt(nloop) == ' ') {
					continue;
				} else {
					nTemp++;

					if (!Pattern.compile("^(([A-F])*([a-f])*(\\d)*)$").matcher(strValue.substring(nloop, nloop + 1)).matches()) {
						return strAryResult;
					}

					strTemp += strValue.substring(nloop, nloop + 1);

					// 判断是否到达截取长度
					if ((nTemp == nLen) || (nloop == strValue.length() - 1 && (strTemp != null && !strTemp.equals("")))) {
						strListResult.add(strTemp);
						nTemp = 0;
						strTemp = "";
					}
				}
			}

			if (strListResult.size() > 0) {
				strAryResult = new String[strListResult.size()];
				for (int i = 0; i < strAryResult.length; i++) {
					strAryResult[i] = strListResult.get(i);
				}
			}
		}

		return strAryResult;
	}

	/**
	 * 16进制转ASCII
	 *
	 * @param hex
	 * @return
	 */
	public static String covertAscii(String hex) {
		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();
		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {
			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char) decimal);
			temp.append(decimal);
		}
		return sb.toString();
	}

	/**
	 * 冒泡排序
	 * 比较相邻的元素。如果第一个比第二个大，就交换他们两个。
	 * 对每一对相邻元素作同样的工作，从开始第一对到结尾的最后一对。在这一点，最后的元素应该会是最大的数。
	 * 针对所有的元素重复以上的步骤，除了最后一个。
	 * 持续每次对越来越少的元素重复上面的步骤，直到没有任何一对数字需要比较。

	 */
	public static List<InventoryBuffer.InventoryTagMap> bubbleSort(List<InventoryBuffer.InventoryTagMap> list)
	{

		String temp = "";
		int size = list.size();
		for(int i = 0 ; i < size-1; i ++)
		{
			for(int j = 0 ;j < size-1-i ; j++)
			{
				if (list.get(j).strEPC.length()>3){
					try {
						if(Integer.parseInt(list.get(j).strEPC.substring(list.get(j).strEPC.length()-5)) > Integer.parseInt(list.get(j+1).strEPC.substring(list.get(j+1).strEPC.length()-5)))  //交换两数位置
						{
							temp = list.get(j).strEPC;
							list.get(j).strEPC = list.get(j+1).strEPC;
							list.get(j+1).strEPC = temp;
						}
					}catch (Exception e){
						Log.i("Tools", "bubbleSort: error");
					}

				}

			}
		}
		return list;
	}

}
