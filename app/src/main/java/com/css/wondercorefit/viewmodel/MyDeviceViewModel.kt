package com.css.wondercorefit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.service.data.DeviceData
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *@author baoyuedong
 *@time 2021-07-24 11:36
 *@description
 */
class MyDeviceViewModel : BaseViewModel() {
    private val _deviceInfos by lazy { MutableLiveData<List<DeviceData>>() }
    val deviceInfo: LiveData<List<DeviceData>> get() = _deviceInfos

    //加载设备
    fun loadDevice(
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val userId = WonderCoreCache.getLoginInfo()!!.userInfo.userId
                    DeviceRepository.queryBindDevice(userId.toString())
                }
            },
            { msg, data ->
                _deviceInfos.value = data
            },
            { code, msg, d ->

            }
        )
    }

    //解绑
    fun unBindDevice(
        id: Int, category: String,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.unbindDevice(id, category)
                    takeIf { ret.isSuccess }.apply {BondDeviceData.setDevice(DeviceType.findByAlias(category), null)}
                    ret
                }
            },
            { msg, data ->
                _deviceInfos.value = _deviceInfos.value //just for refresh
                //WonderCoreCache.saveData(CacheKey.BOND_WEIGHT_INFO,null) //下一步用WonderCoreCache解耦BondDeviceData
                success(msg, data)
            },
            { code, msg, data ->
                failed(code, msg, data)
            }
        )
    }
}