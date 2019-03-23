package com.olc.nfcmanager.CPU;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import com.olc.nfcmanager.CPU.util.AES;
import com.olc.nfcmanager.CPU.util.TripleDES;
import com.olc.nfcmanager.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;


/************************************************************
 * Copyright 2000-2066 Olc Corp., Ltd.
 * All rights reserved.
 * Description     : The Main activity for  NfcDemo
 * History        :( ID, Date, Author, Description)
 * v1.0, 2017/2/8,  zhangyong, create
 ************************************************************/

public class EV1CardManager {
    private static final String TAG = "Ev1";
    static byte[] SELECT = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x5A, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            3, // Lc  = 6  (data/AID has 6 bytes)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };

    static byte[] AUTHOR = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x0A, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x00
    };
    static byte[] FORMAT_PICC = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0xFC, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };
    static byte[] SELECT_APP_IDS = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x6A, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };
    static byte[] GET_KEY_SETTINGS = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x45, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };
    static byte[] DELETE_APP = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0xDA, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            3, // Lc  = 6  (data/AID has 6 bytes)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };

    static byte[] SELECT_AF = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0xAF, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x10,
            (byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x2b,(byte) 0x8f,(byte) 0x09,(byte) 0xbd,(byte) 0x99,(byte) 0xaf,(byte) 0x0d,(byte) 0xf8
            ,(byte) 0x00
    };
    static byte[] SELECT_GET_VER = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x60, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };

    static byte[] SELECT_APP_DF = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x6D, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };

    static byte[] GET_FILES_IDS = {
            (byte) 0x90, // CLA = 00 (first interindustry command set)
            (byte) 0x6F, // INS = A4 (SELECT)
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x00
    };

    static byte[] SELECT_ONE_DF = {
            (byte) 0x90,(byte) 0xBD ,(byte) 0x00,(byte)  0x00,(byte)  7 ,(byte) 0x01 ,(byte) 0x00,(byte)  0x00 ,(byte) 0x00,(byte)  0x00,(byte)  0x00,(byte)  0x00 ,(byte) 0x00
    };
    private static boolean sIsConnect;
    /*
 * EXAMPLE OUTPUT:
 *
PC/SC card in SCL011G Contactless Reader [SCL01x Contactless Reader] (21161044200765) 00 00, protocol T=1, state OK
>> 90 5a 00 00 03 00 00 00 00 (SELECT_APPLICATION)
<< 91 00 (OPERATION_OK)
>> 90 aa 00 00 01 00 00 (AUTHENTICATE_AES)
<< cc 11 3e 08 27 7a a4 0e 04 19 47 e4 6e f9 f4 cd 91 af (ADDITIONAL_FRAME)
>> 90 af 00 00 20 13 33 d8 c8 15 1a f1 d3 f5 71 b2 dd c1 4f ad 1c 32 c4 18 b1 db 14 35 8a 4f 14 3e c9 bc d9 db f2 00 (MORE)
<< c7 6d 8b 1e 84 1b b7 56 9a fa 40 b0 81 da 8c 69 91 00 (OPERATION_OK)
The random A is e2 b1 e6 6b d5 81 70 37 79 2d 47 72 6d b5 a2 1a
The random B is e4 52 67 6a 81 f3 7b 7b 7e 23 25 28 5c 71 f8 1b
Session key is e2 b1 e6 6b e4 52 67 6a 6d b5 a2 1a 5c 71 f8 1b
 *
 */


    public static  String paresIsoDep(Tag tag) {
        Log.i(TAG,"paresIsoDep: ");
        String info = "";
        IsoDep isoDep = IsoDep.get(tag);
        try
        {
            isoDep.connect();
            byte fileNumber = 0x00; //file number (1 byte),
            byte commSett = 0x00; //communication settings (1 byte),
//            byte accessRight1 = 0x33, accessRight2 = 0x33; //access rights (2 bytes),
            byte accessRight1 = 0x12, accessRight2 = 0x30; //access rights (2 bytes),
            byte appMasterKeySetting = (byte) 0xEF;
            byte numOfKeys = (byte) 0x85;  // 0x85   0x80 (AE) + 5 (num of keys)
//            byte[] aid = new byte[] {0x09, 0x09, 0x09};
            byte[] aid = new byte[]{0x12, 0x00, 0x00};
            byte fileSize = 0x20;

            getVersion(isoDep);

            boolean success = selectApplication(isoDep,new byte[] {0x00, 0x00, 0x00});
            if (success){
                authenticate(isoDep,new byte[8], (byte) 0x00, KeyType.DES);
                formatPICC(isoDep);
//                byte[] response = getApplicationIds(isoDep);
//                byte[] response = getApplicationIds(isoDep);
//                if (response == null) {
                    if(createApplication(isoDep,aid, appMasterKeySetting, numOfKeys)){
                        selectApplication(isoDep,aid);
//                        getKeySettings(isoDep);
//                        authenticate(isoDep,new byte[16], (byte) 0x00, DESFireEV1.KeyType.AES);

//                        authenticate(isoDep,new byte[8], (byte) 0x00, DESFireEV1.KeyType.DES);
//                        success = createStdDataFile(isoDep,new byte[] {fileNumber, commSett, accessRight1, accessRight2, 0x40, 0x00, 0x00});
                        success = createStdDataFile(isoDep,new byte[] {0x01, commSett, 0x14, 0x32, 0x20, 0x00, 0x00});
                    }
//                } else {
//                    success = deleteApplication(isoDep,aid);
//                }

            }
            isoDep.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static boolean chaneKeySettings(IsoDep isoDep,byte keySettings) throws IOException {
        byte[] apdu = new byte[7];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.CHANGE_KEY_SETTINGS.getCode();
        apdu[4] = 0x01;
        apdu[5] = keySettings;
        byte[] response = isoDep.transceive(apdu);
        Log.i(TAG, "chaneKeySettings: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00)
            return true;
        return false;
    }

    private static boolean changeKey(IsoDep isoDep, byte keyNo, byte[] bytes) throws IOException {
        byte[] apdu = new byte[31];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.CHANGE_KEY.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x19;
        apdu[5] = keyNo;
        System.arraycopy(bytes, 0, apdu, 6, 16);
        byte[] response = isoDep.transceive(apdu);
        Log.i(TAG, "changeKey: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00)
            return true;
        return false;
    }

    public static void connectIsoDep(IsoDep isoDep) throws IOException {
        if (isoDep != null){
            isoDep.connect();
            sIsConnect = true;
        }
    }

    public static void closeIsoDep(IsoDep isoDep) throws IOException {
        if (isoDep != null){
            isoDep.close();
            sIsConnect = false;
        }
    }

    public static boolean isHasConnect(){
        return sIsConnect;
    }

    public static boolean selectPICC(IsoDep isoDep){
        boolean result = false;
        try{
            result = selectApplication(isoDep,new byte[] {0x00, 0x00, 0x00});
            Log.i(TAG, "selectPICC: " + result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static boolean authenticatePICC(IsoDep isoDep){
        byte[] response = new byte[0];
        try {
            response = authenticate(isoDep,new byte[8], (byte) 0x00, KeyType.DES);
            Log.i(TAG, "authenticatePICC: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean authenticateApplicationByAES(IsoDep isoDep){
        byte[] response = new byte[0];
        try {
            response = authenticate(isoDep,new byte[16], (byte) 0x30, KeyType.AES);
            Log.i(TAG, "authenticateApplicationByAES: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean authenticateApplicationByDES(IsoDep isoDep){
        byte[] response = new byte[0];
        try {
            response = authenticate(isoDep,new byte[8], (byte) 0x00, KeyType.DES);
            Log.i(TAG, "authenticateApplicationByDES: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }




    public static byte[] getApplicationIds(IsoDep isoDep) throws IOException {
        try{
            byte[] response = isoDep.transceive(SELECT_APP_IDS);
            Log.i(TAG, "getApplicationIds: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00){
                if (response.length > 2) {
                    byte[] data = new byte[response.length - 2];
                    System.arraycopy(response, 0, data, 0, data.length);
                    return data;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
    public static byte[] getApplicationIdsAndDF(IsoDep isoDep) throws IOException {
        byte[] response = isoDep.transceive(SELECT_APP_DF);
        Log.i(TAG, "getApplicationIdsAndDF: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00){
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            return response;
        }

        return null;
    }
    public static byte[] getKeySettings(IsoDep isoDep) throws IOException {
        byte[] response = isoDep.transceive(GET_KEY_SETTINGS);
        Log.i(TAG, "getKeySettings: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00){
            byte[] data = new byte[response.length-2];
            System.arraycopy(response, 0, data, 0, data.length);
            return data;
        }

        return null;
    }

    public static boolean formatPICC(IsoDep isoDep) throws IOException {
        boolean result = true;
        byte[] response = isoDep.transceive(FORMAT_PICC);
        Log.i(TAG, "formatPICC: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code != 0x00)
            return false;
        return result;
    }

    public static String getVersion(IsoDep isoDep) throws IOException {
        String info= "";
        byte[] fullResp = new byte[7 + 7 + 14 + 8 + 2];
        int index = 0;

        // 1st frame
        byte[] apdu = new byte[] {
                (byte) 0x90,
                (byte) Command.GET_VERSION.getCode(),
                0x00,
                0x00,
                0x00
        };
        byte[] response = isoDep.transceive(apdu);

        String result = Utils.bytesToHexString(response);
        Log.d(TAG," getVersion = "+result);


        System.arraycopy(response, 0, fullResp, 0, response.length);

        index += response.length;

        byte code = response[response.length-1];
        if (Integer.toHexString(code&0xFF).equals("af")) {
            if(response[0] == 4){
                info+="NXP ";
            }
            int memoryFlag = response[5]/2;
            info+=""+(int)(Math.pow(2,memoryFlag))+"  ";
            // second frame
            apdu[1] = (byte) Command.MORE.getCode();
            response = isoDep.transceive(apdu);
            System.arraycopy(response, 0, fullResp, 7, response.length);
            index += response.length;
            result = Utils.bytesToHexString(response);
            Log.d(TAG," getVersion MORE = "+result);

            code = response[response.length-1];
            if (Integer.toHexString(code&0xFF).equals("af")) {
                // third frame
                response = isoDep.transceive(apdu);
                System.arraycopy(response, 0, fullResp, 14, response.length);
                index += response.length;
                result = Utils.bytesToHexString(response);
                Log.d(TAG," getVersion MORE2 = "+result);
                // postprocessing: doesn't always have a CMAC attached

                byte[] ret = new byte[index];
                System.arraycopy(fullResp, 0, ret, 0, index);
                info +=  Utils.bytesToHexString(fullResp);
                Log.d(TAG," getVersion = "+info);
            }
        }

        return info;
    }

    /**
     * Create a new application.
     * Requires the PICC-level AID to be selected (00 00 00
     *
     * @param aid	3-byte AID
     * @param amks	application master key settings
     * @param nok	number of keys (concatenated with 0x40 or 0x80 for 3K3DES and AES respectively)
     * @return		<code>true</code> on success, <code>false</code> otherwise
     */
    public static boolean createApplication(IsoDep isoDep,byte[] aid, byte amks, byte nok) throws IOException {
        byte[] apdu = new byte[11];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.CREATE_APPLICATION.getCode();
        apdu[4] = 0x05;
        System.arraycopy(aid, 0, apdu, 5, 3);
        apdu[8] = amks;
        apdu[9] = nok;
        byte[] response = isoDep.transceive(apdu);
        Log.i(TAG, "createApplication: " + Utils.bytesToHexString(aid)+" --- "+ Utils.bytesToHexString(response));
        if (response[1] == 0x00)
            return true;
        return false;
    }
    public static String deleteApplication(IsoDep isoDep,byte[] aid) throws IOException {
        boolean result = true;
        System.arraycopy(aid, 0, DELETE_APP, 5, 3);
        byte[] response = isoDep.transceive(DELETE_APP);
        Log.i(TAG, "deleteApplication: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code != 0x00)
            return "error code : "+ Utils.bytesToHexString(response);
        return "success";
    }
    public static boolean selectApplication(IsoDep isoDep,byte[] aid) {
        boolean result = false;
        try{
            byte[] apdu  = {
                    (byte) 0x90, // CLA = 00 (first interindustry command set)
                    (byte) 0x5A, // INS = A4 (SELECT)
                    (byte) 0x00,
                    (byte) 0x00,
                    3, // Lc  = 6  (data/AID has 6 bytes)
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00
            };;
            System.arraycopy(aid, 0, apdu, 5, 3);
            byte[] response = isoDep.transceive(apdu);
            Log.i(TAG, "selectApplication: " + Utils.bytesToHexString(aid)+" --- "+ Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00)
                result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private static boolean createStdDataFile(IsoDep isoDep,byte[] payload) throws IOException {
        return createDataFile(isoDep,payload, (byte) Command.CREATE_STD_DATA_FILE.getCode());
    }
    private static boolean createDataFile(IsoDep isoDep,byte[] payload, byte cmd) throws IOException {
        byte[] apdu = new byte[13];
        apdu[0] = (byte) 0x90;
        apdu[1] = cmd;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07;
        System.arraycopy(payload, 0, apdu, 5, 7);
        apdu[12] = 0x00;

        byte[] response = isoDep.transceive(apdu);
        Log.i(TAG, "createDataFile: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00)
            return true;
        return false;
    }
    public static byte[] getFilesIds(IsoDep isoDep) throws IOException {
        byte[] response = isoDep.transceive(GET_FILES_IDS);
        Log.i(TAG, "getFilesIds: " + Utils.bytesToHexString(response));
        byte code = response[response.length-1];
        if (code == 0x00){
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            return data;
        }

        return null;
    }
    /**
     * Set the version on a DES key. Each least significant bit of each byte of
     * the DES key, takes one bit of the version. Since the version is only
     * one byte, the information is repeated if dealing with 16/24-byte keys.
     *
     * @param a			1K/2K/3K 3DES
     * @param offset	start position of the key within a
     * @param length	key length
     * @param version	the 1-byte version
     */
    public static void setKeyVersion(byte[] a, int offset, int length, byte version) {
        if (length == 8 || length == 16 || length == 24) {
            for (int i = offset + length - 1, j = 0; i >= offset; i--, j = (j + 1) % 8) {
                a[i] &= 0xFE;
                a[i] |= ((version >>> j) & 0x01);
            }
        }
    }
    /**
     * Checks whether a 16-byte key is a 3DES key.
     * <p>
     * Some 3DES keys may actually be DES keys because the LSBit of
     * each byte is used for key versioning by MDF. A 16-byte key is
     * also a DES key if the first half of the key is equal to the second.
     *
     * @param key	the 16-byte 3DES key to check
     * @return		<code>true</code> if the key is a 3DES key
     */
    public static boolean isKey3DES(byte[] key) {
        if (key.length != 16)
            return false;
        byte[] tmpKey = Arrays.copyOfRange(key, 0, key.length);
        setKeyVersion(tmpKey, 0, tmpKey.length, (byte) 0x00);
        for (int i = 0; i < 8; i++)
            if (tmpKey[i] != tmpKey[i + 8])
                return true;
        return false;
    }
    public enum DESMode {
        SEND_MODE,
        RECEIVE_MODE;
    }
    /** Ciphers supported by DESFire EV1. */
    public enum KeyType {
        DES,
        TDES,
        TKTDES,
        AES;
    }
    // DES/3DES decryption: CBC send mode and CBC receive mode
    public static byte[] decrypt(byte[] key, byte[] data, DESMode mode) {
        byte[] modifiedKey = new byte[24];
        System.arraycopy(key, 0, modifiedKey, 16, 8);
        System.arraycopy(key, 0, modifiedKey, 8, 8);
        System.arraycopy(key, 0, modifiedKey, 0, key.length);

		/* MF3ICD40, which only supports DES/3DES, has two cryptographic
		 * modes of operation (CBC): send mode and receive mode. In send mode,
		 * data is first XORed with the IV and then decrypted. In receive
		 * mode, data is first decrypted and then XORed with the IV. The PCD
		 * always decrypts. The initial IV, reset in all operations, is all zeros
		 * and the subsequent IVs are the last decrypted/plain block according with mode.
		 *
		 * MDF EV1 supports 3K3DES/AES and remains compatible with MF3ICD40.
		 */
        byte[] ciphertext = new byte[data.length];
        byte[] cipheredBlock = new byte[8];

        switch (mode) {
            case SEND_MODE:
                // XOR w/ previous ciphered block --> decrypt
                for (int i = 0; i < data.length; i += 8) {
                    for (int j = 0; j < 8; j++) {
                        data[i + j] ^= cipheredBlock[j];
                    }
                    cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
                    System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
                }
                break;
            case RECEIVE_MODE:
                // decrypt --> XOR w/ previous plaintext block
                cipheredBlock = TripleDES.decrypt(modifiedKey, data, 0, 8);
                // implicitly XORed w/ IV all zeros
                System.arraycopy(cipheredBlock, 0, ciphertext, 0, 8);
                for (int i = 8; i < data.length; i += 8) {
                    cipheredBlock = TripleDES.decrypt(modifiedKey, data, i, 8);
                    for (int j = 0; j < 8; j++) {
                        cipheredBlock[j] ^= data[i + j - 8];
                    }
                    System.arraycopy(cipheredBlock, 0, ciphertext, i, 8);
                }
                break;
            default:
                System.err.println("Wrong way (decrypt)");
                return null;
        }

        return ciphertext;
    }
    // IV sent is the global one but it is better to be explicit about it: can be null for DES/3DES
    // if IV is null, then it is set to zeros
    // Sending data that needs encryption.
    public static byte[] send(byte[] key, byte[] data, KeyType type, byte[] iv) {
        switch (type) {
            case DES:
            case TDES:
                return decrypt(key, data, DESMode.SEND_MODE);
            case TKTDES:
                return TripleDES.encrypt(iv == null ? new byte[8] : iv, key, data);
            case AES:
                return AES.encrypt(iv == null ? new byte[16] : iv, key, data);
            default:
                return null;
        }
    }
    /**
     * Validates a key according with its type.
     *
     * @param key	the key
     * @param type	the type
     * @return		{@code true} if the key matches the type,
     * 				{@code false} otherwise
     */
    public static boolean validateKey(byte[] key, KeyType type) {
        if (type == KeyType.DES && (key.length != 8)
                || type == KeyType.TDES && (key.length != 16 || !isKey3DES(key))
                || type == KeyType.TKTDES && key.length != 24
                || type == KeyType.AES && key.length != 16) {
            System.err.println(String.format("Key validation failed: length is %d and type is %s", key.length, type));
            return false;
        }
        return true;
    }

    // rotate the array one byte to the left
    public static byte[] rotateLeft(byte[] a) {
        byte[] ret = new byte[a.length];

        System.arraycopy(a, 1, ret, 0, a.length - 1);
        ret[a.length - 1] = a[0];

        return ret;
    }
    // Receiving data that needs decryption.
    public static byte[] recv(byte[] key, byte[] data, KeyType type, byte[] iv) {
        switch (type) {
            case DES:
            case TDES:
                return decrypt(key, data, DESMode.RECEIVE_MODE);
            case TKTDES:
                return TripleDES.decrypt(iv == null ? new byte[8] : iv, key, data);
            case AES:
                return AES.decrypt(iv == null ? new byte[16] : iv, key, data);
            default:
                return null;
        }
    }
    public static byte[] authenticate(IsoDep isoDep,byte[] key, byte keyNo, KeyType type) throws IOException {

        if (!validateKey(key,type))
            return null;
        if (type != KeyType.AES) {
            // remove version bits from Triple DES keys
            setKeyVersion(key, 0, key.length, (byte) 0x00);
        }
        switch (type) {
            case DES:
            case TDES:
                AUTHOR[1] = (byte) Command.AUTHENTICATE_DES_2K3DES.getCode();
                break;
            case TKTDES:
                AUTHOR[1] = (byte) Command.AUTHENTICATE_3K3DES.getCode();
                break;
            case AES:
                AUTHOR[1] = (byte) Command.AUTHENTICATE_AES.getCode();
                break;
            default:
                assert false : type;
        }
        byte[] result = isoDep.transceive(AUTHOR);
        Log.i(TAG, "AUTHOR1:   " + Utils.bytesToHexString(result)+" result.length="+result.length);

        byte[] rankB = new byte[result.length - 2];
        System.arraycopy(result, 0, rankB, 0, rankB.length);

        // step 3
        final byte[] iv0 = type == KeyType.AES ? new byte[16] : new byte[8];
        byte[] randB = recv(new byte[8], rankB, type, iv0);
        if (randB == null)
            return null;
        byte[] randBr = rotateLeft(randB);
        byte[] randA = new byte[randB.length];
        SecureRandom g = new SecureRandom();
        g.nextBytes(randA);

        // step 3: encryption
        byte[] plaintext = new byte[randA.length + randBr.length];
        System.arraycopy(randA, 0, plaintext, 0, randA.length);
        System.arraycopy(randBr, 0, plaintext, randA.length, randBr.length);
        byte[] iv1 = Arrays.copyOfRange(rankB,randB.length - iv0.length, randB.length);
        byte[] ciphertext = send(key, plaintext, type, iv1);
        if (ciphertext == null)
            return null;

        // 2nd message exchange
        byte[] apdu = new byte[5 + ciphertext.length + 1];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0xAF;
        apdu[4] = (byte) ciphertext.length;
        System.arraycopy(ciphertext, 0, apdu, 5, ciphertext.length);
        result = isoDep.transceive(apdu);
        Log.i(TAG, "authenticate: " + Utils.bytesToHexString(result));
        return result;
    }

    public static boolean createStdFile(IsoDep isoDep, byte fileId) {
        boolean result = true;
        try{
//            result = createStdDataFile(isoDep,new byte[] {fileId, 0x00, 0x14, 0x32, 0x20, 0x00, 0x00});
            result = createStdDataFile(isoDep,new byte[] {fileId, 0x00, 0x0E, 0x0E, 0xF, 0x00, 0x00});
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static byte[] getFileSettings(IsoDep isoDep, byte fileNo) {
        byte[] response = null;
        byte[] apdu = new byte[7];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0xF5;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x01;
        apdu[5] = (byte) fileNo;
        apdu[6] = 0x00;
        try {
            response = isoDep.transceive(apdu);
            Log.i(TAG, "getFileSettings: " + Utils.bytesToHexString(response));
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    public static byte[] readData(IsoDep isoDep,byte fileId,byte[] offset,byte[] length){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] apdu = new byte[13];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0xBD;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07;
        apdu[5] = fileId;
        System.arraycopy(offset, 0, apdu, 6, offset.length);
        System.arraycopy(length, 0, apdu, 9, length.length);
        apdu[12] = 0x00;
        byte[] response = new byte[0];
        try {
            response = isoDep.transceive(apdu);
            byte[] data = new byte[response.length - 2];
            System.arraycopy(response, 0, data, 0, data.length);
            Log.i(TAG, "readData: " + Utils.bytesToHexString(response));
            baos.write(response);

            byte code = response[response.length-1];
            while (Integer.toHexString(code&0xFF).equals("af")) {
                apdu = new byte[] {(byte) 0x90, (byte) Command.MORE.getCode(), 0x00, 0x00, 0x00};
                try {
                    response = isoDep.transceive(apdu);
                    baos.write(response);
                    code = response[response.length-1];
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            byte[] result = baos.toByteArray();
            byte[] datas = new byte[result.length - 2];
            System.arraycopy(result,0,datas,0,datas.length);
            return datas;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String writeData(IsoDep isoDep,byte fileId,byte[] offset,byte[] length,byte[] data){
        String success = "";

        int payloadSent = 0;
        byte[] response = new byte[0];
        byte code;

        byte[] apdu = new byte[13+data.length];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0x3D;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte)(7+data.length);
        apdu[5] = fileId;
        System.arraycopy(offset, 0, apdu, 6, offset.length);
        System.arraycopy(length, 0, apdu, 9, length.length);
        System.arraycopy(data, 0, apdu, 12, data.length);

        try {
            response = isoDep.transceive(apdu);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "writeData result: " + Utils.bytesToHexString(response));
        code = response[response.length-1];
        if (code == 0x00) {
            success =  "success";
        } else if (Integer.toHexString(code&0xFF).equals("af")) {
            apdu[1] = (byte) Command.MORE.getCode();
            int totalPayload = 1+offset.length+length.length+data.length;

        } else {
            success = " fail, errorCode = "+ Utils.bytesToHexString(response);
        }
        return success;
    }

    public static boolean  deleteFile(IsoDep mIsoDep, byte fileId) {
        boolean success = false;
        byte[] apdu = new byte[] {
                (byte) 0x90,
                (byte) Command.DELETE_FILE.getCode(),
                0x00,
                0x00,
                0x01,
                fileId,
                0x00
        };
        try {
            byte[] response = new byte[0];
            response = mIsoDep.transceive(apdu);
            Log.i(TAG, "deleteFile result: " + Utils.bytesToHexString(response));
            byte code = response[response.length-1];
            if (code == 0x00) {
                success =  true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    public enum Command {

        // security-level
        AUTHENTICATE_DES_2K3DES		(0x0A),
        AUTHENTICATE_3K3DES			(0x1A),
        AUTHENTICATE_AES			(0xAA),
        CHANGE_KEY_SETTINGS			(0x54),
        SET_CONFIGURATION			(0x5C),
        CHANGE_KEY					(0xC4),
        GET_KEY_VERSION				(0x64),

        // PICC level
        CREATE_APPLICATION			(0xCA),
        DELETE_APPLICATION			(0xDA),
        GET_APPLICATIONS_IDS		(0x6A),
        FREE_MEMORY					(0x6E),
        GET_DF_NAMES				(0x6D),
        GET_KEY_SETTINGS			(0x45),
        SELECT_APPLICATION			(0x5A),
        FORMAT_PICC					(0xFC),
        GET_VERSION					(0x60),
        GET_CARD_UID				(0x51),

        // application level
        GET_FILE_IDS				(0x6F),
        GET_FILE_SETTINGS			(0xF5),
        CHANGE_FILE_SETTINGS		(0x5F),
        CREATE_STD_DATA_FILE		(0xCD),
        CREATE_BACKUP_DATA_FILE		(0xCB),
        CREATE_VALUE_FILE			(0xCC),
        CREATE_LINEAR_RECORD_FILE	(0xC1),
        CREATE_CYCLIC_RECORD_FILE	(0xC0),
        DELETE_FILE					(0xDF),

        // file level
        READ_DATA					(0xBD),
        WRITE_DATA					(0x3D),
        GET_VALUE					(0x6C),
        CREDIT						(0x0C),
        DEBIT						(0xDC),
        LIMITED_CREDIT				(0x1C),
        WRITE_RECORDS				(0x3B),
        READ_RECORDS				(0xBB),
        CLEAR_RECORD_FILE			(0xEB),
        COMMIT_TRANSACTION			(0xC7),
        ABORT_TRANSACTION			(0xA7),

        //TODO 9.1-2 section commands from SDS missing; other auth methods as well (e.g. AES)
        MORE						(0xAF),
        UNKNOWN_COMMAND				(1001);

        private int code;

        private Command(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        private static Command getCommand(int code) {
            for (Command c : Command.values())
                if (code == c.getCode())
                    return c;
            return UNKNOWN_COMMAND;
        }

    }
}


