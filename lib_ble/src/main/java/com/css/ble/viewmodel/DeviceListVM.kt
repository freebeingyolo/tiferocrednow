package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceVM
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceListVM : BaseViewModel() {


    fun loadDeviceInfo(
        success: ((msg: String?, d: Any?) -> Unit)? = null,
        failed: ((Int, String?, d: Any?) -> Unit)? = null
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val userId = WonderCoreCache.getLoginInfo()!!.userId
                    val ret = DeviceRepository.queryBindDevice(userId.toString())
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
            { m, devices ->
                //更新BondDevice
                val list = mutableListOf<DeviceInfo>()
                for (d in DeviceType.values()) {
                    val one = DeviceInfo(d.alias, d.icon, d)
                    list.add(one)
                }
                _deviceInfos.value = list
                success?.invoke(m, devices)
            },
            { code, msg, data ->
                hideLoading()
                failed?.invoke(code, msg, data)
            }
        )
    }

    fun unBindDevice(
        d: BondDeviceData,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        DeviceInfoVM().unBindDevice(
            d,
            { msg, d ->
                _deviceInfos.value = _deviceInfos.value
                success(msg, d)
            }, failed
        )
    }

    private val _deviceInfos = MutableLiveData<MutableList<DeviceInfo>>()
    val deviceInfos: LiveData<MutableList<DeviceInfo>> get() = _deviceInfos

    data class DeviceInfo(
        val name: String,
        @DrawableRes val icon: Int,
        val deviceType: DeviceType
    ) {

        fun getBondDeviceData(): BondDeviceData? {

            return BondDeviceData.getDevice(deviceType)
        }
    }
}

