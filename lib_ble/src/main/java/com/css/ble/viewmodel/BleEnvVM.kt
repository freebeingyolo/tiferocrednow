package com.css.ble.viewmodel

import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil

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