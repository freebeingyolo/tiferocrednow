package com.css.step.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.css.service.data.StepData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.css.step.ISportStepInterface
import com.css.step.R
import com.css.step.TodayStepCounter
import com.css.step.TodayStepDcretor
import com.css.step.data.ConstantData
import com.css.step.data.TodayStepData
import com.css.step.utils.BootstrapService
import com.css.step.utils.Logger
import com.css.step.utils.OnStepCounterListener
import com.css.step.utils.TodayStepDBHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class TodayStepService : Service(), Handler.Callback {
    private val TAG = "TodayStepService"
    //保存数据库频率
    private val DB_SAVE_COUNTER = 50
    private var currentNotifySteps: Int = 0
    private lateinit var stepData: StepData
    private var notificationIsOpen = true

    //传感器的采样周期，这里使用SensorManager.SENSOR_DELAY_FASTEST，如果使用SENSOR_DELAY_UI会导致部分手机后台清理内存之后传感器不记步
    private val SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST

    private val HANDLER_WHAT_SAVE_STEP = 0
    private val LAST_SAVE_STEP_DURATION = 5000

    val INTENT_NAME_0_SEPARATE = "intent_name_0_separate"
    val INTENT_NAME_BOOT = "intent_name_boot"

    var currentTimeSportStep = 0

    private var sensorManager: SensorManager? = null
    private var stepDetector: TodayStepDcretor? = null
    private var stepCounter: TodayStepCounter? = null

    private var nm: NotificationManager? = null
    var notification: Notification? = null
    private var builder: Notification.Builder? = null

    private var mSeparate = false
    private var mBoot = false

    private var mDbSaveCount = 0

    private var mTodayStepDBHelper: TodayStepDBHelper? = null

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                "android.intent.action.OPEN_NOTIFICATION" -> {
                    notificationIsOpen = true
                }
                "android.intent.action.CLOSE_NOTIFICATION" -> {
                    notificationIsOpen = false
                }
            }
        }
    }

    private val sHandler: Handler = Handler(this)

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            HANDLER_WHAT_SAVE_STEP -> {
                Logger().e(TAG, "HANDLER_WHAT_SAVE_STEP")
                mDbSaveCount = 0
                saveDb(true, currentTimeSportStep)
            }
            else -> {
            }
        }
        return false
    }

    override fun onCreate() {
        super.onCreate()
        stepData = WonderCoreCache.getData(CacheKey.STEP_DATA, StepData::class.java) ?: StepData()
        mTodayStepDBHelper = TodayStepDBHelper(applicationContext)
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        initNotification(currentTimeSportStep + defaultSteps())

        //广播
        val filter = IntentFilter()
        filter.addAction("android.intent.action.OPEN_NOTIFICATION")
        filter.addAction("android.intent.action.CLOSE_NOTIFICATION")
        registerReceiver(receiver, filter)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger().e(TAG, "onStartCommand:" + currentTimeSportStep)
        if (null != intent) {
            mSeparate = intent.getBooleanExtra(INTENT_NAME_0_SEPARATE, false)
            mBoot = intent.getBooleanExtra(INTENT_NAME_BOOT, false)
        }
        mDbSaveCount = 0
        updateNotification(currentTimeSportStep)

        //注册传感器
        startStepDetector()
        //TODO:测试数据Start
//        if(Logger.sIsDebug) {
//            if (!isStepCounter()) {
//                Toast.makeText(getApplicationContext(), "Lib 当前手机没有计步传感器", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "Lib 当前手机使用计步传感器", Toast.LENGTH_LONG).show();
//
//            }
//        }
        //TODO:测试数据End
        return START_STICKY
    }

    private fun initNotification(currentStep: Int) {
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //----------------  针对8.0 新增代码 --------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = Notification.Builder(this.applicationContext, ConstantData.CHANNEL_ID)
            val notificationChannel =
                NotificationChannel(
                    ConstantData.CHANNEL_ID,
                    ConstantData.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                )
            notificationChannel.enableLights(false) // 如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false) // 是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            nm?.createNotificationChannel(notificationChannel)
            builder?.setChannelId(ConstantData.CHANNEL_ID)
        } else {
            builder = Notification.Builder(this.applicationContext)
                .setVibrate(null)
                .setSound(null)
                .setLights(0, 0, 0)
        }
        builder!!.setPriority(Notification.PRIORITY_MIN)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent("com.css.Notification.action"),
            0
        )
        builder!!.setContentIntent(contentIntent)
        val smallIcon: Int =
            resources.getIdentifier("icon_step_small", "mipmap", packageName)
        if (0 != smallIcon) {
            Logger().e(TAG, "smallIcon")
            builder!!.setSmallIcon(smallIcon)
        } else {
            builder!!.setSmallIcon(R.mipmap.icon) // 设置通知小ICON
        }
        builder!!.setTicker(getString(R.string.app_name))
        builder!!.setContentTitle(
            getString(
                R.string.title_notification_bar,
                currentStep.toString()
            )
        )
        val km = getDistanceByStep(currentStep.toLong())
        val calorie = getCalorieByStep(currentStep.toLong())
        builder!!.setContentText("步行 $km km    消耗 $calorie kcal")

        //设置不可清除
        builder!!.setOngoing(true)
        notification = builder!!.build()
        //将Service设置前台，这里的id和notify的id一定要相同否则会出现后台清理内存Service被杀死通知还存在的bug
        startForeground(R.string.app_name, notification)
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        nm!!.notify(R.string.app_name, notification)
        stepData.todaySteps = currentStep
        WonderCoreCache.saveData(CacheKey.STEP_DATA, stepData)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Logger().e(TAG, "onBind:" + currentTimeSportStep)
        return mIBinder().asBinder()
    }

    private fun startStepDetector() {

//        getLock(this);

        //android4.4以后如果有stepcounter可以使用计步传感器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && isStepCounter()) {
            addStepCounterListener()
        } else {
            addBasePedoListener()
        }
    }

    private fun addStepCounterListener() {
        Logger().e(TAG, "addStepCounterListener")
        if (null != stepCounter) {
            Logger().e(TAG, "已经注册TYPE_STEP_COUNTER")
            currentTimeSportStep = stepCounter!!.getCurrentStep().toInt()
            updateNotification(currentTimeSportStep)
            return
        }
        val countSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: return
        stepCounter =
            TodayStepCounter(applicationContext, mOnStepCounterListener, mSeparate, mBoot)
        Logger().e(TAG, "countSensor")
        sensorManager!!.registerListener(stepCounter, countSensor, SAMPLING_PERIOD_US)
    }

    private fun addBasePedoListener() {
        Logger().e(TAG, "addBasePedoListener")
        if (null != stepDetector) {
            Logger().e(TAG, "已经注册TYPE_ACCELEROMETER")
            currentTimeSportStep = stepDetector!!.getCurrentStep()
            updateNotification(currentTimeSportStep)
            return
        }
        //没有计步器的时候开启定时器保存数据
        val sensor = sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        stepDetector = TodayStepDcretor(this, mOnStepCounterListener)
        Log.e(TAG, "TodayStepDcretor")
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        sensorManager!!.registerListener(stepDetector, sensor, SAMPLING_PERIOD_US)
    }

    override fun onDestroy() {
        Logger().e(TAG, "onDestroy:" + currentTimeSportStep)
        stopForeground(true)
//        unregisterReceiver(receiver)
        super.onDestroy()
    }


    override fun onUnbind(intent: Intent?): Boolean {
        Logger().e(TAG, "onUnbind:" + currentTimeSportStep)
        return super.onUnbind(intent)
    }

    /**
     * 步数每次回调的方法
     *
     * @param currentStep
     */
    private fun updateTodayStep(currentStep: Int) {
        currentTimeSportStep = currentStep
        updateNotification(currentTimeSportStep)
        saveStep(currentStep)
    }

    private fun saveStep(currentStep: Int) {
        sHandler.removeMessages(HANDLER_WHAT_SAVE_STEP)
        sHandler.sendEmptyMessageDelayed(HANDLER_WHAT_SAVE_STEP, LAST_SAVE_STEP_DURATION.toLong())
        if (DB_SAVE_COUNTER > mDbSaveCount) {
            mDbSaveCount++
            return
        }
        mDbSaveCount = 0
        saveDb(false, currentStep)
    }

    /**
     * @param handler     true handler回调保存步数，否false
     * @param currentStep
     */
    private fun saveDb(handler: Boolean, currentStep: Int) {
        val todayStepData = TodayStepData()
        todayStepData.setToday(getTodayDate())
        todayStepData.setDate(System.currentTimeMillis())
        todayStepData.setStep(currentStep.toLong())
        if (null != mTodayStepDBHelper) {
            Logger().e(TAG, "saveDb handler : $handler")
            if (!handler || !mTodayStepDBHelper!!.isExist(todayStepData)) {
                Logger().e(TAG, "saveDb currentStep : $currentStep")
                mTodayStepDBHelper!!.insert(todayStepData)
            }
        }
    }

    private fun cleanDb() {
        Logger().e(TAG, "cleanDb")
        mDbSaveCount = 0
        if (null != mTodayStepDBHelper) {
            mTodayStepDBHelper!!.deleteTable()
            mTodayStepDBHelper!!.createTable()
        }
    }

    private fun getTodayDate(): String? {
        val date = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    /**
     * 更新通知
     */
    private fun updateNotification(stepCount: Int) {
        this.stepData = WonderCoreCache.getData(CacheKey.STEP_DATA, StepData::class.java) ?: StepData()
        val realSteps = stepCount + defaultSteps()
        if (null == builder || null == nm) {
            return
        }
        builder!!.setContentTitle(getString(R.string.title_notification_bar, realSteps.toString()))
        val km = getDistanceByStep(realSteps.toLong())
        val calorie = getCalorieByStep(realSteps.toLong())
        builder!!.setContentText("步行 $km km    消耗 $calorie kcal")
        notification = builder!!.build()
        nm!!.notify(R.string.app_name, notification)
        currentNotifySteps = realSteps
//          EventBus.getDefault().post(EventMessage<StepData>(EventMessage.Code.MAIN_INDEX_BACK, StepData(1, 2, 3, "")))
        this.stepData.todaySteps = realSteps
        WonderCoreCache.saveData(CacheKey.STEP_DATA, stepData)
        if (!notificationIsOpen) {
            Log.d("0000", " stop notification ")
            val intentBootstrap = Intent(this, BootstrapService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intentBootstrap)
            } else {
                startService(intentBootstrap)
            }
        }
    }

    private fun isStepCounter(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
    }

    private fun isStepDetector(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
    }

    private val mOnStepCounterListener: OnStepCounterListener = object : OnStepCounterListener {
        override fun onChangeStepCounter(step: Int) {
            updateTodayStep(step)
        }

        override fun onStepCounterClean() {
            currentTimeSportStep = 0
            updateNotification(currentTimeSportStep)
            cleanDb()
        }
    }

    inner class mIBinder : ISportStepInterface.Stub() {
        val SPORT_DATE = "sportDate"
        val STEP_NUM = "stepNum"
        val DISTANCE = "km"
        val CALORIE = "kaluli"
        override fun getCurrentTimeSportStep(): Int {
            return currentNotifySteps
        }

        override fun getTodaySportStepArray(): Int {
            if (null != mTodayStepDBHelper) {
                val todayStepDataArrayList: List<TodayStepData>? =
                    mTodayStepDBHelper!!.getQueryAll()
                if (null == todayStepDataArrayList || 0 == todayStepDataArrayList.size) {
                    return defaultSteps()
                }
                val jsonArray = JSONArray()
                for (i in todayStepDataArrayList.indices) {
                    val todayStepData: TodayStepData = todayStepDataArrayList[i]
                    try {
                        val subObject = JSONObject()
                        subObject.put(
                            TodayStepDBHelper(applicationContext).TODAY,
                            todayStepData.getToday()
                        )
                        subObject.put(SPORT_DATE, todayStepData.getDate())
                        subObject.put(STEP_NUM, todayStepData.getStep())
                        subObject.put(DISTANCE, (todayStepData.getStep()))
                        subObject.put(CALORIE, getCalorieByStep(todayStepData.getStep()))
                        jsonArray.put(subObject)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                Logger().e(TAG, jsonArray.toString())
//                    initNotification(defaultSteps() + todayStepDataArrayList[todayStepDataArrayList.size - 1].getStep().toInt())
                //仅返回步数信息
                return defaultSteps() + todayStepDataArrayList[todayStepDataArrayList.size - 1].getStep()
                    .toInt()

                // 返回所有信息jsonArray
                //return jsonArray.toString()
            }

            return 0
        }

    }

    private fun defaultSteps(): Int {
        return stepData.defaultSteps
    }

    // 公里计算公式
    private fun getDistanceByStep(steps: Long): String {
        return String.format("%.2f", steps * 0.7f / 1000)
    }

    // 千卡路里计算公式
    private fun getCalorieByStep(steps: Long): String {
        return String.format("%.1f", steps * 0.7f * 60 * 1.036f / 1000)
    }

}