package com.css.wondercorefit.ui.viewmodel

import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.service.utils.CacheKey

object DeviceInfoViewModel : BaseViewModel() {

//    private val str1 = BondDeviceData.getDevice(CacheKey.BOND_WEIGHT_INFO)!!
//    private val str2 = BondDeviceData.getDevice(CacheKey.BOND_WHEEL_INFO )!!

    private val str1 = "测试设备1"
    private val str2 = "测试设备2"

    val name = arrayListOf(str1, str2)
}