package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel

class DeviceListVM : BaseViewModel() {

    val _deviceInfos: MutableLiveData<MutableList<DeviceInfo>> = MutableLiveData<MutableList<DeviceInfo>>()

    data class DeviceInfo(
        val name: String,
        @DrawableRes val icon: Int
    )
}

