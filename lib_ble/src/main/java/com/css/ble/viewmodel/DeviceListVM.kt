package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType

class DeviceListVM : BaseViewModel() {

    val _deviceInfos: MutableLiveData<MutableList<DeviceInfo>> = MutableLiveData<MutableList<DeviceInfo>>()

    data class DeviceInfo(val name: String, @DrawableRes val icon: Int) {
        val deviceType: DeviceType
            get() = when (icon) {
                R.mipmap.icon_weight -> DeviceType.WEIGHT
                else -> DeviceType.WHEEL
            }

        fun getBondDeviceData(): BondDeviceData? {
            return when (icon) {
                R.mipmap.icon_weight -> BondDeviceData.bondWeight
                R.mipmap.icon_abroller -> BondDeviceData.bondWheel
                else -> null
            }
        }
    }
}

