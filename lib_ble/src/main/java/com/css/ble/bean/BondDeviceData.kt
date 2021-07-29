package com.css.ble.bean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R

import com.css.service.data.BaseData
import com.css.service.data.DeviceData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import java.lang.IllegalArgumentException

/**
 * @author yuedong
 * @date 2021-05-13
 */
enum class DeviceType(
    val alias: String,
    @StringRes val nameId: Int,
    @DrawableRes val icon: Int,
    val cacheKey: CacheKey
) {
    WEIGHT("体脂秤", R.string.device_weight, R.mipmap.icon_weight, CacheKey.BOND_WEIGHT_INFO),
    WHEEL("健腹轮", R.string.device_wheel, R.mipmap.icon_abroller, CacheKey.BOND_WHEEL_INFO),
    HORIZONTAL_BAR("单杠", R.string.device_horizontalbar, R.mipmap.icon_horizontalbar, CacheKey.BOND_HORIZONTALBAR_INFO),
    PUSH_UP("俯卧撑", R.string.device_pushup, R.mipmap.icon_pushup, CacheKey.BOND_PUSHUP_INFO),
    COUNTER("计数器", R.string.device_counter, R.mipmap.icon_counter, CacheKey.BOND_COUNTER_INFO),
    ;

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
    val cacheKey: DeviceType get() = DeviceType.values()[type]

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

        fun displayName(type: DeviceType): String {
            val data = getDevice(type)
            return if (data == null) {
                ActivityUtils.getTopActivity().getString(type.nameId)
            } else {
                return data.displayName
            }
        }

        fun getDevice(key: DeviceType): BondDeviceData? = WonderCoreCache.getData(key.cacheKey, BondDeviceData::class.java)

        fun setDevice(key: DeviceType, data: BondDeviceData?) = WonderCoreCache.saveData(key.cacheKey, data)
    }

    val displayName: String
        get() = if (alias.isNullOrEmpty()) {
            ActivityUtils.getTopActivity().getString(DeviceType.values()[type].nameId)
        } else alias!!


    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', type=$type, alias=$alias)"
    }
}
