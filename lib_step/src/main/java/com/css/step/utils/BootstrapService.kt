package com.css.step.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import com.css.service.data.UserData
import com.css.step.R
import com.css.step.data.ConstantData

class BootstrapService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeGround(this)
        stopSelf()
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun startForeGround (service: BootstrapService) {
        var notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var mChannel: NotificationChannel?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(
                ConstantData.CHANNEL_ID,
                ConstantData.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
            var notification: Notification =
                Notification.Builder(applicationContext, ConstantData.CHANNEL_ID).build()
            startForeground(R.string.app_name, notification)
        }
    }
}