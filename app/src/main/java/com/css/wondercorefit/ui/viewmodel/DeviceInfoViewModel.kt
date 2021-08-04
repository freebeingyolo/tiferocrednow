package com.css.wondercorefit.ui.viewmodel

import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.utils.WonderCoreCache
import com.css.wondercorefit.viewmodel.MyDeviceViewModel

object DeviceInfoViewModel : BaseViewModel() {
    private val str1 = "测试设备1"
    private val str2 = "测试设备2"

    fun getDeviceInfoSize () {
        return
    }
    val name = arrayListOf(str1, str2)
}