package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import java.lang.IllegalArgumentException

/**
 * @author yuedong
 * @date 2021-05-18
 */
object BleEnvVM : BaseViewModel() {
    val bleEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val locationPermission: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val locationOpened: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isBleEnvironmentOk get() = bleEnabled.value!! && locationPermission.value!! && locationOpened.value!!

    val bleErrType
        get() = when {
            !bleEnabled.value!! -> ErrorType.BLE_OFF
            !locationPermission.value!! -> ErrorType.LOCATION_PERMISSION_OFF
            !locationOpened.value!! -> ErrorType.LOCATION_OFF
            else -> throw IllegalArgumentException("ble env is ok,no error msg")
        }

    val openBLE: LiveData<Boolean> by lazy { MutableLiveData() }
    val requestLocationPermission: LiveData<Boolean> by lazy { MutableLiveData() }
    val openLocation: LiveData<Boolean> by lazy { MutableLiveData() }

    //打开蓝牙,具体业务实现在BleEntry
    fun openBLE() {
        (openBLE as MutableLiveData).value = true
        (openBLE as MutableLiveData).value = false
    }

    //请求权限,具体业务实现在BleEntry
    fun requestLocationPermission() {
        (requestLocationPermission as MutableLiveData).value = true
        (requestLocationPermission as MutableLiveData).value = false
    }

    //打开定位,具体业务实现在BleEntry
    fun openLocation() {
        (openLocation as MutableLiveData).value = true
        (openLocation as MutableLiveData).value = false
    }

    fun doTimeout() {

    }

}