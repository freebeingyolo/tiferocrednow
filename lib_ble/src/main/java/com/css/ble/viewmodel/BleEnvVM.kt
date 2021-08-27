package com.css.ble.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.bus.LiveDataBus
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
            (bleObsrv as MutableLiveData).value = v
        }
    var locationPermission: Boolean
        get() = bleInitMap["locationPermission"] ?: false
        set(v) {
            bleInitMap["locationPermission"] = v
            if (bleInitMap.size == 3 && !isBleEnvironmentInit.value!!) isBleEnvironmentInit.value = true
            (locationPermissionObsrv as MutableLiveData).value = v
        }
    var locationOpened: Boolean
        get() = bleInitMap["locationOpened"] ?: false
        set(v) {
            bleInitMap["locationOpened"] = v
            if (bleInitMap.size == 3 && !isBleEnvironmentInit.value!!) isBleEnvironmentInit.value = true
            (locationOpenObsrv as MutableLiveData).value = v
        }
    val bleObsrv: LiveData<Boolean> by lazy { LiveDataBus.BusMutableLiveData(false) }
    val locationPermissionObsrv: LiveData<Boolean> by lazy { MutableLiveData() }
    val locationOpenObsrv: LiveData<Boolean> by lazy { MutableLiveData() }

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


}