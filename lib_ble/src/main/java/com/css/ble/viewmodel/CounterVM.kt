package com.css.ble.viewmodel

import LogUtils
import androidx.annotation.NonNull
import androidx.lifecycle.MutableLiveData
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.util.StringUtils
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.utils.DataUtils
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 计数器
 */
class CounterVM : HorizontalBarVM() {
    private var motionState2 = 0x00
    override val deviceType: DeviceType = DeviceType.COUNTER
    private var initExerciseCount = -1
    private var initExerciseDuration = -1L

    val exerciseCountDelta
        get() = run { //锻炼增量
            val it = exerciseCount.value!!
            if (initExerciseCount < 0) 0 else (it - initExerciseCount)
        }
    val exerciseKcalTxtDelta
        get() = run {
            val it = exerciseCountDelta
            if (it == -1) "--"
            else {
                DecimalFormat("0.00000").format(1f * weightKg * it * 25 / 30000)
            }
        }
    val exerciseDurationDelta
        get() = run {
            val it = exerciseDuration.value!!
            if (it == -1L) 0 else it - initExerciseDuration
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
    override fun onDiscovered(d: Device, isBonding: Boolean) {
        sendNotification(isEnabled = true)
        fetchAllState()
    }

    override fun onDisconnected(d: Device?) {

    }

    override fun onBondedOk(d: BondDeviceData) {
    }

    override fun onBondedFailed(d: BondDeviceData) {
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
        when {
            hexData.startsWith("F55F0701") -> {//次数
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                if (v == 0 && exerciseCountDelta > 0) { //固件sb,非得让次数清零指令在清零指令之前发送
                    uploadExerciseData()
                }
                (exerciseCount as MutableLiveData).value = v
                if (initExerciseCount == -1) initExerciseCount = v
            }
            hexData.startsWith("F55F0703") -> {//电量
                val v = value[5].toInt() * 100 //固件sb,不用百分比表示电量而用1表示充满电，0表示没电
                (batteryLevel as MutableLiveData).value = v
            }
            hexData.startsWith("F55F0702") -> {//清零
                uploadExerciseData()
                initExerciseCount = 0
                initExerciseDuration = 0
            }
            hexData.startsWith("F55F0704") -> {//计时
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseDuration as MutableLiveData).value = v * 1000L
                if (initExerciseDuration == -1L) initExerciseDuration = v * 1000L
            }
            hexData.startsWith("F55F0705") -> {//运动状态
                //LogUtils.d("onCharacteristicChanged#F55F0705#$hexData")
                val v = DataUtils.bytes2IntBig(value[5])
                if (motionState2 == 0x00 && v == 0x01) {
                    onMotionStart()
                } else if (motionState2 == 0x01 && v == 0x00) {
                    onMotionEnd()
                }
                motionState2 = v
            }
        }
    }

    override fun uploadExerciseData(success: ((String?, Any?) -> Unit)?, failed: ((Int, String?, Any?) -> Unit)?) {
        super.uploadExerciseData(
            time = (exerciseDurationDelta / 1000).toInt(),
            num = exerciseCountDelta,
            calory = (exerciseKcalTxtDelta).toFloat(),
            type = deviceType.alias,
            { _, _ ->
                initExerciseCount = exerciseCount.value!!
                initExerciseDuration = exerciseDuration.value!!
            },
            null
        )
    }

    private fun onMotionStart() {
        LogUtils.d("onMotionStart")
    }

    private fun onMotionEnd() {
        LogUtils.d("onMotionEnd")
        //finishExercise()
    }

    private fun fetchAllState() {
        val data: ByteArray = StringUtils.toByteArray("F55F07060100", "")
        val data2 = ((data).sum() and 0xff).toByte()
        val data3 = data + data2
        writeCharacter(data3, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                LogUtils.d("fetchAllState-failed->" + StringUtils.toHex(data3))
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("fetchAllState-ok->" + StringUtils.toHex(data3))
            }
        })
    }

    override fun reset(cb: WriteCharacteristicCallback?) {
        val data: ByteArray = StringUtils.toByteArray("F55F0702", "")
        val data2 = DataUtils.shortToByteBig(0x0100)
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        LogUtils.d("reset-->" + StringUtils.toHex(data4))
        writeCharacter(data4, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
            }
        })
    }

    override fun resetData() {
        super.resetData()
        initExerciseCount = -1
        initExerciseDuration = -1
    }
}