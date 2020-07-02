package com.rfid.trans;

public interface TagCallback {
    public void tagCallback(ReadTag tag);
    public int tagCallbackFailed(int reason);
    public int CRCErrorCallBack(int errorcode);
    public void FinishCallBack();
}
