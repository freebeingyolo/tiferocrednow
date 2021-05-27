package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.service.utils.WonderCoreCache

class DeviceListVM : BaseViewModel() {

    val _deviceInfos: MutableLiveData<MutableList<DeviceInfo>> = MutableLiveData<MutableList<DeviceInfo>>()

    data class DeviceInfo(
        val name: String,
        @DrawableRes val icon: Int
    ) {

        fun getBondDeviceData(): BondDeviceData? {
            return when (icon) {
                R.mipmap.icon_weight -> {
                    BondDeviceData.bondWeight
                }
                else -> {
                    BondDeviceData.bondWheel
                }
            }
        }
    }
}

