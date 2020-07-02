package com.rfid.trans;

import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class StringUtility {
    public static boolean DEBUG = false;
    public static String TAG = "DeviceAPI_";

    public StringUtility() {
    }

    public static String byte2Bit(byte b) {
        return "" + (byte)(b >> 7 & 1) + (byte)(b >> 6 & 1) + (byte)(b >> 5 & 1) + (byte)(b >> 4 & 1) + (byte)(b >> 3 & 1) + (byte)(b >> 2 & 1) + (byte)(b >> 1 & 1) + (byte)(b >> 0 & 1);
    }

    public static byte BitToByte(String byteStr) {
        if (byteStr == null) {
            return 0;
        } else {
            int len = byteStr.length();
            if (len != 4 && len != 8) {
                return 0;
            } else {
                int re;
                if (len == 8) {
                    if (byteStr.charAt(0) == '0') {
                        re = Integer.parseInt(byteStr, 2);
                    } else {
                        re = Integer.parseInt(byteStr, 2) - 256;
                    }
                } else {
                    re = Integer.parseInt(byteStr, 2);
                }

                return (byte)re;
            }
        }
    }

    public static String bytes2HexString(byte[] b, int size) {
        String ret = "";

        try {
            for(int i = 0; i < size; ++i) {
                String hex = Integer.toHexString(b[i] & 255);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }

                ret = ret + hex.toUpperCase();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return ret;
    }

    public static String bytes2HexString2(byte[] b, int size) {
        StringBuffer sb = new StringBuffer();

        try {
            for(int i = 0; i < size; ++i) {
                String hex = Integer.toHexString(b[i] & 255);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }

                sb.append(hex.toUpperCase());
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return sb.toString();
    }

    public static String bytes2HexString(byte[] b) {
        return bytes2HexString(b, b.length);
    }

    public static String bytesToHexString(byte[] b, int size) {
        String ret = "";

        try {
            for(int i = 0; i < size; ++i) {
                String hex = byte2HexString(b[i]);
                ret = ret + hex;
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return ret;
    }

    public static byte[] charsTobytes(char[] c, int size) {
        if (c != null && c.length != 0) {
            byte[] b = new byte[size];

            try {
                for(int i = 0; i < size; ++i) {
                    b[i] = (byte)c[i];
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            return b;
        } else {
            return null;
        }
    }

    public static char[] bytesTochars(byte[] c, int size) {
        if (c != null && c.length != 0) {
            char[] b = new char[size];

            try {
                for(int i = 0; i < size; ++i) {
                    b[i] = (char)(c[i] & 255);
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            return b;
        } else {
            return null;
        }
    }

    public static String byte2HexString(byte b) {
        String ret = "";

        try {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }

            ret = ret + hex.toUpperCase();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return ret;
    }

    public static String chars2HexString(char[] c, int size) {
        StringBuffer sb = new StringBuffer();

        try {
           // int j = 0;

            for(int i = 0; i < size; ++i) {
                int j = Integer.valueOf(c[i]);
                String hex = Integer.toHexString(j);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }

                sb.append(hex.toUpperCase());
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return sb.toString();
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];

        for(int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte)((int)(num >> offset & 255L));
        }

        return byteNum;
    }

    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];

        for(int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte)(num >> offset & 255);
        }

        return byteNum;
    }

    public static long byteArrayTolong(byte[] byteArray) {
        byte[] a = new byte[8];
        int i = a.length - 1;

        for(int j = byteArray.length - 1; i >= 0; --j) {
            if (j >= 0) {
                a[i] = byteArray[j];
            } else {
                a[i] = 0;
            }

            --i;
        }

        long v0 = (long)(a[0] & 255) << 56;
        long v1 = (long)(a[1] & 255) << 48;
        long v2 = (long)(a[2] & 255) << 40;
        long v3 = (long)(a[3] & 255) << 32;
        long v4 = (long)(a[4] & 255) << 24;
        long v5 = (long)(a[5] & 255) << 16;
        long v6 = (long)(a[6] & 255) << 8;
        long v7 = (long)(a[7] & 255);
        return v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7;
    }

    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 255;
        addr |= bytes[1] << 8 & '\uff00';
        addr |= bytes[2] << 16 & 16711680;
        addr |= bytes[3] << 24 & -16777216;
        return addr;
    }

    public static byte[] Int2bytes(int value) {
        byte[] bytes = new byte[]{(byte)Integer.parseInt(String.valueOf(value >> 24 & 255), 16), (byte)Integer.parseInt(String.valueOf(value >> 16 & 255), 16), (byte)Integer.parseInt(String.valueOf(value >> 8 & 255), 16), (byte)Integer.parseInt(String.valueOf(value & 255), 16)};

        for(int i = 0; i < 4; ++i) {
            Log.i("StringUtility", "Int2bytes() bytes = " + String.format("%02x", bytes[i]));
        }

        return bytes;
    }

    public static long charArrayTolong(char[] array) {
        char[] a = new char[8];
        int i = a.length - 1;

        for(int j = array.length - 1; i >= 0; --j) {
            if (j >= 0) {
                a[i] = array[j];
            } else {
                a[i] = 0;
            }

            --i;
        }

        long v0 = (long)(a[0] & 255) << 56;
        long v1 = (long)(a[1] & 255) << 48;
        long v2 = (long)(a[2] & 255) << 40;
        long v3 = (long)(a[3] & 255) << 32;
        long v4 = (long)(a[4] & 255) << 24;
        long v5 = (long)(a[5] & 255) << 16;
        long v6 = (long)(a[6] & 255) << 8;
        long v7 = (long)(a[7] & 255);
        Log.i("StringUtility", v0 + "@" + v1 + "@" + v2 + "@" + v3 + "@" + v4 + "@" + v5 + "@" + v6 + "@" + v7);
        return v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7;
    }

    public static byte[] reverse(byte[] b) {
        byte[] temp = new byte[b.length];

        for(int i = 0; i < b.length; ++i) {
            temp[i] = b[b.length - 1 - i];
        }

        return temp;
    }

    private static final BigInteger readUnsignedShort(byte[] readBuffer) throws IOException {
        if (readBuffer != null && readBuffer.length >= 2) {
            byte[] uint64 = new byte[]{0, 0, 0};
            System.arraycopy(readBuffer, 0, uint64, 0, 2);
            return new BigInteger(reverse(uint64));
        } else {
            return new BigInteger("0");
        }
    }

    public static final BigInteger readUnsignedInt64(byte[] readBuffer) throws IOException {
        if (readBuffer != null && readBuffer.length >= 8) {
            byte[] uint64 = new byte[9];
            uint64[8] = 0;
            System.arraycopy(readBuffer, 0, uint64, 0, 8);
            return new BigInteger(reverse(uint64));
        } else {
            return new BigInteger("0");
        }
    }

    public static long chars2Long(char[] c, int start, int len) {
        byte[] bytes = getBytes(c);

        for(int i = 0; i < bytes.length; ++i) {
            Log.i("StringUtility", "chars2Long bytes[i]:" + bytes[i]);
        }

        return byteArrayTolong(bytes);
    }

    public static String char2HexString(char c) {
        char[] cs = new char[]{c};
        return chars2HexString(cs, 1);
    }

    public static boolean isOctNumber(String str) {
        boolean flag = false;
        int i = 0;

        for(int n = str.length(); i < n; ++i) {
            char c = str.charAt(i);
            if (c == '0' | c == '1' | c == '2' | c == '3' | c == '4' | c == '5' | c == '6' | c == '7' | c == '8' | c == '9') {
                flag = true;
            }
        }

        return flag;
    }

    /** @deprecated */
    @Deprecated
    public static boolean isHexNumber(String str) {
        boolean flag = false;

        for(int i = 0; i < str.length(); ++i) {
            char cc = str.charAt(i);
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4' || cc == '5' || cc == '6' || cc == '7' || cc == '8' || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C' || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a' || cc == 'b' || cc == 'c' || cc == 'c' || cc == 'd' || cc == 'e' || cc == 'f') {
                flag = true;
            }
        }

        return flag;
    }

    public static boolean isOctNumberRex(String str) {
        String validate = "\\d+";
        return str.matches(validate);
    }

    public static boolean isHexNumberRex(String str) {
        String validate = "(?i)[0-9a-f]+";
        return str.matches(validate);
    }

    public static char[] hexString2Chars(String s) {
        s = s.replace(" ", "");
        char[] bytes = new char[s.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            bytes[i] = (char)Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static boolean isDecimal(String decimal) {
        int len = decimal.length();
        int i = 0;

        char ch;
        do {
            if (i >= len) {
                return true;
            }

            ch = decimal.charAt(i++);
        } while(ch >= '0' && ch <= '9');

        return false;
    }

    public static byte[] hexString2Bytes(String s) {
        byte[] bytes = new byte[s.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            bytes[i] = (byte)Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    public static String int2HexString(int n) {
        String str = Integer.toHexString(n);
        int l = str.length();
        return l == 1 ? "0" + str : str.substring(l - 2, l);
    }

    public static String ints2HexString(int[] c, int size) {
        String ret = "";

        try {
         //   int j = 0;

            for(int i = 0; i < size; ++i) {
                int j = Integer.valueOf(c[i]);
                String hex = Integer.toHexString(j);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }

                ret = ret + hex.toUpperCase();
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return ret;
    }

    public static int string2Int(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception var3) {
            return defValue;
        }
    }

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }
}
