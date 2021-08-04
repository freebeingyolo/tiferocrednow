package com.css.ble.viewmodel

import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description
 */
object HorizontalBarVM : BaseDeviceScan2ConnVM(DeviceType.HORIZONTAL_BAR){

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString().startsWith("")
    }

    override fun filterName(name: String): Boolean {
        return name.startsWith("")
    }


}