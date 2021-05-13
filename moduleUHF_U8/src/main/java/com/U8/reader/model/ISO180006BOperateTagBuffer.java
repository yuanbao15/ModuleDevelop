package com.U8.reader.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ISO180006BOperateTagBuffer {
	
    public static class ISO180006BOperateTagMap {
    	public byte btAntId;
    	public String strUID;
    	public int nTotal;
    	public byte btStartAdd;
    	public int nLength;
    	public String strData;
    	public byte btStatus;
		
		public ISO180006BOperateTagMap() {
			btAntId = 0;
			strUID = "";
			nTotal = 0;
			btStartAdd = 0;
			nLength = 0;
			strData = "";
			btStatus = 0;
		}
    }

    public Map<String, Integer> dtIndexMap;
	public List<ISO180006BOperateTagMap> lsTagList;
	public byte btAntId;
	public int nTagCount;
	public String strReadData;
	public byte btWriteLength;
	public byte btStatus;

    public ISO180006BOperateTagBuffer() {
		btAntId = (byte) 0xFF;
		nTagCount = 0;
		strReadData = "";
		btWriteLength = 0x00;
		btStatus = 0x00;
		
		dtIndexMap = new LinkedHashMap<String, Integer>();
		lsTagList = new ArrayList<ISO180006BOperateTagMap>();
    }

    public void clearBuffer() {
		btAntId = (byte) 0xFF;
		nTagCount = 0;
		strReadData = "";
		btWriteLength = 0x00;
		btStatus = 0x00;

		clearTagMap();
    }
    
    public final void clearTagMap() {
    	dtIndexMap.clear();
		lsTagList.clear();
    }

}
