package com.css.ble.viewmodel.base

import com.css.ble.bean.DeviceType
import java.util.*


/**
 * @author yuedong
 * @date 2021-06-15
 */
abstract class BaseWheelVM : BaseDeviceVM() {
    companion object {
        val UUID_SRVC = "85c60010-4d69-4b6a-afba-fe94fdd1beef"
        val UUID_WRITE = "85c60001-4d69-4b6a-afba-fe94fdd1beef"
        val UUID_NOTIFY = "85c60002-4d69-4b6a-afba-fe94fdd1beef"
        val FoundByName = "FoundByName"
        val FoundByUuid = "FoundByUuid"

        val UUID_SRVC2 = UUID.fromString(UUID_SRVC)
        val UUID_WRITE2 = UUID.fromString(UUID_WRITE)
        val UUID_NOTIFY2 = UUID.fromString(UUID_NOTIFY)
    }

    override val deviceType: DeviceType = DeviceType.WHEEL
    override fun unBind() {
    }
}