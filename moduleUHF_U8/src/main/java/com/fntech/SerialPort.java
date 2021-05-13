package com.fntech;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = "FNSerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int databit, int stopbit, int parity) throws SecurityException, IOException {
        if (!device.canRead() || !device.canWrite()) {
            try {
                Process su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\nexit\n";
                su.getOutputStream().write(cmd.getBytes());
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception var8) {
                var8.printStackTrace();
                throw new SecurityException();
            }
        }

        this.mFd = OpenSerialPort(device.getAbsolutePath(), baudrate, databit, stopbit, parity);
        if (this.mFd == null) {
            throw new IOException();
        } else {
            this.mFileInputStream = new FileInputStream(this.mFd);
            this.mFileOutputStream = new FileOutputStream(this.mFd);
        }
    }

    public InputStream getInputStream() {
        return this.mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mFileOutputStream;
    }

    private static native FileDescriptor OpenSerialPort(String var0, int var1, int var2, int var3, int var4);

    public native void CloseSerialPort();

    static {
        System.loadLibrary("SerialPort");
    }
}
