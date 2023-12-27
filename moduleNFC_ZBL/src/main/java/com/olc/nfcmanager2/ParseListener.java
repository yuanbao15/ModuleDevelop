package com.olc.nfcmanager2;

/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  NfcManager
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/3/7,  zhangyong, create
 ************************************************************/

public interface ParseListener {
    void onParseComplete(String result);
}
