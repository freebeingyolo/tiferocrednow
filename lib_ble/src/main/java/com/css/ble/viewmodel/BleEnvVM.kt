package com.css.ble.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel

/**
 * @author yuedong
 * @date 2021-05-18
 */
open class BleEnvVM : BaseViewModel() {
    val bleEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val locationPermission: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val locationOpened: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val isBleEnvironmentOk get() = bleEnabled.value!! && locationPermission.value!! && locationOpened.value!!

}