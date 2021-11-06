package com.css.ble.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.commons.observer.Observe
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 俯卧撑板
 */
class PushUpVM : HorizontalBarVM() {
    override val deviceType: DeviceType = DeviceType.PUSH_UP
    val UUID_SRVC = "0000ffb0-0000-1000-8000-00805f9b34fb"
    val UUID_WRITE = "0000ffb1-0000-1000-8000-00805f9b34fb"
    val UUID_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb"


    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-PUSH")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    override val bonded_tip: String
        get() = "俯卧撑板已连接成功，开启你的挑战之旅吧！"

    @Observe
    override fun discovered(d: Device) {
        super.discovered(d)
    }

    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            val weightData = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO).value
            val userInfo = WonderCoreCache.getUserInfo()
            val weightKg = weightData?.weightKg ?: userInfo.targetWeightFloat
            DecimalFormat("0.00000").format(weightKg * it * 0.0007f)
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