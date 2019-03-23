package com.epichust.modulepdf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.artifex.mupdf.MuPDFCore;
import com.artifex.mupdf.MuPDFPageAdapter;
import com.artifex.mupdf.ReaderView;
import com.artifex.mupdf.SavePdf;


/**
 * Created by yuanbao on 2018/4/16.
 */     //bug:标注的区域应该只能在pdf里面，不可在菜单上。
public class PdfActivity extends Activity implements View.OnClickListener{

    //几个按钮的id，后面写定义和监听方法。
    ReaderView readerView;
    RelativeLayout rlSign;
    RelativeLayout rlClear;
    RelativeLayout rlSave;

    String in_path;
    String out_path;
    SignatureView signView;
    float density; // 屏幕分辨率密度
    MuPDFCore muPDFCore;
    Save_Pdf save_pdf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);      //测试页面activity_pdf_test是好的

        rlSign = (RelativeLayout) findViewById(R.id.rl_sign);
        rlSign.setOnClickListener(this);
        rlClear = (RelativeLayout) findViewById(R.id.rl_clear);
        rlClear.setOnClickListener(this);
        rlSave = (RelativeLayout) findViewById(R.id.rl_save);
        rlSave.setOnClickListener(this);

        signView = (SignatureView)findViewById(R.id.sign);

        // 计算分辨率密度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        density = metric.density;

//		in_path = "/storage/self/primary/" + "123.pdf";
//		out_path = "/storage/self/primary/" + "ttt.pdf";

//		in_path = "/storage/emulated/0/" + "123.pdf";       //这个是手机地址yb可用
//		out_path = "/storage/emulated/0/" + "ttt.pdf";

//GIONEE		in_path = "/storage/emulated/legacy/" + "123.pdf";
//GIONEE		out_path = "/storage/emulated/legacy/" + "ttt.pdf";

//        in_path = Environment.getExternalStorageDirectory().toString() + "/UZMap" + "/123.pdf";
        //作为module开发后，其路径由intent传参过来
        Intent data = getIntent();
        in_path = data.getStringExtra("filePath");
        if(in_path != null){
            Toast.makeText(getApplicationContext(), "传递值："+in_path, Toast.LENGTH_SHORT).show();
        }

        out_path = Environment.getExternalStorageDirectory().toString() + "/UZMap" + "/ttt.pdf";

        try {
            readerView = (ReaderView)findViewById(R.id.reader);
            //先测试是否跳转能打开这个activity，不做耗时操作，显示页面即可

            muPDFCore = new MuPDFCore(in_path);         // PDF的文件路径
            readerView.setAdapter(new MuPDFPageAdapter(this, muPDFCore));
            readerView.setDisplayedViewIndex(0);        //展示第一页
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PdfActivity","读取pdf未成功");
        }
    }


    //报错，在library module模块中定义R.id什么的，
    // 不能用switch case语句，只能用if else。。或者写onclick方法体，较多不考虑
    @Override
    public void onClick(View v) {
        //三个按钮：标记、清除、保存
        if(v.getId() == R.id.rl_sign){
            if (rlSave.getVisibility() == View.GONE) {
                signView.setVisibility(View.VISIBLE);
                rlSave.setVisibility(View.VISIBLE);
                rlClear.setVisibility(View.VISIBLE);
            } else {
                signView.clear();
                signView.setVisibility(View.GONE);
                rlSave.setVisibility(View.GONE);
                rlClear.setVisibility(View.GONE);
            }
        }else if(v.getId() == R.id.rl_clear){
            signView.clear();
        }else if(v.getId() == R.id.rl_save){
            float scale = readerView.getmScale();// /得到放大因子
            SavePdf savePdf = new SavePdf(in_path, out_path);
            savePdf.setScale(scale);
            savePdf.setPageNum(readerView.getDisplayedViewIndex() + 1);

            savePdf.setWidthScale(1.0f * readerView.scrollX
                    / readerView.getDisplayedView().getWidth());// 计算宽偏移的百分比
            savePdf.setHeightScale(1.0f * readerView.scrollY
                    / readerView.getDisplayedView().getHeight());// 计算长偏移的百分比

            savePdf.setDensity(density);
            Bitmap bitmap = Bitmap.createBitmap(signView.getWidth(),
                    signView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            signView.draw(canvas);
            savePdf.setBitmap(bitmap);
            save_pdf = new Save_Pdf(savePdf);
            save_pdf.execute();
            signView.clear();
            signView.setVisibility(View.GONE);
            rlSave.setVisibility(View.GONE);
            rlClear.setVisibility(View.GONE);
        }


        /*switch (view.getId()) {
            case R.id.rl_sign:
                if (rlSave.getVisibility() == View.GONE) {
                    signView.setVisibility(View.VISIBLE);
                    rlSave.setVisibility(View.VISIBLE);
                    rlClear.setVisibility(View.VISIBLE);
                } else {
                    signView.clear();
                    signView.setVisibility(View.GONE);
                    rlSave.setVisibility(View.GONE);
                    rlClear.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_clear:
                signView.clear();
                break;
            case R.id.rl_save:
                float scale = readerView.getmScale();// /得到放大因子
                SavePdf savePdf = new SavePdf(in_path, out_path);
                savePdf.setScale(scale);
                savePdf.setPageNum(readerView.getDisplayedViewIndex() + 1);

                savePdf.setWidthScale(1.0f * readerView.scrollX
                        / readerView.getDisplayedView().getWidth());// 计算宽偏移的百分比
                savePdf.setHeightScale(1.0f * readerView.scrollY
                        / readerView.getDisplayedView().getHeight());// 计算长偏移的百分比

                savePdf.setDensity(density);
                Bitmap bitmap = Bitmap.createBitmap(signView.getWidth(),
                        signView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                signView.draw(canvas);
                savePdf.setBitmap(bitmap);
                save_pdf = new Save_Pdf(savePdf);
                save_pdf.execute();
                signView.clear();
                signView.setVisibility(View.GONE);
                rlSave.setVisibility(View.GONE);
                rlClear.setVisibility(View.GONE);
                break;

            default:
                break;
        }*/
    }

    /*
     * 用于存储的异步,并上传更新
     */
    class Save_Pdf extends AsyncTask<String, String, String> {

        private SavePdf savePdf;
        private AlertDialog dialog;

        public Save_Pdf(SavePdf savePdf) {
            this.savePdf = savePdf;
            dialog = new AlertDialog.Builder(PdfActivity.this)
                    .setTitle("正在存储...").create();
        }

        @Override
        protected String doInBackground(String... params) {
            savePdf.addText();
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected void onPostExecute(String o) {
            try {
                muPDFCore = new MuPDFCore(out_path);
                readerView.setAdapter(new MuPDFPageAdapter(PdfActivity.this,
                        muPDFCore));

                String temp = in_path;
                in_path = out_path;
                out_path = temp;
                readerView.setmScale(1.0f);
                readerView.setDisplayedViewIndex(readerView
                        .getDisplayedViewIndex());
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(save_pdf != null){
            save_pdf.cancel(true);
        }
    }
}
