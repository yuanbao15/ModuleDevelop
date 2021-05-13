package com.U8.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StringTool {

	/**
	 * 字符串转16进制数组，字符串以空格分割。
	 * @param strHexValue	16进制字符串
	 * @return	数组
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
	 * @param strAryHex	要转换的字符串数组
	 * @param nLen		长度
	 * @return	数组
	 */
    public static byte[] stringArrayToByteArray(String[] strAryHex, int nLen) {
    	if (strAryHex == null) return null;

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
	 * @param btAryHex	要转换的字符串数组
	 * @param nIndex	起始位置
	 * @param nLen		长度
	 * @return	字符串
	 */
    public static String byteArrayToString(byte[] btAryHex, int nIndex, int nLen) {
    	if (nIndex + nLen > btAryHex.length) {
    		nLen = btAryHex.length - nIndex;
    	}

    	String strResult = String.format("%02X", btAryHex[nIndex]);
    	for (int nloop = nIndex + 1; nloop < nIndex + nLen; nloop++ ) {
    		String strTemp = String.format(" %02X", btAryHex[nloop]);

    		strResult += strTemp;
    	}

    	return strResult;
    }

	/**
	 * 将字符串按照指定长度截取并转存为字符数组，空格忽略。
	 * @param strValue	输入字符串
	 * @return	数组
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
                    
                    if (!Pattern.compile("^(([A-F])*([a-f])*(\\d)*)$")
                    		.matcher(strValue.substring(nloop, nloop + 1))
                    		.matches()) {
                        return strAryResult;
                    }

                    strTemp += strValue.substring(nloop, nloop + 1);

                    //判断是否到达截取长度
                    if ((nTemp == nLen) || (nloop == strValue.length() - 1 
                    		&& (strTemp != null && !strTemp.equals("")))) {
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
}
