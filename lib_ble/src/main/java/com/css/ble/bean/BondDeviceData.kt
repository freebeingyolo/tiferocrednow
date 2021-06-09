package com.css.ble.bean

import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.service.data.BaseData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

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
        val bondWeight: BondDeviceData?
            get() =
                if (!WonderCoreCache.containsKey(CacheKey.BOND_WEIGHT_INFO)) null
                else WonderCoreCache.getData(CacheKey.BOND_WEIGHT_INFO, BondDeviceData::class.java)

        val bondWheel: BondDeviceData?
            get() =
                if (!WonderCoreCache.containsKey(CacheKey.BOND_WHEEL_INFO)) null
                else WonderCoreCache.getData(CacheKey.BOND_WHEEL_INFO, BondDeviceData::class.java)

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
    }

    val displayName: String
        get() = if (alias.isNullOrEmpty()) {
            when (type) {
                DeviceType.WEIGHT.ordinal -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        } else alias!!

    constructor() : this("", "", DeviceType.WEIGHT)
    constructor(mac: String, manufacturerDataHex: String, type: DeviceType) : this(mac, manufacturerDataHex,type.ordinal)

    fun getCacheKey(): String {
        return when (type) {
            DeviceType.WEIGHT.ordinal -> {
                CacheKey.BOND_WEIGHT_INFO.k
            }
            else -> {
                CacheKey.BOND_WHEEL_INFO.k
            }
        }
    }

    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', type=$type, alias=$alias)"
    }
}
