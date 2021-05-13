package com.U8.reader;

public class Converter {
	public static final int LITTLE_ENDIAN = 0;
	public static final int BIG_ENDIAN = 1;
	
	public static byte[] getBytes(int number, int order) {
		int temp = number;
		byte[] b = new byte[4];
		
		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}
	
	public static byte[] getBytes(long number, int order) {
		long temp = number;
		byte[] b = new byte[8];
		
		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}
	
	public static byte[] getBytes(short number, int order) {
		int temp = number;
		byte[] b = new byte[8];
		
		if (order == LITTLE_ENDIAN) {
			for (int i = b.length - 1; i >= 0; i--) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				b[i] = new Long(temp & 0xff).byteValue();
				temp = temp >> 8;
			}
		}

		return b;

	}

	public static long byteToLong(byte[] b, int order) {
		long s = 0;
		long s0 = b[0] & 0xff;// 最低位
		long s1 = b[1] & 0xff;
		long s2 = b[2] & 0xff;
		long s3 = b[3] & 0xff;
		long s4 = b[4] & 0xff;// 最低位
		long s5 = b[5] & 0xff;
		long s6 = b[6] & 0xff;
		long s7 = b[7] & 0xff;

		if (order == LITTLE_ENDIAN) {
			s1 <<= 8 * 1;
			s2 <<= 8 * 2;
			s3 <<= 8 * 3;
			s4 <<= 8 * 4;
			s5 <<= 8 * 5;
			s6 <<= 8 * 6;
			s7 <<= 8 * 7;
		} else {
			s0 <<= 8 * 7;
			s1 <<= 8 * 6;
			s2 <<= 8 * 5;
			s3 <<= 8 * 4;
			s4 <<= 8 * 3;
			s5 <<= 8 * 2;
			s6 <<= 8 * 1;
		}
		s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;

		return s;
	}

	public static int byteToInt(byte[] b, int order) {
		int s = 0;
		int s0 = b[0] & 0xff;// 最低位
		int s1 = b[1] & 0xff;
		int s2 = b[2] & 0xff;
		int s3 = b[3] & 0xff;
		
		if (order == LITTLE_ENDIAN) {
			s2 <<= 8;
			s1 <<= 16;
			s0 <<= 24;
		} else {
			s3 <<= 24;
			s2 <<= 16;
			s1 <<= 8;
		}
		
		s = s0 | s1 | s2 | s3;

		return s;
	}

	public static short byteToShort(byte[] b, int order) {
		short s = 0;
		short s0 = (short) (b[0] & 0xff);// 最低位
		short s1 = (short) (b[1] & 0xff);
		
		if (order == LITTLE_ENDIAN) {
			s0 <<= 8;
			s = (short) (s0 | s1);
		} else {
			s1 <<= 8;
			s = (short) (s0 | s1);
		}
		
		return s;
	}
}
