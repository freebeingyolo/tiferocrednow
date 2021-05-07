package com.css.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.*

class TodayStepCounter : SensorEventListener{
    private val TAG = "TodayStepCounter"

    private var sOffsetStep:Float
    private var sCurrStep:Float
    private var mTodayDate: String? = null
    private var mCleanStep = true
    private var mShutdown = false

    /**用来标识对象第一次创建， */
    private var mCounterStepReset = true

    private var mContext: Context? = null
    private var mOnStepCounterListener: OnStepCounterListener? = null

    private var mSeparate = false
    private var mBoot = false

    private var mWakeLock: WakeLock? = null

    constructor(
        mContext: Context?,
        mOnStepCounterListener: OnStepCounterListener?,
        mSeparate: Boolean,
        mBoot: Boolean
    ) {
        this.mContext = mContext
        this.mOnStepCounterListener = mOnStepCounterListener
        this.mSeparate = mSeparate
        this.mBoot = mBoot
        sCurrStep =  PreferencesHelper().getCurrentStep(mContext)
        mCleanStep = PreferencesHelper().getCleanStep(mContext!!)
        mTodayDate = PreferencesHelper().getStepToday(mContext!!)
        sOffsetStep = PreferencesHelper().getStepOffset(mContext!!)
        mShutdown = PreferencesHelper().getShutdown(mContext!!)
        Logger().e(TAG, "mShutdown : $mShutdown")
        //开机启动监听到，一定是关机开机了
        if (mBoot || shutdownBySystemRunningTime()) {
            mShutdown = true
            mContext?.let { PreferencesHelper().setShutdown(it, mShutdown) }
            Logger().e(TAG, "开机启动监听到")
        }
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

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val counterStep = event.values[0].toInt()
            if (mCleanStep) {
                //TODO:只有传感器回调才会记录当前传感器步数，然后对当天步数进行清零，所以步数会少，少的步数等于传感器启动需要的步数，假如传感器需要10步进行启动，那么就少10步
                cleanStep(counterStep)
            } else {
                //处理关机启动
                if (mShutdown || shutdownByCounterStep(counterStep)) {
                    Logger().e(TAG, "onSensorChanged shutdown")
                    shutdown(counterStep)
                }
            }
            sCurrStep = counterStep - sOffsetStep
            if (sCurrStep < 0) {
                //容错处理，无论任何原因步数不能小于0，如果小于0，直接清零
                Logger().e(TAG, "容错处理，无论任何原因步数不能小于0，如果小于0，直接清零")
                cleanStep(counterStep)
            }
            mContext?.let { PreferencesHelper().setCurrentStep(it, sCurrStep.toFloat()) }
            mContext?.let { PreferencesHelper().setElapsedRealtime(it, SystemClock.elapsedRealtime()) }
            mContext?.let { PreferencesHelper().setLastSensorStep(it, counterStep.toFloat()) }
            Logger().e(
                TAG,
                "counterStep : $counterStep --- sOffsetStep : $sOffsetStep --- sCurrStep : $sCurrStep"
            )
            updateStepCounter()
        }
    }

    private fun cleanStep(counterStep: Int) {
        //清除步数，步数归零，优先级最高
        sCurrStep = 0f
        sOffsetStep = counterStep.toFloat()
        mContext?.let { PreferencesHelper().setStepOffset(it, sOffsetStep.toFloat()) }
        mCleanStep = false
        mContext?.let { PreferencesHelper().setCleanStep(it, mCleanStep) }
        Logger().e(TAG, "mCleanStep : " + "清除步数，步数归零")
    }

    private fun shutdown(counterStep: Int) {
        val tmpCurrStep = mContext?.let { PreferencesHelper().getCurrentStep(it) } as Int
        //重新设置offset
        sOffsetStep = (counterStep - tmpCurrStep).toFloat()
        mContext?.let { PreferencesHelper().setStepOffset(it, sOffsetStep.toFloat()) }
        mShutdown = false
        mContext?.let { PreferencesHelper().setShutdown(it, mShutdown) }
    }

    private fun shutdownByCounterStep(counterStep: Int): Boolean {
        if (mCounterStepReset) {
            //只判断一次
            if (counterStep < PreferencesHelper().getLastSensorStep(mContext!!)) {
                //当前传感器步数小于上次传感器步数肯定是重新启动了，只是用来增加精度不是绝对的
                Logger().e(TAG, "当前传感器步数小于上次传感器步数肯定是重新启动了，只是用来增加精度不是绝对的")
                return true
            }
            mCounterStepReset = false
        }
        return false
    }

    private fun shutdownBySystemRunningTime(): Boolean {
        if (mContext?.let { PreferencesHelper().getElapsedRealtime(it) }!! > SystemClock.elapsedRealtime()) {
            //上次运行的时间大于当前运行时间判断为重启，只是增加精度，极端情况下连续重启，会判断不出来
            Logger().e(TAG, "上次运行的时间大于当前运行时间判断为重启，只是增加精度，极端情况下连续重启，会判断不出来")
            return true
        }
        return false
    }

    private fun dateChangeCleanStep() {
        //时间改变了清零，或者0点分隔回调
        if (getTodayDate() != mTodayDate || mSeparate) {
            getLock(mContext)
            mCleanStep = true
            mContext?.let { PreferencesHelper().setCleanStep(it, mCleanStep) }
            mTodayDate = getTodayDate()
            mContext?.let { PreferencesHelper().setStepToday(it, mTodayDate) }
            mShutdown = false
            mContext?.let { PreferencesHelper().setShutdown(it, mShutdown) }
            mBoot = false
            mSeparate = false
            sCurrStep = 0f
            mContext?.let { PreferencesHelper().setCurrentStep(it, sCurrStep.toFloat()) }
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

    private fun updateStepCounter() {
        if (null != mOnStepCounterListener) {
            mOnStepCounterListener!!.onChangeStepCounter(sCurrStep.toInt())
        }
    }

    fun getCurrentStep(): Float {
        sCurrStep = PreferencesHelper().getCurrentStep(mContext)
        return sCurrStep
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    @Synchronized
    private fun getLock(context: Context?): WakeLock? {
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) mWakeLock!!.release()
            mWakeLock = null
        }
        if (mWakeLock == null) {
            val mgr = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
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
        return mWakeLock
    }
}