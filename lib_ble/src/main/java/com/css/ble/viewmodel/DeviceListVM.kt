package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import kotlin.concurrent.thread

class DeviceListVM : BaseViewModel() {

    val _deviceInfos: MutableLiveData<MutableList<DeviceInfo>> by lazy {
        MutableLiveData<MutableList<DeviceInfo>>().apply {
            value = mutableListOf<DeviceInfo>().also {
                loadData()
            }
        }
    }

    fun loadData() {
        thread(true) {
            var d = DeviceInfo("体脂秤", R.mipmap.icon_weight)
            _deviceInfos.value!!.add(d)
            d = DeviceInfo("健腹轮", R.mipmap.icon_abroller)
            _deviceInfos.value!!.add(d)
            _deviceInfos.postValue(_deviceInfos.value)
        }
    }

    data class DeviceInfo(
        val name: String,
        @DrawableRes val icon: Int
    )
}

