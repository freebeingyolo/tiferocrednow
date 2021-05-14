package com.css.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.CountDownTimer
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import com.css.step.service.TodayStepService
import com.css.step.utils.Logger
import com.css.step.utils.OnStepCounterListener
import com.css.step.utils.PreferencesHelper
import java.text.SimpleDateFormat
import java.util.*

class TodayStepDcretor: SensorEventListener {

    private val TAG = "TodayStepDcretor"

    //存放三轴数据
    val valueNum = 5

    //用于存放计算阈值的波峰波谷差值
    var tempValue = FloatArray(valueNum)
    var tempCount = 0

    //是否上升的标志位
    var isDirectionUp = false

    //持续上升次数
    var continueUpCount = 0

    //上一点的持续上升的次数，为了记录波峰的上升次数
    var continueUpFormerCount = 0

    //上一点的状态，上升还是下降
    var lastStatus = false

    //波峰值
    var peakOfWave = 0f

    //波谷值
    var valleyOfWave = 0f

    //此次波峰的时间
    var timeOfThisPeak: Long = 0

    //上次波峰的时间
    var timeOfLastPeak: Long = 0

    //当前的时间
    var timeOfNow: Long = 0

    //当前传感器的值
    var gravityNew = 0f

    //上次传感器的值
    var gravityOld = 0f

    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    val initialValue = 1.7.toFloat()

    //初始阈值
    var ThreadValue = 2.0.toFloat()

    //初始范围
    var minValue = 11f
    var maxValue = 19.6f

    /**
     * 0-准备计时   1-计时中   2-正常计步中
     */
    private var CountTimeState = 0
    private var CURRENT_SETP = 0
    private var TEMP_STEP = 0
    private var lastStep = -1

    //用x、y、z轴三个维度算出的平均值
    private var average = 0f
    private var timer: Timer? = null

    // 倒计时3.5秒，3.5秒内不会显示计步，用于屏蔽细微波动
    private val duration: Long = 1500
    private var time: TimeCount? = null
    private var mOnStepCounterListener: OnStepCounterListener? = null
    private var mContext: Context? = null
    private var mTodayDate: String? = null

    private var mWakeLock: WakeLock? = null

    constructor(mContext: Context?, mOnStepCounterListener: OnStepCounterListener?) {
        this.mOnStepCounterListener = mOnStepCounterListener
        this.mContext = mContext
        CURRENT_SETP = mContext?.let { PreferencesHelper().getCurrentStep(it).toInt() }!!
        mTodayDate = PreferencesHelper().getStepToday(mContext!!)
        dateChangeCleanStep()
        initBroadcastReceiver()
        updateStepCounter()
    }

    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Intent.ACTION_TIME_TICK == intent.action || Intent.ACTION_TIME_CHANGED == intent.action) {
                    Logger().e(TAG, "ACTION_TIME_TICK")
                    //service存活做0点分隔
                    dateChangeCleanStep()
                }
            }
        }
        mContext!!.registerReceiver(mBatInfoReceiver, filter)
    }

    private fun dateChangeCleanStep() {
        //时间改变了清零，或者0点分隔回调
        if (getTodayDate() != mTodayDate) {
            getLock(mContext)
            CURRENT_SETP = 0
            mContext?.let { PreferencesHelper().setCurrentStep(it, CURRENT_SETP.toFloat()) }
            mTodayDate = getTodayDate()
            mContext?.let { PreferencesHelper().setStepToday(it, mTodayDate) }
            if (null != mOnStepCounterListener) {
                mOnStepCounterListener!!.onStepCounterClean()
            }
        }
    }

    private fun getTodayDate(): String {
        val date = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    override fun onAccuracyChanged(arg0: Sensor?, arg1: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        val sensor = event.sensor
        synchronized(this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event)
            }
        }
    }

    @Synchronized
    private fun calc_step(event: SensorEvent) {
        average = Math.sqrt(
            Math.pow(event.values[0].toDouble(), 2.0)
                    + Math.pow(event.values[1].toDouble(), 2.0) + Math.pow(
                event.values[2]
                    .toDouble(), 2.0
            )
        ).toFloat()
        detectorNewStep(average)
    }

    /*
     * 检测步子，并开始计步
	 * 1.传入sersor中的数据
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * */
    fun detectorNewStep(values: Float) {
        if (gravityOld == 0f) {
            gravityOld = values
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak
                timeOfNow = System.currentTimeMillis()
                if ((timeOfNow - timeOfLastPeak >= 200
                            ) && (peakOfWave - valleyOfWave >= ThreadValue) && ((timeOfNow - timeOfLastPeak) <= 2000)
                ) {
                    timeOfThisPeak = timeOfNow
                    //更新界面的处理，不涉及到算法
                    preStep()
                }
                if ((timeOfNow - timeOfLastPeak >= 200
                            && (peakOfWave - valleyOfWave >= initialValue))
                ) {
                    timeOfThisPeak = timeOfNow
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave)
                }
            }
        }
        gravityOld = values
    }

    private fun preStep() {
        if (CountTimeState == 0) {
            // 开启计时器
            time = TimeCount(duration, 500)
            time!!.start()
            CountTimeState = 1
            Log.v(TAG, "开启计时器")
        } else if (CountTimeState == 1) {
            TEMP_STEP++
            Log.v(TAG, "计步中 TEMP_STEP:$TEMP_STEP")
        } else if (CountTimeState == 2) {
            CURRENT_SETP++
            mContext?.let { PreferencesHelper().setCurrentStep(it, CURRENT_SETP.toFloat()) }
            updateStepCounter()
        }
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    fun DetectorPeak(newValue: Float, oldValue: Float): Boolean {
        lastStatus = isDirectionUp
        if (newValue >= oldValue) {
            isDirectionUp = true
            continueUpCount++
        } else {
            continueUpFormerCount = continueUpCount
            continueUpCount = 0
            isDirectionUp = false
        }

//        Log.v(TAG, "oldValue:" + oldValue);
        if ((!isDirectionUp && lastStatus
                    && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue)))
        ) {
            peakOfWave = oldValue
            return true
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue
            return false
        } else {
            return false
        }
    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    fun Peak_Valley_Thread(value: Float): Float {
        var tempThread = ThreadValue
        if (tempCount < valueNum) {
            tempValue[tempCount] = value
            tempCount++
        } else {
            tempThread = averageValue(tempValue, valueNum)
            for (i in 1 until valueNum) {
                tempValue[i - 1] = tempValue[i]
            }
            tempValue[valueNum - 1] = value
        }
        return tempThread
    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    fun averageValue(value: FloatArray, n: Int): Float {
        var ave = 0f
        for (i in 0 until n) {
            ave += value[i]
        }
        ave = ave / valueNum
        if (ave >= 8) {
            Log.v(TAG, "超过8")
            ave = 4.3.toFloat()
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8")
            ave = 3.3.toFloat()
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7")
            ave = 2.3.toFloat()
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4")
            ave = 2.0.toFloat()
        } else {
            Log.v(TAG, "else")
            ave = 1.7.toFloat()
        }
        return ave
    }

    inner class TimeCount(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            // 如果计时器正常结束，则开始计步
            time?.cancel()
            CURRENT_SETP += TEMP_STEP
            lastStep = -1
            //            CountTimeState = 2;
            Log.v(TAG, "计时正常结束")
            timer = Timer(true)
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    if (lastStep == CURRENT_SETP) {
                        timer!!.cancel()
                        CountTimeState = 0
                        lastStep = -1
                        TEMP_STEP = 0
                        Log.v(TAG, "停止计步：$CURRENT_SETP")
                    } else {
                        lastStep = CURRENT_SETP
                    }
                }
            }
            timer!!.schedule(task, 0, 2000)
            CountTimeState = 2
        }

        override fun onTick(millisUntilFinished: Long) {
            if (lastStep == TEMP_STEP) {
                Log.v(TAG, "onTick 计时停止")
                time!!.cancel()
                CountTimeState = 0
                lastStep = -1
                TEMP_STEP = 0
            } else {
                lastStep = TEMP_STEP
            }
        }
    }

    fun getCurrentStep(): Int {
        CURRENT_SETP = mContext?.let { PreferencesHelper().getCurrentStep(it).toInt() }!!
        return CURRENT_SETP
    }

    private fun updateStepCounter() {
        if (null != mOnStepCounterListener) {
            mOnStepCounterListener!!.onChangeStepCounter(CURRENT_SETP)
        }
    }

    @Synchronized
    private fun getLock(context: Context?): WakeLock? {
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) mWakeLock!!.release()
            mWakeLock = null
        }
        if (mWakeLock == null) {
            val mgr = context!!
                .getSystemService(Context.POWER_SERVICE) as PowerManager
            mWakeLock = mgr.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                TodayStepService::class.java.name
            )
            mWakeLock?.setReferenceCounted(true)
            val c = Calendar.getInstance()
            c.timeInMillis = System.currentTimeMillis()
            val hour = c[Calendar.HOUR_OF_DAY]
            if (hour >= 23 || hour <= 6) {
                mWakeLock?.acquire(5000)
            } else {
                mWakeLock?.acquire(300000)
            }
        }
        return (mWakeLock)
    }

}