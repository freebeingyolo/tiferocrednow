package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author yuedong
 * @date 2021-06-15
 */
abstract class BaseWheelVM : BaseDeviceVM() {
    protected val TAG = javaClass.simpleName

    companion object {
        val UUID_SRVC = "85c60010-4d69-4b6a-afba-fe94fdd1beef"
        val UUID_WRITE = "85c60001-4d69-4b6a-afba-fe94fdd1beef"
        val UUID_NOTIFY = "85c60002-4d69-4b6a-afba-fe94fdd1beef"
        val FoundByName = "FoundByName"
        val FoundByUuid = "FoundByUuid"

        val UUID_SRVC2 = UUID.fromString(UUID_SRVC)
        val UUID_WRITE2 = UUID.fromString(UUID_WRITE)
        val UUID_NOTIFY2 = UUID.fromString(UUID_NOTIFY)
    }

    private var timeOutJob: Job? = null

    protected fun startTimeoutTimer(timeOut: Long) {
        if (timeOut == TIMEOUT_NEVER) return
        if (timeOutJob != null) {
            cancelTimeOutTimer()
            LogUtils.e(TAG, "timeOutJob not null,call cancelTimeOutTimer first", 3)
        }
        Log.d(TAG, "startTimeoutTimer")
        timeOutJob = viewModelScope.launch {
            delay(timeOut)
            timeOutJob = null
            onScanTimeOut()
        }
    }

    protected fun cancelTimeOutTimer() {
        if (timeOutJob != null) {
            LogUtils.d(TAG, "cancelTimeOutTimer")
            timeOutJob!!.cancel()
            timeOutJob = null
            onScanTimerOutCancel()
        }
    }

    open fun onScanTimeOut() {}
    open fun onScanTimerOutCancel() {}

}