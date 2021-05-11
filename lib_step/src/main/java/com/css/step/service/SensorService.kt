package com.css.step.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.css.step.R
import com.css.step.data.ConstantData

class SensorService : Service(), SensorEventListener {
    private val TAG = "SensorService"

    //传感器
    private var sensorManager: SensorManager? = null
    //计步传感器类型 0-counter 1-detector
    private var stepSensor = -1
    //系统计步器步数
    private var systemSteps: Int = 0


    override fun onCreate() {
        super.onCreate()
        Thread(Runnable { getStepDetector() }).start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // -------------适配 8.0 service------------------
        var notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var mChannel: NotificationChannel?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(ConstantData.CHANNEL_ID, ConstantData.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
            var notification: Notification = Notification.Builder(getApplicationContext(), ConstantData.CHANNEL_ID).build()
            startForeground(R.string.app_name, notification);
        }
        return START_STICKY
    }

    /**
     * 获取传感器实例
     */
    private fun getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null
        }
        // 获取传感器管理器的实例
        sensorManager = this
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //android4.4以后可以使用计步传感器
        if (Build.VERSION.SDK_INT >= 19) {
            addCountStepListener()
        }
    }

    /**
     * 添加传感器监听
     */
    private fun addCountStepListener() {
        val countSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (countSensor != null) {
            stepSensor = 0
            sensorManager!!.registerListener(
                this@SensorService,
                countSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else if (detectorSensor != null) {
            stepSensor = 1
            sensorManager!!.registerListener(
                this@SensorService,
                detectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        systemSteps = event!!.values[0].toInt()
        Log.d(TAG,"onSensorChanged  ： $systemSteps")
//        saveStepData()

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}