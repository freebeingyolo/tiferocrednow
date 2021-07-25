package com.css.ble.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.DeviceRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceListVM : BaseViewModel() {


    fun loadDeviceInfo() {

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
                                BondDeviceData.setDevice(BondDeviceData(d1))
                            }
                        }
                    }
                    ret
                }
            },
            { _, devices ->
                //更新BondDevice
                val list = mutableListOf<DeviceInfo>()
                for ((k, v) in BondDeviceData.IMPORT_DEVICE) {
                    val one = DeviceInfo(k.alias, v)
                    list.add(one)
                }
                _deviceInfos.value = list
            },
            { _, msg, _ ->
                showToast(msg)
                hideLoading()
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
        @DrawableRes val icon: Int
    ) {
        val deviceType: DeviceType
            get() = when (icon) {
                R.mipmap.icon_weight -> DeviceType.WEIGHT
                else -> DeviceType.WHEEL
            }

        fun getBondDeviceData(): BondDeviceData? {
            return when (icon) {
                R.mipmap.icon_weight -> BondDeviceData.bondWeight
                R.mipmap.icon_abroller -> BondDeviceData.bondWheel
                else -> null
            }
        }
    }
}

