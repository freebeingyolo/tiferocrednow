package com.css.ble.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.util.StringUtils
import com.css.ble.bean.DeviceType
import com.css.ble.utils.DataUtils
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 计数器
 */
class CounterVM : HorizontalBarVM() {
    val UUID_SRVC = "0000ffb0-0000-1000-8000-00805f9b34fb"
    val UUID_WRITE = "0000ffb1-0000-1000-8000-00805f9b34fb"
    val UUID_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb"
    private var motionState = 0x00
    override val deviceType: DeviceType = DeviceType.COUNTER

    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else DecimalFormat("0.00000").format(it * 1f*25/30000)
    }

    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-SWF")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    override val bonded_tip: String
        get() = "计数器已连接成功，开启你的挑战之旅吧！"

    @Observe
    override fun discovered(d: Device) {
        sendNotification(UUID.fromString(Companion.UUID_SRVC), UUID.fromString(Companion.UUID_NOTIFY), true, null)
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
        val hexData = StringUtils.toHex(value, "")
        LogUtils.d("onCharacteristicChanged#$hexData")
        (batteryLevel as MutableLiveData).value = 1
        when {
            hexData.startsWith("F55F0701") -> {//次数
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseCount as MutableLiveData).value = v
            }
            hexData.startsWith("F55F0703") -> {//电量
                val v = value[5].toInt()
                (batteryLevel as MutableLiveData).value = v
            }
            hexData.startsWith("F55F0704") -> {//计时
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseDuration as MutableLiveData).value = v * 1000L
            }
            hexData.startsWith("F55F0705") -> {//电量
                LogUtils.d("onCharacteristicChanged#F55F0705#$hexData")
                val v = DataUtils.bytes2IntBig(value[5])
                if (motionState == 0x00 && v == 0x01) {
                    onMotionStart()
                } else if (motionState == 0x01 && v == 0x00) {
                    onMotionEnd()
                }
                motionState = v
            }
        }
    }

    private fun onMotionStart() {
        LogUtils.d("onMotionStart")
    }

    private fun onMotionEnd() {
        LogUtils.d("onMotionEnd")
        finishExercise()
        reset()
    }

    override fun reset(cb: WriteCharacteristicCallback?) {
        val data: ByteArray = StringUtils.toByteArray("F55F0702", "")
        val data2 = DataUtils.shortToByteBig(0x0100)
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        LogUtils.d("reset-->" + StringUtils.toHex(data4, ""))
        writeCharacter(UUID.fromString(Companion.UUID_SRVC), UUID.fromString(Companion.UUID_WRITE), data4, object :
            WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
            }
        })
    }
}