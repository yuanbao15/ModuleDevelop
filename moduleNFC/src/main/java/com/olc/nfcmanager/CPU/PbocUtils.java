package com.olc.nfcmanager.CPU;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.olc.nfcmanager.Utils;

import java.io.IOException;

/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  NfcDemo
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/2/14,  zhangyong, create
 ************************************************************/

public class PbocUtils {
    private static PbocUtils sPbocUtils;
    public static final String TAG= "PBOC";
    private String mType;
    private String mInfo;
    private int mPageCount;
    private IsoDep mIsoDep;
    private PbocUtils(){

    }

    public static synchronized PbocUtils getInstance(){
        if (sPbocUtils == null){
            sPbocUtils = new PbocUtils();
        }
        return sPbocUtils;
    }

    public  String getType(){
        return mType;
    }
    public  String getInfo(){
        return mInfo;
    }
    public int getPageCount() {
        return mPageCount;
    }

    public  String parsePBOC(Tag tag) {
        if (tag != null){
            mIsoDep = IsoDep.get(tag);
            try {
                mIsoDep.connect();
                mType = "PBOC";
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (mIsoDep.isConnected()){
                    try {
                        mIsoDep.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return "";
    }

    public String getBalances(){
        String info = "";
        try {
            EV1CardManager.connectIsoDep(mIsoDep);
            if (selectWalletApp(mIsoDep)){
                info  = getData(mIsoDep);
                selectFile(mIsoDep);
                getWalletInfo(mIsoDep);
            }
            EV1CardManager.closeIsoDep(mIsoDep);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mIsoDep.isConnected()){
                try {
                    mIsoDep.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return info;
    }
    public  boolean selectWalletApp(IsoDep isoDep) throws IOException {
        try{
            // select PSF (1PAY.SYS.DDF01)
            byte[] name = { (byte) '1', (byte) 'P',
                    (byte) 'A', (byte) 'Y', (byte) '.', (byte) 'S', (byte) 'Y',
                    (byte) 'S', (byte) '.', (byte) 'D', (byte) 'D', (byte) 'F',
                    (byte) '0', (byte) '1', };	//1PAY.SYS.DDF01
            byte[] command = new byte[name.length + 6];
            command[1] = (byte) 0xA4;
            command[2] = (byte) 0x04;
            command[4] = (byte) name.length;
            System.arraycopy(name,0,command,5,name.length);

            byte[] response = isoDep.transceive(command);
            Log.i(TAG, "selectWalletApp: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0){
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
    public String getData(IsoDep isoDep) throws IOException {
        try{
            byte[] command = new byte[4];
            command[1] = (byte) 0xB2;
            command[2] = (byte) 0x01;
            command[3] = (byte) 0x0C;

            byte[] response = isoDep.transceive(command);
            Log.i(TAG, "getData: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0){
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
    public String selectFile(IsoDep isoDep) throws IOException {
        try{
            byte[] aid = {(byte) 0xA0,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x03,(byte) 0x86,(byte) 0x98,(byte) 0x07,(byte) 0x01};
            byte[] command = new byte[6+aid.length];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xA4;
            command[2] = (byte) 0x04;
            command[3] = (byte) 0x00;
            command[4] = (byte) aid.length;
            System.arraycopy(aid,0,command,5,aid.length);
            byte[] response = isoDep.transceive(command);
            Log.i(TAG, "selectFile: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0){
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    public  String getWalletInfo(IsoDep isoDep) throws IOException {
        try{
            boolean isEP = true;
            byte[] command = { (byte) 0x80, // CLA Class
                    (byte) 0x5C, // INS Instruction
                    (byte) 0x00, // P1 Parameter 1
                    (byte) (isEP ? 2 : 1), // P2 Parameter 2
                    (byte) 0x04, // Le
            };
            byte[] response = isoDep.transceive(command);
            Log.i(TAG, "getWalletInfo: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00){
                byte[] data = new byte[response.length-2];
                System.arraycopy(response, 0, data, 0, data.length);
                return Utils.bytesToHexString(data);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }


}
