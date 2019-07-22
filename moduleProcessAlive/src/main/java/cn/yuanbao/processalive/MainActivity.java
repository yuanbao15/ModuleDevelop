package cn.yuanbao.processalive;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * @fileName MainActivity
 * @description 程序入口：实现进程保活，暂时实现了双进程守护、JobService 检测与拉起
 * Created by yuanbao on 2019/7/17
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, WorkService.class));
        startService(new Intent(this, DaemonService.class));
        // 定时任务保证服务常驻进程
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // api版本不低于21
            startService(new Intent(this, JobWakeUpService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("TAG","onDestroy");
    }
}
