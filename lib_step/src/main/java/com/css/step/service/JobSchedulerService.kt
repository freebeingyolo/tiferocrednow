package com.css.step.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class JobSchedulerService: JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val intent = Intent(application, TodayStepService::class.java)
        application.startService(intent)

//        Toast.makeText(getApplicationContext(), "onStartJob", Toast.LENGTH_SHORT).show();


//        Toast.makeText(getApplicationContext(), "onStartJob", Toast.LENGTH_SHORT).show();
//        Logger.e(JobSchedulerService.TAG, "onStartJob")

        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}