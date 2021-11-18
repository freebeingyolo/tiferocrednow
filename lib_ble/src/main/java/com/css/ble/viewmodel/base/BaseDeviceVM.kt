package com.css.ble.viewmodel.base

import LogUtils
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-06-09
 */
abstract class BaseDeviceVM : BaseViewModel() {
    protected val TAG = javaClass.simpleName
    abstract val deviceType: DeviceType

    companion object {
        const val TIMEOUT_NEVER = -1L
    }

    class BondDeviceInfo {
        var mac: String = ""
        var name: String? = null
        var isAilink: Boolean = false
        var manifactureHex: String = ""

        override fun toString(): String {
            return "BondDeviceInfo(mac='$mac', manifactureHex='$manifactureHex')"
        }
    }

    val weightKg get() = run {
        val weightData = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO).value
        weightData?.weightKg ?: 60f
    }
    open fun notifyWeightKgChange(wKg: Float){}

    protected var timeOutJob: Job? = null

    protected open fun startTimeoutTimer(timeOut: Long) {
        if (timeOut == TIMEOUT_NEVER) return
        if (timeOutJob != null) {
            cancelTimeOutTimer()
            LogUtils.e(TAG, "timeOutJob not null,call cancelTimeOutTimer first", 3)
        }
        timeOutJob = viewModelScope.launch {
            delay(timeOut)
            LogUtils.d(TAG,"onTimerTimeout#${timeOutJob?.hashCode()}")
            timeOutJob = null
            onTimerTimeout()
        }
        Log.d(TAG, "startTimeoutTimer#${timeOutJob.hashCode()}")
    }

    protected open fun cancelTimeOutTimer() {
        if (timeOutJob != null) {
            LogUtils.d(TAG, "cancelTimeOutTimer#${timeOutJob.hashCode()}")
            timeOutJob!!.cancel()
            timeOutJob = null
            onTimerCancel()
        }
    }

    abstract fun onTimerTimeout()
    abstract fun onTimerCancel()
    abstract fun disconnect()
    abstract fun connect()
    abstract fun release()
    abstract fun connectStateTxt(): String

}