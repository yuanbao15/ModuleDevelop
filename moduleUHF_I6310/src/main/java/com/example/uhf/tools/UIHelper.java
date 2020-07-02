package com.example.uhf.tools;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.yuanbao.moduleuhf_i6310.R;


public class UIHelper {
   
	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}

	/**
     * 显示弹出框消息
     *
     * @param act
     * @param titleInt
     * @param messageInt
     * @param iconInt
     */
    public static void alert(Activity act, int titleInt, int messageInt,
                             int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(messageInt);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 显示弹出框消息
     *
     * @param act
     * @param titleInt
     * @param message
     * @param iconInt
     */
    public static void alert(Activity act, int titleInt, String message,
                             int iconInt) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle(titleInt);
            builder.setMessage(message);
            builder.setIcon(iconInt);

            builder.setNegativeButton(R.string.close, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
