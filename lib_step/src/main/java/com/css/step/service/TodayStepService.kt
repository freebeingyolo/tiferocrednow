package com.css.step.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.util.Log
import com.css.step.*
import com.css.step.data.ConstantData
import com.css.step.data.TodayStepData
import com.css.step.db.StepDataDao
import com.css.step.utils.Logger
import com.css.step.utils.OnStepCounterListener
import com.css.step.utils.TimeUtil
import com.css.step.utils.TodayStepDBHelper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class TodayStepService: Service(), Handler.Callback {
    private val TAG = "TodayStepService"

    //保存数据库频率
    private val DB_SAVE_COUNTER = 50


    //当前日期
    private var currentDate: String? = null
    //昨天日期
    private var yesterdayDate: String? = null
    //当前步数
    private var currentStep: Int = 0
    //数据库
    private var stepDataDao: StepDataDao? = null

    //传感器的采样周期，这里使用SensorManager.SENSOR_DELAY_FASTEST，如果使用SENSOR_DELAY_UI会导致部分手机后台清理内存之后传感器不记步
    private val SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST

    private val HANDLER_WHAT_SAVE_STEP = 0
    private val LAST_SAVE_STEP_DURATION = 5000

    private val BROADCAST_REQUEST_CODE = 100

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
        initCurrentSteps()
        mTodayStepDBHelper = TodayStepDBHelper(applicationContext)
        sensorManager = this
            .getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        initNotification(currentTimeSportStep  + defaultSteps())

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
            notificationChannel.enableLights(false)//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false)//是否显示角标
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            nm?.createNotificationChannel(notificationChannel)
            builder?.setChannelId(ConstantData.CHANNEL_ID)
        } else {
            builder = Notification.Builder(this.applicationContext)
        }
        builder!!.setPriority(Notification.PRIORITY_MIN)

        val receiverName: String? = getReceiver(applicationContext)
        var contentIntent = PendingIntent.getBroadcast(
            this,
            BROADCAST_REQUEST_CODE,
            Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (!TextUtils.isEmpty(receiverName)) {
            contentIntent = try {
                PendingIntent.getBroadcast(
                    this,
                    BROADCAST_REQUEST_CODE,
                    Intent(this, Class.forName(receiverName!!)),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            } catch (e: Exception) {
                e.printStackTrace()
                PendingIntent.getBroadcast(
                    this,
                    BROADCAST_REQUEST_CODE,
                    Intent(),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
        builder!!.setContentIntent(contentIntent)
        val smallIcon: Int =
            getResources().getIdentifier("icon_step_small", "mipmap", getPackageName())
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
        val intent = Intent(this, TodayStepService::class.java)
        startService(intent)
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
    }

    private fun isStepCounter(): Boolean {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
    }

    private fun isStepDetector(): Boolean {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
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


    inner class mIBinder : ISportStepInterface.Stub(){
        val SPORT_DATE = "sportDate"
        val STEP_NUM = "stepNum"
        val DISTANCE = "km"
        val CALORIE = "kaluli"
        override fun getCurrentTimeSportStep(): Int {
            return currentTimeSportStep
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
                    return defaultSteps() + todayStepDataArrayList[todayStepDataArrayList.size - 1].getStep().toInt()

                    // 返回所有信息jsonArray
                    //return jsonArray.toString()
                }

                return 0
            }

    }

    private fun initCurrentSteps() {
        //获取当前时间
        currentDate = TimeUtil.getCurrentDate()
        //获取昨天时间
        val preCalendar = Calendar.getInstance()
        preCalendar.add(Calendar.DATE, -1)
        yesterdayDate = SimpleDateFormat("yyyy年MM月dd日").format(preCalendar.time)
        //获取数据库
        stepDataDao = StepDataDao(applicationContext)
    }

    private fun defaultSteps(): Int {
        var currentEntity = stepDataDao?.getCurDataByDate(currentDate!!)?.steps
        var yesterdayEntity = stepDataDao?.getCurDataByDate(yesterdayDate!!)?.steps
        if (yesterdayEntity == null) {
            yesterdayEntity = currentEntity
        }
        val defaultSteps:Int = (currentEntity?.toInt())!! - (yesterdayEntity?.toInt()!!)
        Log.d(TAG , " defaultSteps   :  $defaultSteps")
        return (defaultSteps * 0.8f).toInt()
    }

    // 公里计算公式
    private fun getDistanceByStep(steps: Long): String {
        return String.format("%.2f", steps * 0.7f / 1000)
    }

    // 千卡路里计算公式
    private fun getCalorieByStep(steps: Long): String {
        return String.format("%.1f", steps * 0.7f * 60 * 1.036f / 1000)
    }

    fun getReceiver(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_RECEIVERS
            )
            val activityInfos = packageInfo.receivers
            if (null != activityInfos && activityInfos.size > 0) {
                for (i in activityInfos.indices) {
                    val receiverName = activityInfos[i].name
                    var superClazz = Class.forName(receiverName).superclass
                    var count = 1
                    while (null != superClazz) {
                        if (superClazz.name == "java.lang.Object") {
                            break
                        }
                        if (superClazz.name == BaseClickBroadcast::class.java.name) {
                            Log.e(TAG, "receiverName : $receiverName")
                            return receiverName
                        }
                        if (count > 20) {
                            //用来做容错，如果20个基类还不到Object直接跳出防止while死循环
                            break
                        }
                        count++
                        superClazz = superClazz.superclass
                        Log.e(TAG, "superClazz : $superClazz")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}