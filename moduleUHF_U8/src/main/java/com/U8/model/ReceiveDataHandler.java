package com.U8.model;
	
public class ReceiveDataHandler {
	
	private static final ReceiveDataHandler mReceiverHolder = new ReceiveDataHandler();
	private int code; // 0 Successful, 1 Failure
	private String message; //额外信息
	private String result; // 返回结果
	
	private ReceiveDataHandler(){}
	
	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
		
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}



	
	public static ReceiveDataHandler  getInstance(){
		return mReceiverHolder;
	}
}
