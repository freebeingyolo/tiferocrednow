package com.css.step

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class TodayStepManager {
    private val TAG = "TodayStepManager"
    private val JOB_ID = 100

    /**
     * 在程序的最开始调用，最好在自定义的application oncreate中调用
     *
     * @param application
     */
    fun init(application: Application) {
        StepAlertManagerUtils().set0SeparateAlertManager(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            initJobScheduler(application);
        }
        startTodayStepService(application)
    }

    fun startTodayStepService(application: Application) {
        val intent = Intent(application, TodayStepService::class.java)
        application.startService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initJobScheduler(application: Application) {
        Logger().e(TAG, "initJobScheduler")
        val jobScheduler =
            application.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val builder = JobInfo.Builder(
            JOB_ID,
            ComponentName(application.packageName, JobSchedulerService::class.java.name)
        )
        builder.setMinimumLatency(5000) // 设置任务运行最少延迟时间
            .setOverrideDeadline(60000) // 设置deadline，若到期还没有达到规定的条件则会开始执行
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // 设置网络条件
            .setRequiresCharging(true) // 设置是否充电的条件
            .setRequiresDeviceIdle(false) // 设置手机是否空闲的条件
        val resultCode = jobScheduler.schedule(builder.build())
        if (JobScheduler.RESULT_FAILURE == resultCode) {
            Logger().e(TAG, "jobScheduler 失败")
        }
    }
}