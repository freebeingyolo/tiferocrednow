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

        fun getBondDeviceData(): BondDeviceData {
            var cls: Class<out BondDeviceData>
            var key: String
            when (icon) {
                R.mipmap.icon_weight -> {
                    cls = BondDeviceData::class.java
                    key = WonderCoreCache.BOND_WEIGHT_INFO
                }
                else -> {
                    cls = BondDeviceData::class.java
                    key = WonderCoreCache.BOND_WHEEL_INFO
                }
            }
            return WonderCoreCache.getData(key, cls)
        }
    }
}

