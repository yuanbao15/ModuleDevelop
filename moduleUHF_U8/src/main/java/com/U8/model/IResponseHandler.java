package com.U8.model;


public interface IResponseHandler {
	
	/**
	 * 成功
	 * msg         成功时返回的消息
	 * data         成功时返回的数据，已转换为字符串
	 * parameters  成功时返回的响应帧中的Parameters域的数据
	 */
	public void onSuccess(String msg, Object data, byte[] parameters);
	
	/**
	 * 失败 
	 * msg 失败的消息
	 */
	public void onFailure(String msg);
	
}
