package com.css.ble.bean

import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

        val bondWeight: BondDeviceData?
            get() =
                if (!WonderCoreCache.containsKey(WonderCoreCache.BOND_WEIGHT_INFO)) null
                else WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)

        val bondWheel: BondDeviceData?
            get() =
                if (!WonderCoreCache.containsKey(WonderCoreCache.BOND_WHEEL_INFO)) null
                else WonderCoreCache.getData(WonderCoreCache.BOND_WHEEL_INFO, BondDeviceData::class.java)

        fun displayName(@DeviceType type: Int): String {
            val data = when (type) {
                TYPE_WEIGHT -> bondWeight
                else -> bondWheel
            }
            return if (data == null) {
                when (type) {
                    TYPE_WEIGHT -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
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
                TYPE_WEIGHT -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        } else alias!!

    constructor() : this("", "", TYPE_WEIGHT) {

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

    override fun toString(): String {
        return "BondDeviceData(mac='$mac', manufacturerDataHex='$manufacturerDataHex', type=$type, alias=$alias)"
    }
}
