package com.css.ble.viewmodel

import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *@author baoyuedong
 *@time 2021-07-25 19:05
 *@description
 */
class DeviceInfoVM : BaseViewModel() {

    fun getDeviceInfo(
        id: Int,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.queryDeviceListDetails(id)
                    if (ret.isSuccess) {
                        for (d in DeviceType.values()) {
                            val data = ret.data?.find { it.deviceCategory == d.alias }
                            val data2 = data?.let {
                                val ret = BondDeviceData(it)
                                val vm = DeviceVMFactory.getViewModel<BaseDeviceVM>(ret.deviceType)
                                ret.deviceConnect = vm.connectStateTxt()
                                ret
                            }
                            BondDeviceData.setDevice(d, data2)//本地与云端不一致同步云端的
                        }
                    }
                    ret
                }
            },
            { msg, data ->
                hideLoading()
                success(msg, data)
            },
            { code, msg, data ->
                hideLoading()
                failed(code, msg, data)
            }
        )
    }

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
                        BondDeviceData.setDevice(d.deviceType, d)
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