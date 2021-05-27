package com.css.ble.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import java.lang.IllegalArgumentException

/**
 * @author yuedong
 * @date 2021-05-18
 */
object BleEnvVM : BaseViewModel() {
    var bleEnabled: Boolean
        get() = bleInitMap["bleEnabled"] ?: false
        set(v) {
            bleInitMap["bleEnabled"] = v
            if (bleInitMap.size == 3 && !isBleEnvironmentInit.value!!) isBleEnvironmentInit.value = true
        }
    var locationPermission: Boolean
        get() = bleInitMap["locationPermission"] ?: false
        set(v) {
            bleInitMap["locationPermission"] = v
            if (bleInitMap.size == 3 && !isBleEnvironmentInit.value!!) isBleEnvironmentInit.value = true
        }
    var locationOpened: Boolean
        get() = bleInitMap["locationOpened"] ?: false
        set(v) {
            bleInitMap["locationOpened"] = v
            if (bleInitMap.size == 3 && !isBleEnvironmentInit.value!!) isBleEnvironmentInit.value = true
        }
    val bleInitMap: MutableMap<String, Boolean> by lazy { mutableMapOf() }
    val isBleEnvironmentOk get() = isBleEnvironmentInit.value!! && bleEnabled && locationPermission && locationOpened
    val isBleEnvironmentInit: MutableLiveData<Boolean> by lazy { MutableLiveData(false) } //蓝牙权限和开关监听器

    val bleErrType
        get() = when {
            !bleEnabled -> ErrorType.BLE_OFF
            !locationPermission -> ErrorType.LOCATION_PERMISSION_OFF
            !locationOpened -> ErrorType.LOCATION_OFF
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