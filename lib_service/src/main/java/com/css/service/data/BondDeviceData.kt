package com.css.service.data

import androidx.annotation.IntDef

/**
 * @author yuedong
 * @date 2021-05-13
 */

class BondDeviceData(var mac: String, var manufacturerDataHex: String, @DeviceType var type: Int) : BaseData() {
    constructor() : this("", "", TYPE_WEIGHT) {

    }

    companion object {
        const val TYPE_WEIGHT = 0
        const val TYPE_WHEEL = 1
    }

    @IntDef(TYPE_WEIGHT, TYPE_WHEEL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.FIELD)
    annotation class DeviceType
}
