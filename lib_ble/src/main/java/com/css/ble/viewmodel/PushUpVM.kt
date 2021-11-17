package com.css.ble.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.commons.observer.Observe
import com.css.ble.bean.DeviceType
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 俯卧撑板
 */
class PushUpVM : HorizontalBarVM() {
    override val deviceType: DeviceType = DeviceType.PUSH_UP

    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-PUSH")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    override val bonded_tip: String
        get() = "俯卧撑板已连接成功，开启你的挑战之旅吧！"

    @Observe
    override fun onDiscovered(d: Device,isBonding:Boolean) {
        super.onDiscovered(d,isBonding)
    }

    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.00000").format(1f * weightKg * it * 0.0007f)
        }
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