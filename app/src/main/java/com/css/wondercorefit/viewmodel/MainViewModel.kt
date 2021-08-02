package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.service.data.DeviceData
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : BaseViewModel() {
    var deviceData = MutableLiveData<List<DeviceData>>()

    //加载设备
    fun loadDevice() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val userId = WonderCoreCache.getLoginInfo()!!.userInfo.userId
                    val ret = DeviceRepository.queryBindDevice(userId.toString())
                    if (ret.isSuccess) {
                        if (ret.data.isNullOrEmpty()) {
                            for (d in DeviceType.values()) {
                                BondDeviceData.setDevice(d, null)
                            }
                        } else {
                            for (d1 in ret.data!!) {
                                BondDeviceData(d1).let {
                                    BondDeviceData.setDevice(it.cacheKey, it)
                                }
                            }
                        }
                    }
                    ret
                }
            },
            { msg, data ->
                deviceData.value = data
            },
            { code, msg, d ->
                showToast(msg)
            }
        )
    }


}