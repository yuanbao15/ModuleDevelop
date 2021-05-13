package com.U8.model;


public abstract class ResponseHandler implements IResponseHandler {
	private static final String TAG = "ResponseHandler";

	/*
	 * (non-Javadoc)
	 * @see com.fn.useries.model.IResponseHandler#onSuccess(java.lang.String, java.lang.Object, byte[])
	 */
	@Override
	public void onSuccess(String msg, Object data, byte[] parameters) {

	}
	/*
	 * (non-Javadoc)
	 * @see com.fn.useries.model.IResponseHandler#onFailure(java.lang.String)
	 */
	@Override
	public void onFailure(String msg) {
		System.out.println("onFailure---msg:" + msg);
	}

}
