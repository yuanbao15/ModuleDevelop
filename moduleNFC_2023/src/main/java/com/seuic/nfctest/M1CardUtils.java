package com.seuic.nfctest;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class M1CardUtils {

    /**
     * 读指定扇区的指定块
     *
     * @param mifareClassic
     * @param sectorIndex   扇区索引 0~15(16个扇区)
     * @param blockIndex    块索引 0~3
     * @param keyA          密钥，可为空，没有使用默认的密钥
     * @return
     */

    public static byte[] readBlock(MifareClassic mifareClassic, int sectorIndex, int blockIndex, byte[] keyA) throws IOException {
        try {
            String metaInfo = "";
            mifareClassic.connect();
            int type = mifareClassic.getType();//获取TAG的类型
            int sectorCount = mifareClassic.getSectorCount();//获取TAG中包含的扇区数
            String typeS = getMifareClassicType(type);
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mifareClassic.getBlockCount() + "个块\n存储空间: " + mifareClassic.getSize() + "B\n";
            Log.e("kyl", metaInfo);
            byte[] data = null;
            String hexString = null;
            if (m1AuthByKey(mifareClassic, sectorIndex, keyA) || m1Auth(mifareClassic, sectorIndex)) {
                int bCount;
                int bIndex;
                bCount = mifareClassic.getBlockCountInSector(sectorIndex);//获得当前扇区的所包含块的数量；
                bIndex = mifareClassic.sectorToBlock(sectorIndex);//当前扇区的第1块的块号
                for (int i = 0; i < bCount; i++) {
                    data = mifareClassic.readBlock(bIndex);
                    hexString = DataUtil.bytesToHexString(data);
                    Log.e("kyl", sectorIndex + "扇区" + bIndex + "块：" + hexString);
                    if (blockIndex == i) {
                        break;
                    }
                    bIndex++;
                }
            } else {
                Log.e("kyl", "密码校验失败,扇区：" + sectorIndex);
            }
            return data;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (mifareClassic != null) {
                    mifareClassic.close();
                }

            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }


    /**
     * 读指定扇区的所有块
     *
     * @param mifareClassic
     * @param sectorIndex   扇区索引 0~15(16个扇区)
     * @param keyA          密钥，可为空，没有使用默认的密钥
     * @return
     */

    public static List<byte[]> readBlock(MifareClassic mifareClassic, int sectorIndex, byte[] keyA) throws IOException {
        List<byte[]> dataList = new ArrayList<byte[]>();
        try {
            String metaInfo = "";
            mifareClassic.connect();
            int type = mifareClassic.getType();//获取TAG的类型
            int sectorCount = mifareClassic.getSectorCount();//获取TAG中包含的扇区数
            String typeS = getMifareClassicType(type);
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mifareClassic.getBlockCount() + "个块\n存储空间: " + mifareClassic.getSize() + "B\n";
            Log.e("kyl", metaInfo);
            if (m1AuthByKey(mifareClassic, sectorIndex, keyA) || m1Auth(mifareClassic, sectorIndex)) {
                int bCount;
                int bIndex;
                bCount = mifareClassic.getBlockCountInSector(sectorIndex);//获得当前扇区的所包含块的数量；
                bIndex = mifareClassic.sectorToBlock(sectorIndex);//当前扇区的第1块的块号
                for (int i = 0; i < bCount; i++) {
                    byte[] data = mifareClassic.readBlock(bIndex);
                    String hexString = DataUtil.bytesToHexString(data);
                    Log.e("kyl", sectorIndex + "扇区" + bIndex + "块：" + hexString);
                    dataList.add(data);
                    bIndex++;
                }
            } else {
                Log.e("kyl", "密码校验失败,扇区：" + sectorIndex);
            }
            return dataList;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (mifareClassic != null) {
                    mifareClassic.close();
                }
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }


    /**
     * 读所有扇区的所有块
     *
     * @param mifareClassic
     * @param keyA          密钥，可为空，没有使用默认的密钥
     * @return
     */

    public static Map<Integer, List<byte[]>> readBlock(MifareClassic mifareClassic, byte[] keyA) throws IOException {
        try {
            Map<Integer, List<byte[]>> result = new HashMap<Integer, List<byte[]>>();
            String metaInfo = "";
            mifareClassic.connect();
            int type = mifareClassic.getType();//获取TAG的类型
            int sectorCount = mifareClassic.getSectorCount();//获取TAG中包含的扇区数,一般m1卡扇区数为16个
            String typeS = getMifareClassicType(type);
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mifareClassic.getBlockCount() + "个块\n存储空间: " + mifareClassic.getSize() + "B\n";
            Log.e("kyl", metaInfo);
            for (int j = 0; j < sectorCount; j++) {
                if (m1AuthByKey(mifareClassic, j, keyA) || m1Auth(mifareClassic, j)) {
                    List<byte[]> dataList = new ArrayList<byte[]>();
                    int bCount;
                    int bIndex;
                    bCount = mifareClassic.getBlockCountInSector(j);//获得当前扇区的所包含块的数量；
                    bIndex = mifareClassic.sectorToBlock(j);//当前扇区的第1块的块号
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mifareClassic.readBlock(bIndex);
                        String hexString = DataUtil.bytesToHexString(data);
                        Log.e("kyl", j + "扇区" + bIndex + "块：" + hexString);
                        dataList.add(data);
                        bIndex++;
                    }
                    result.put(j, dataList);
                } else {
                    Log.e("kyl", "密码校验失败,扇区：" + j);
                }
            }
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            try {
                if (mifareClassic != null) {
                    mifareClassic.close();
                }
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }


    /**
     * 获取m1卡类型
     *
     * @param type
     * @return
     */
    private static String getMifareClassicType(int type) {
        String str = null;
        switch (type) {
            case MifareClassic.TYPE_CLASSIC:
                str = "TYPE_CLASSIC";
                break;
            case MifareClassic.TYPE_PLUS:
                str = "TYPE_PLUS";
                break;
            case MifareClassic.TYPE_PRO:
                str = "TYPE_PRO";
                break;
            case MifareClassic.TYPE_UNKNOWN:
                str = "TYPE_UNKNOWN";
                break;
        }
        return str;
    }


    /**
     * 往指定扇区的指定块写数据
     *
     * @param tag
     * @param sectorIndex 扇区索引 0~15(16个扇区)
     * @param blockIndex  块索引 0~63
     * @param blockByte   写入数据必须是16字节
     * @return
     * @throws IOException
     */

    public static boolean writeBlock(Tag tag, int sectorIndex, int blockIndex, byte[] blockByte) throws IOException {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        try {
            mifareClassic.connect();
            if (m1Auth(mifareClassic, sectorIndex)) {
                mifareClassic.writeBlock(blockIndex, blockByte);
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try {
                mifareClassic.close();
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
        return true;
    }


    /**
     * 使用默认密码校验
     *
     * @param mifareClassic
     * @param position
     * @return
     * @throws IOException
     */
    public static boolean m1Auth(MifareClassic mifareClassic, int position) throws IOException {
        if (mifareClassic.authenticateSectorWithKeyA(position, MifareClassic.KEY_DEFAULT)) {
            return true;
        } else if (mifareClassic.authenticateSectorWithKeyB(position, MifareClassic.KEY_DEFAULT)) {
            return true;
        }
        return false;
    }


    /**
     * 使用指定密码校验
     *
     * @param mifareClassic
     * @param position
     * @param keyA
     * @return
     * @throws IOException
     */
    public static boolean m1AuthByKey(MifareClassic mifareClassic, int position, byte[] keyA) throws IOException {
        if (keyA != null && keyA.length == 6) {
            if (mifareClassic.authenticateSectorWithKeyA(position, keyA)) {
                return true;
            }
        }
        return false;
    }
}
