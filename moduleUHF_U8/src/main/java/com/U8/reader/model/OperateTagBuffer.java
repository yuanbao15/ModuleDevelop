package com.U8.reader.model;

import java.util.ArrayList;
import java.util.List;

public class OperateTagBuffer {
	
    public static class OperateTagMap {
    	public String strPC;
    	public String strCRC;
    	public String strEPC;
    	public String strData;
    	public int nDataLen;
    	public byte btAntId;
    	public int nReadCount;
		
		public OperateTagMap() {
			strPC = "";
			strCRC = "";
			strEPC = "";
			strData = "";
			nDataLen = 0;
			btAntId = 0;
			nReadCount = 0;
		}
    }
    
	public String strAccessEpcMatch;
	public List<OperateTagMap> lsTagList;
	
	public OperateTagBuffer() {
		strAccessEpcMatch = "";
		lsTagList = new ArrayList<OperateTagMap>();
	}
	
    public final void clearBuffer() {
    	lsTagList.clear();
    }

}
