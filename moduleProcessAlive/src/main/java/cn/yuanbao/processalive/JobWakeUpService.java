package cn.yuanbao.processalive;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

/**
 * @fileName JobWakeUpService
 *      保证在息屏后，CPU进入休眠状态时进行唤醒
 * @description 定时轮询，看服务是否挂掉，若挂掉则重启服务
 * Created by yuanbao on 2019/7/17 
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobWakeUpService extends JobService{
    private final int jobWakeUpId = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启一个轮寻
        JobInfo.Builder jobBuilder = new JobInfo.Builder(jobWakeUpId,new ComponentName(this,JobWakeUpService.class));
        jobBuilder.setPeriodic(2000);
        JobScheduler jobScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobBuilder.build());
        return START_STICKY; // 保持启动状态
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        //开启定时任务，定时轮寻 看WorkService有没有被杀死
        //如果杀死了启动 轮寻onStartJob
        boolean isAlive = isServiceAlive(WorkService.class.getName()); // 判断服务有没有在运行
        if (!isAlive){
            startService(new Intent(this, WorkService.class));
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     * serviceName 是包名+服务的类名 true表示正运行 false表示未运行
     */
    private boolean isServiceAlive(String serviceName) {
        boolean isWork = false;
        ActivityManager myAm = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAm.getRunningServices(100);
        if (myList.size()<0){
            return false;
        }
        // 遍历正在运行的服务，看自己的服务名是否在其中
        for (int i=0;i<myList.size();i++){
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)){
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
