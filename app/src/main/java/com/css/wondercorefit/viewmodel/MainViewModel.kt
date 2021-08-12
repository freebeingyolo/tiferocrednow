package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.viewmodel.DeviceListVM
import com.css.service.data.DeviceData

class MainViewModel : BaseViewModel() {
    var deviceData = MutableLiveData<List<DeviceData>>()

    //加载设备
    fun loadDevice() {
        DeviceListVM().loadDeviceInfo(
            { msg, data ->
                //deviceData.value = data
            },
            { code, msg, d ->
                showToast(msg)
            })
    }

}