package com.css.ble.viewmodel

import androidx.annotation.NonNull
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.commons.observer.Observe
import com.css.ble.bean.DeviceType
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-11-18 10:27
 *@description  棒棒糖跳绳
 */
class RopeBBTVM : RopeVM() {
    override val deviceType: DeviceType = DeviceType.ROPE_BBT
    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-BBTTS")
    }

    @Observe
    override fun onConnectionStateChanged(@NonNull device: Device) {
        super.onConnectionStateChanged(device)
    }

    @Observe
    override fun onNotificationChanged(@NonNull request: Request, isEnabled: Boolean) {
        super.onNotificationChanged(request, isEnabled)
    }

    @Observe
    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
    }
}