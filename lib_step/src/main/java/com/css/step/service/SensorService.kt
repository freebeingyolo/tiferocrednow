package com.css.step.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.css.step.R
import com.css.step.data.ConstantData
import com.css.step.data.StepEntity
import com.css.step.db.StepDataDao
import com.css.step.utils.TimeUtil

class SensorService : Service(), SensorEventListener {
    private val TAG = "SensorService"

    //传感器
    private var sensorManager: SensorManager? = null
    //系统计步器步数
    private var systemSteps: Int = 0
    //当前日期
    private var currentDate: String? = null
    //数据库
    private var stepDataDao: StepDataDao? = null
    //广播接收
    private var mInfoReceiver: BroadcastReceiver? = null


    override fun onCreate() {
        super.onCreate()
        initBroadcastReceiver()
        initTodayData()
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
     * 初始化广播
     */
    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN)
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT)
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIME_TICK)

        mInfoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, " onReceive  action  :  $intent.action ")
                when (intent.action) {
                    // 屏幕灭屏广播
                    Intent.ACTION_SCREEN_OFF -> saveStepData()
                    //关机广播，保存好当前数据
                    Intent.ACTION_SHUTDOWN -> saveStepData()
                    // 屏幕解锁广播
                    Intent.ACTION_USER_PRESENT -> saveStepData()
                    // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
                    // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
                    // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
                    Intent.ACTION_CLOSE_SYSTEM_DIALOGS -> saveStepData()
                    //监听日期变化
                    Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIME_TICK -> {
                        saveStepData()
                    }
                }
            }
        }
        //注册广播
        registerReceiver(mInfoReceiver, filter)
    }

    /**
     * 初始化当天数据
     */
    private fun initTodayData() {
        //获取当前时间
        currentDate = TimeUtil.getCurrentDate()
        //获取数据库
        stepDataDao = StepDataDao(applicationContext)
        Thread(Runnable { getStepDetector() }).start()
//        // 获取当天的数据，
//        val entity = stepDataDao!!.getCurDataByDate(currentDate!!)
//        // 为空则说明还没有该天的数据，有则说明已经开始当天的计步了
//        currentStep = if (entity == null) 0 else Integer.parseInt(entity.steps!!)
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
            sensorManager!!.registerListener(
                this@SensorService,
                countSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else if (detectorSensor != null) {
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
        saveStepData()

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    /**
     * 保存系统计步器数据到数据库中
     */
    private fun saveStepData() {
        //查询数据库中的数据
        var entity = stepDataDao?.getCurDataByDate(currentDate!!)
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        if (entity == null) {
            //没有则新建一条数据
            Log.d(TAG, "addNewData   :   $currentDate     $systemSteps ")
            entity = StepEntity()
            entity.curDate = currentDate
            entity.steps = systemSteps.toString()
            stepDataDao?.addNewData(entity)
        } else {
            //有则更新当前的数据
            Log.d(TAG, "updateCurData   :   $currentDate     $systemSteps ")
            entity.steps = systemSteps.toString()
            stepDataDao?.updateCurData(entity)
        }
    }

    override fun onDestroy() {
        Log.d(TAG,"onDestroy")
        stopForeground(true)
        unregisterReceiver(mInfoReceiver)
    }
}