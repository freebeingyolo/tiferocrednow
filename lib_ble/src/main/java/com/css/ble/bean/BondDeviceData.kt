package com.css.ble.bean

import androidx.annotation.IntDef
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.service.data.BaseData
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-13
 */

class BondDeviceData(
    var mac: String,
    var manufacturerDataHex: String,
    @DeviceType var type: Int
) : BaseData() {
    var alias: String? = null

    companion object {
        const val TYPE_WEIGHT = 0
        const val TYPE_WHEEL = 1

    }

    constructor() : this("", "", TYPE_WEIGHT) {

    }

    val displayName: String?
        get() {
            if (!alias.isNullOrEmpty()) return alias
            return when (type) {
                TYPE_WEIGHT -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        }


    @IntDef(TYPE_WEIGHT, TYPE_WHEEL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.FIELD)
    annotation class DeviceType


    fun getCacheKey(): String {
        return when (type) {
            TYPE_WEIGHT -> {
                WonderCoreCache.BOND_WEIGHT_INFO
            }
            else -> {
                WonderCoreCache.BOND_WHEEL_INFO
            }
        }
    }
}
