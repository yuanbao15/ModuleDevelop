package com.olc.nfcmanager;

import android.text.InputType;
import android.text.method.NumberKeyListener;

/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  NfcDemo
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/2/28,  zhangyong, create
 ************************************************************/

public class MyNumberKeyListener extends NumberKeyListener {
    private char[] mAccpectKey = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','A','B','C','D','E','F'};
    @Override
    protected char[] getAcceptedChars() {
        return mAccpectKey;
    }

    @Override
    public int getInputType() {
        return InputType.TYPE_CLASS_TEXT;
    }
}
