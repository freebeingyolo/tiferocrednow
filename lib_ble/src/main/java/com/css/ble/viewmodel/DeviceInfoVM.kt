package com.css.ble.viewmodel

import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *@author baoyuedong
 *@time 2021-07-25 19:05
 *@description
 */
class DeviceInfoVM : BaseViewModel() {


    fun unBindDevice(
        d: BondDeviceData,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.unbindDevice(d.id, d.deviceCategory)
                    if (ret.isSuccess) {
                        BondDeviceData.setDevice(d.deviceType, null)
                    }
                    ret
                }
            },
            { msg, data ->
                success(msg, data)
            },
            { code, msg, data ->
                failed(code, msg, data)
            }
        )
    }

    fun updateDeviceName(
        d: BondDeviceData,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.updateDeviceName(d.id, d.displayName)
                    if (ret.isSuccess) {
                        BondDeviceData.setDevice(d.deviceType,d)
                    }
                    ret
                }
            },
            { msg, data ->
                success(msg, data)
            },
            { code, msg, data ->
                failed(code, msg, data)
            }
        )
    }
}