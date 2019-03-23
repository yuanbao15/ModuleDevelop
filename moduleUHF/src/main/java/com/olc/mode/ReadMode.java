package com.olc.mode;

import java.io.Serializable;

public class ReadMode implements Serializable{
	private static final long serialVersionUID = 1L;
	private String EPCNo="";
	private String  TIDNo = "";
    private String  CountNo ="";
	public String getEPCNo() {
		return EPCNo;
	}
	public void setEPCNo(String epcNo) {
		EPCNo = epcNo;
	}
	//
	public String getTIDNo() {
		return TIDNo;
	}
	public void setTIDNo(String tidNo) {
		TIDNo = tidNo;
	}
	public String  getCountNo() {
		return CountNo;
	}
	public void setCountNo(String  countNo) {
		CountNo = countNo;
	}
	
}
