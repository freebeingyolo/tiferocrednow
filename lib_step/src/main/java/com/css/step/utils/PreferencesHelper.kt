package com.css.step.utils

import android.content.Context
import android.content.SharedPreferences
import com.css.step.utils.Logger

class PreferencesHelper {
    private val TAG = "PreferencesHelper"

    val APP_SHARD = "today_step_share_prefs"

    // 上一次计步器的步数
    val LAST_SENSOR_TIME = "last_sensor_time"

    // 步数补偿数值，每次传感器返回的步数-offset=当前步数
    val STEP_OFFSET = "step_offset"

    // 当天，用来判断是否跨天
    val STEP_TODAY = "step_today"

    // 清除步数
    val CLEAN_STEP = "clean_step"

    // 当前步数
    val CURR_STEP = "curr_step"

    //手机关机监听
    val SHUTDOWN = "shutdown"

    //系统运行时间
    val ELAPSED_REALTIMEl = "elapsed_realtime"

    /**
     * Get SharedPreferences
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE)
    }

    fun setLastSensorStep(context: Context, lastSensorStep: Float) {
        Logger().e(TAG, "setLastSensorStep")
        getSharedPreferences(context).edit().putFloat(LAST_SENSOR_TIME, lastSensorStep).commit()
    }

    fun getLastSensorStep(context: Context): Float {
        Logger().e(TAG, "getLastSensorStep")
        return getSharedPreferences(context).getFloat(LAST_SENSOR_TIME, 0.0f)
    }

    fun setStepOffset(context: Context, stepOffset: Float) {
        Logger().e(TAG, "setStepOffset")
        getSharedPreferences(context).edit().putFloat(STEP_OFFSET, stepOffset).commit()
    }

    fun getStepOffset(context: Context): Float {
        Logger().e(TAG, "getStepOffset")
        return getSharedPreferences(context).getFloat(STEP_OFFSET, 0.0f)
    }

    fun setStepToday(context: Context, stepToday: String?) {
        Logger().e(TAG, "setStepToday")
        getSharedPreferences(context).edit().putString(STEP_TODAY, stepToday).commit()
    }

    fun getStepToday(context: Context): String? {
        Logger().e(TAG, "getStepToday")
        return getSharedPreferences(context).getString(STEP_TODAY, "")
    }

    /**
     * true清除步数从0开始，false否
     * @param context
     * @param cleanStep
     */
    fun setCleanStep(context: Context, cleanStep: Boolean) {
        Logger().e(TAG, "setCleanStep")
        getSharedPreferences(context).edit().putBoolean(CLEAN_STEP, cleanStep).commit()
    }

    /**
     * true 清除步数，false否
     * @param context
     * @return
     */
    fun getCleanStep(context: Context): Boolean {
        Logger().e(TAG, "getCleanStep")
        return getSharedPreferences(context).getBoolean(CLEAN_STEP, true)
    }

    fun setCurrentStep(context: Context, currStep: Float) {
        Logger().e(TAG, "setCurrentStep")
        getSharedPreferences(context).edit().putFloat(CURR_STEP, currStep).commit()
    }

    fun getCurrentStep(context: Context?): Float {
        Logger().e(TAG, "getCurrentStep")
        return getSharedPreferences(context!!).getFloat(CURR_STEP, 0.0f)
    }

    fun setShutdown(context: Context, shutdown: Boolean) {
        Logger().e(TAG, "setShutdown")
        getSharedPreferences(context).edit().putBoolean(SHUTDOWN, shutdown).commit()
    }

    fun getShutdown(context: Context): Boolean {
        Logger().e(TAG, "getShutdown")
        return getSharedPreferences(context).getBoolean(SHUTDOWN, false)
    }

    fun setElapsedRealtime(context: Context, elapsedRealtime: Long) {
        Logger().e(TAG, "setElapsedRealtime")
        getSharedPreferences(context).edit().putLong(ELAPSED_REALTIMEl, elapsedRealtime).commit()
    }

    fun getElapsedRealtime(context: Context): Long {
        Logger().e(TAG, "getElapsedRealtime")
        return getSharedPreferences(context).getLong(ELAPSED_REALTIMEl, 0L)
    }
}