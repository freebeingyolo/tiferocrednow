package com.css.ble.bean

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.service.bus.LiveDataBus
import com.css.service.data.BaseData
import com.css.service.data.DeviceData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * @author yuedong
 * @date 2021-05-13
 */
enum class DeviceType(val alias: String) {
    WEIGHT("体脂秤"),
    WHEEL("健腹轮");
    //HORIZONTAL_BAR("单杠");

    companion object {
        fun findByAlias(alias: String): DeviceType {
            for (d in values()) {
                if (d.alias == alias) return d
            }
            throw IllegalArgumentException("$alias is not match DeviceType.values")
        }
    }
}


class BondDeviceData(
    var mac: String,
    var manufacturerDataHex: String,
    var type: Int
) : BaseData() {
    var alias: String? = null
    var id: Int = 0
    var deviceCategory: String = ""

    constructor(d: DeviceData) : this() {
        this.id = d.id
        this.mac = d.bluetoothAddress
        this.alias = d.deviceName
        this.deviceCategory = d.deviceCategory
        this.type = DeviceType.findByAlias(d.deviceCategory).ordinal
    }

    constructor() : this("", "", DeviceType.WEIGHT)
    constructor(mac: String, manufacturerDataHex: String, type: DeviceType) : this(mac, manufacturerDataHex, type.ordinal) {
        this.deviceCategory = type.alias
    }

    companion object {
        val IMPORT_DEVICE = linkedMapOf(
            DeviceType.WEIGHT to R.mipmap.icon_weight,
            DeviceType.WHEEL to R.mipmap.icon_abroller,
        )

        var bondWeight: BondDeviceData?
            private set(value) {
                if (value == null) {
                    WonderCoreCache.removeKey(CacheKey.BOND_WEIGHT_INFO)
                } else {
                    WonderCoreCache.saveData(CacheKey.BOND_WEIGHT_INFO, value)
                }
            }
            get() = WonderCoreCache.getData(CacheKey.BOND_WEIGHT_INFO,BondDeviceData::class.java)
        var bondWheel: BondDeviceData?
            set(value) {
                LogUtils.d("bondWheel --> $value",10)
                if (value == null) {
                    WonderCoreCache.removeKey(CacheKey.BOND_WHEEL_INFO)
                } else
                    WonderCoreCache.saveData(CacheKey.BOND_WHEEL_INFO, value)
            }
            get() = WonderCoreCache.getData(CacheKey.BOND_WHEEL_INFO,BondDeviceData::class.java)

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

        fun getDevice(key: DeviceType): BondDeviceData? = when (key) {
            DeviceType.WEIGHT -> bondWeight
            DeviceType.WHEEL -> bondWheel
            else -> throw IllegalStateException("")
        }

        fun setDevice(key: DeviceType, data: BondDeviceData?) {
            when (key) {
                DeviceType.WEIGHT -> bondWeight = data
                DeviceType.WHEEL -> bondWheel = data
                else -> throw IllegalStateException("Illegal key:${key}")
            }
        }

        fun setDevice(data: BondDeviceData) {
            setDevice(data.cacheKey, data)
        }
    }

    val displayName: String
        get() = if (alias.isNullOrEmpty()) {
            when (type) {
                DeviceType.WEIGHT.ordinal -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        } else alias!!

    val cacheKey: DeviceType = DeviceType.values()[type]

    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', type=$type, alias=$alias)"
    }
}
