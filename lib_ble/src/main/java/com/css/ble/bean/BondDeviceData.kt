package com.css.ble.bean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.service.bus.LiveDataBus
import com.css.service.data.BaseData
import com.css.service.data.DeviceData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-13
 */
enum class DeviceType(
    val alias: String,
    @StringRes val nameId: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val icon2: Int,
    val cacheKey: CacheKey
) {
    WEIGHT("体脂秤", R.string.device_weight, R.mipmap.icon_weight, R.mipmap.card_weight, CacheKey.BOND_WEIGHT_INFO),
    WHEEL("健腹轮", R.string.device_wheel, R.mipmap.icon_abroller, R.mipmap.card_wheel, CacheKey.BOND_WHEEL_INFO),

    //HORIZONTAL_BAR("单杠");
    HORIZONTAL_BAR("单杠", R.string.device_horizontalbar, R.mipmap.icon_horizontalbar, R.mipmap.card_wheel, CacheKey.BOND_HORIZONTALBAR_INFO),
    PUSH_UP("俯卧撑", R.string.device_pushup, R.mipmap.icon_pushup, R.mipmap.card_wheel, CacheKey.BOND_PUSHUP_INFO),
    COUNTER("计数器", R.string.device_counter, R.mipmap.icon_counter, R.mipmap.card_wheel, CacheKey.BOND_COUNTER_INFO),
    ;

    companion object {
        //通过alias寻找DevcieType
        fun findByAlias(alias: String): DeviceType {
            for (d in values()) {
                if (d.alias == alias) return d
            }
            throw IllegalArgumentException("$alias is not match DeviceType.values")
        }

        fun findByCacheKey(k: CacheKey): DeviceType {
            for (d in values()) {
                if (d.cacheKey == k) return d
            }
            throw IllegalArgumentException("$k is not match DeviceType.values")
        }
    }
}

class BondDeviceData private constructor() : BaseData() {
    lateinit var mac: String
    lateinit var manufacturerDataHex: String
    lateinit var deviceType: DeviceType

    var alias: String? = null
    var id: Int = 0
    var deviceCategory: String = ""
    val deviceImg: Int get() = deviceType.icon2
    var deviceConnect: String? = "未连接"
    var moduleType: String = "" //设备型号
    var moduleVersion: String = "" //固件版本

    constructor(mac: String, manufacturerDataHex: String, deviceType: DeviceType) : this() {
        this.mac = mac
        this.manufacturerDataHex = manufacturerDataHex
        this.deviceType = deviceType
        this.deviceCategory = deviceType.alias
        this.alias = deviceType.alias
    }

    constructor(d: DeviceData) : this(d.bluetoothAddress, "", DeviceType.findByAlias(d.deviceCategory)) {
        this.id = d.id
        this.alias = d.deviceName
        this.deviceCategory = d.deviceCategory
        this.moduleType = d.moduleType
        this.moduleVersion = d.moduleVersion
    }

    override fun equals(other: Any?): Boolean {
        if (other is BondDeviceData) {
            return this.mac == other.mac && this.id == other.id && this.deviceType == other.deviceType
        }
        return false
    }

    fun buidUploadParams(): HashMap<String, Any?> {
        return hashMapOf(
            "deviceCategory" to deviceCategory,
            "deviceName" to displayName,
            "bluetoothAddress" to mac,
            "moduleType" to moduleType,
            "moduleVersion" to moduleVersion
        )
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

        fun getDevice(key: DeviceType): BondDeviceData? =
            WonderCoreCache.getData(key.cacheKey, BondDeviceData::class.java)

        fun setDevice(key: DeviceType, data: BondDeviceData?) {
            WonderCoreCache.saveData(key.cacheKey, data)
        }

        fun getDeviceStateLiveData(): MutableLiveData<Pair<String, String?>> {
            return LiveDataBus.get().with("DeviceState")
        }

        fun getDeviceLiveDataMerge(vararg keys: CacheKey = WonderCoreCache.deviceCacheKeys): LiveData<Pair<CacheKey, BondDeviceData?>> {
            return WonderCoreCache.getLiveDataMerge<BondDeviceData, Pair<CacheKey, BondDeviceData?>>(
                { k, v -> Pair(k, v) },
                *keys
            )
        }

        fun getDeviceLiveData(key: CacheKey): LiveData<BondDeviceData> {
            return WonderCoreCache.getLiveData(key)
        }

        fun getDevices(vararg keys: CacheKey = WonderCoreCache.deviceCacheKeys): List<BondDeviceData> {
            return WonderCoreCache.getDatas(BondDeviceData::class.java, *keys)
        }
    }

    val displayName: String
        get() = if (alias.isNullOrEmpty()) {
            ActivityUtils.getTopActivity().getString(deviceType.nameId)
        } else alias!!

    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', alias=$alias, id=$id, deviceCategory='$deviceCategory', deviceConnect=$deviceConnect, moduleType='$moduleType', moduleVersion='$moduleVersion')"
    }

}
