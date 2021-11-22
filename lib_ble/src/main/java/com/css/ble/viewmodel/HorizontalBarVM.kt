package com.css.ble.viewmodel

import LogUtils
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.Device
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.util.StringUtils
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.utils.DataUtils
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 单杠
 */
open class HorizontalBarVM : BaseDeviceScan2ConnVM() {
    override val deviceType: DeviceType = DeviceType.HORIZONTAL_BAR

    val modeObsvr: LiveData<Mode> by lazy { MutableLiveData(Mode.byTime60) }
    val modeObsvrStr: LiveData<String> = Transformations.map(modeObsvr) {
        when (it) {
            Mode.byCount -> getString(R.string.byCount)
            else -> String.format(getString(R.string.byTime), it.time)
        }
    }

    //transformations
    var mode: Mode
        set(value) {
            (modeObsvr as MutableLiveData).value = value
        }
        get() = modeObsvr.value!!

    enum class Mode(val time: Int) {
        byCount(0),
        byTime30(30),
        byTime60(60),
        byTime90(90)
    }


    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.0000").format(it * weightKg * 25 / 30000f)
        }
    }

    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-LYDG")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }


    fun getModels(): List<String> {
        return listOf(
            getString(R.string.byCount),
            String.format(getString(R.string.byTime), 30),
            String.format(getString(R.string.byTime), 60),
            String.format(getString(R.string.byTime), 90)
        )
    }

    override val bonded_tip: String get() = "单杠已连接成功，开启你的挑战之旅吧！"

    override fun onDiscovered(d: Device, isBonding: Boolean) {
        //开启通知
        sendNotification(true)
        if (deviceType == DeviceType.HORIZONTAL_BAR) writeWeight()
    }

    override fun onFoundDevice(d: Device) {
    }

    override fun onDisconnected(d: Device?) {
    }

    override fun onBondedOk(d: BondDeviceData) {
    }

    override fun onBondedFailed(d: BondDeviceData) {
    }

    fun writeWeight(cb: WriteCharacteristicCallback? = null) {
        val weightKgx10 = (weightKg * 10).toInt().toShort()
        val data: ByteArray = StringUtils.toByteArray("F55F060902", "")
        val data2 = DataUtils.shortToByteBig(weightKgx10)
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        LogUtils.d("writeWeight:data4:" + StringUtils.toHex(data4))
        writeCharacter(data4, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
            }
        })
        notifyWeightKgChange(weightKg)
    }

    fun switchMode(m: Mode, cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray("F55F060402", "")
        val data2 = DataUtils.shortToByteBig(m.ordinal.toShort())
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        LogUtils.d("switchMode:data4:" + StringUtils.toHex(data4))
        writeCharacter(data4, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
                mode = m
            }
        })
    }

    open fun reset(cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray("F55F060502", "")
        val data2 = DataUtils.shortToByteBig(0x0001)
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        writeCharacter(data4, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
            }
        })
    }

    //这个是必须的，由于EasyBle的框架bug，必须声明才能反射调用到
    @Observe
    override fun onConnectionStateChanged(@NonNull device: Device) {
        super.onConnectionStateChanged(device)
    }

    @Observe
    override fun onNotificationChanged(@NonNull request: Request, isEnabled: Boolean) {
        LogUtils.d("onNotificationChanged#${request.type}#$isEnabled")
    }

    //F5 5F 07 07 01 00 64 C8   电量
    //F55F0701  计数模式时间
    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
        val hexData = StringUtils.toHex(value, "")
        when {
            hexData.startsWith("F55F0701") -> {//计数模式时间
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseDuration as MutableLiveData).value = v * 1000L
            }
            hexData.startsWith("F55F0702") -> {//倒计模式时间
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseDuration as MutableLiveData).value = v * 1000L
            }
            hexData.startsWith("F55F0703") -> {//运动次数
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (exerciseCount as MutableLiveData).value = v
            }
            hexData.startsWith("F55F0704") -> {//模式切换
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                val m = Mode.values()[v]
                (modeObsvr as MutableLiveData).value = m

                clearAllExerciseData() //本地清零
            }
            hexData.startsWith("F55F0705") -> {//重置切换

            }
            hexData.startsWith("F55F0706") -> {//设备休眠/关机

            }
            hexData.startsWith("F55F0707") -> {//电池电量
                val v = DataUtils.bytes2IntBig(value[5], value[6])
                (batteryLevel as MutableLiveData).value = v
            }
            hexData.startsWith("F55F0708") -> {//提醒上传数据：【切换模式，双击power,时间超出范围，计数超过范围】
                finishExercise()
            }
        }
    }

    override fun finishExercise(
        success: ((String?, Any?) -> Unit)?,
        failed: ((Int, String?, Any?) -> Unit)?
    ) {
        //计数模式：exerciseDuration；倒计时模式：mode.time-exerciseDuration
        finishExercise(
            time = (exerciseDuration.value!! / 1000).toInt().let { if (mode == Mode.byCount) it else mode.time - it },
            num = exerciseCountTxt.value!!.toInt(),
            calory = (exerciseKcalTxt.value!!).toFloat(),
            type = deviceType.alias,
            success,
            failed
        )
    }

    fun jumpToStatistic() {
        (callUILiveData as MutableLiveData).value = "jumpToStatistic"
    }

}