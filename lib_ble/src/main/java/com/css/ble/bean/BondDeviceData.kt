package com.css.ble.bean

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.service.data.BaseData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import java.lang.IllegalStateException

/**
 * @author yuedong
 * @date 2021-05-13
 */
enum class DeviceType {
    WEIGHT, WHEEL;
}

class BondDeviceData(
    var mac: String,
    var manufacturerDataHex: String,
    var type: Int
) : BaseData() {
    var alias: String? = null

    companion object {
        val bondWeightObsrv: LiveData<BondDeviceData> by lazy {
            MutableLiveData(
                if (!WonderCoreCache.containsKey(CacheKey.BOND_WEIGHT_INFO)) null
                else WonderCoreCache.getData(CacheKey.BOND_WEIGHT_INFO, BondDeviceData::class.java)
            )
        }
        val bondWheelObsrv: LiveData<BondDeviceData> by lazy {
            MutableLiveData(
                if (!WonderCoreCache.containsKey(CacheKey.BOND_WHEEL_INFO)) null
                else WonderCoreCache.getData(CacheKey.BOND_WHEEL_INFO, BondDeviceData::class.java)
            )
        }
        var bondWeight: BondDeviceData?
            private set(value) {
                (bondWeightObsrv as MutableLiveData).value = value
                if (value == null) {
                    WonderCoreCache.removeKey(CacheKey.BOND_WEIGHT_INFO)
                } else {
                    WonderCoreCache.saveData(CacheKey.BOND_WEIGHT_INFO, value)
                }
            }
            get() = bondWeightObsrv.value
        var bondWheel: BondDeviceData?
            set(value) {
                (bondWheelObsrv as MutableLiveData).value = value
                if (value == null) {
                    WonderCoreCache.removeKey(CacheKey.BOND_WHEEL_INFO)
                } else
                    WonderCoreCache.saveData(CacheKey.BOND_WHEEL_INFO, value)
            }
            get() = bondWheelObsrv.value

        fun displayName(type: DeviceType): String {
            val data = when (type) {
                DeviceType.WEIGHT -> bondWeight
                else -> bondWheel
            }
            return if (data == null) {
                when (type) {
                    DeviceType.WEIGHT -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                    else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
                }
            } else {
                return data.displayName
            }
        }

        fun getDevice(key: CacheKey): BondDeviceData? = when (key) {
            CacheKey.BOND_WEIGHT_INFO -> bondWeight
            CacheKey.BOND_WHEEL_INFO -> bondWheel
            else -> throw IllegalStateException("")
        }

        fun setDevice(key: CacheKey, data: BondDeviceData?) {
            when (key) {
                CacheKey.BOND_WEIGHT_INFO -> bondWeight = data
                CacheKey.BOND_WHEEL_INFO -> bondWheel = data
                else -> throw IllegalStateException("")
            }
        }
    }

    val displayName: String
        get() = if (alias.isNullOrEmpty()) {
            when (type) {
                DeviceType.WEIGHT.ordinal -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        } else alias!!

    constructor() : this("", "", DeviceType.WEIGHT)
    constructor(mac: String, manufacturerDataHex: String, type: DeviceType) : this(mac, manufacturerDataHex, type.ordinal)

    val cacheKey: CacheKey = when (type) {
        DeviceType.WEIGHT.ordinal -> {
            CacheKey.BOND_WEIGHT_INFO
        }
        else -> {
            CacheKey.BOND_WHEEL_INFO
        }
    }

    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', type=$type, alias=$alias)"
    }
}
