package com.css.ble.viewmodel

import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.viewmodel.base.BaseDeviceVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                    val t1 = System.currentTimeMillis()
                    val ret = DeviceRepository.queryDeviceListDetails(id)
                    if (ret.isSuccess && ret.data != null && ret.data!!.isNotEmpty()) {
                        for (data in ret.data!!) {
                            val data2 = data.let {
                                val ret = BondDeviceData(it)
                                val vm = DeviceVMFactory.getViewModel<BaseDeviceVM>(ret.deviceType)
                                ret.deviceConnect = vm.connectStateTxt()
                                ret
                            }
                            BondDeviceData.setDevice(data2.deviceType, data2) //云端可能改变了设备信息，需要同步一下最新
                        }
                    }
                    val t2 = System.currentTimeMillis()
                    (t2 - t1 - 200).takeIf { it > 0 }?.let { delay(it) }
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