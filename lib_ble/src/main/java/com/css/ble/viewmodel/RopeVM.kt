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
 *@author chanpal
 *@time 2021-10-29
 *@description 跳绳器
 */
class RopeVM : BaseDeviceScan2ConnVM() {
    val UUID_SRVC = "0000ffb0-0000-1000-8000-00805f9b34fb"
    val UUID_WRITE = "0000ffb1-0000-1000-8000-00805f9b34fb"
    val UUID_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb"
    override val deviceType: DeviceType = DeviceType.ROPE
    var isStart = false
    val modeObsvr: LiveData<Mode> by lazy { MutableLiveData(Mode.byFree) }
    val modeObsvrStr: LiveData<String> = Transformations.map(modeObsvr) {
        getString(it.msgId)
    }
    //transformations


    var mode: Mode
        set(value) {
            (modeObsvr as MutableLiveData).value = value
        }
        get() = modeObsvr.value!!

    enum class Mode(val msgId: Int) {
        byFree(R.string.byFree),
        byCountTime(R.string.byCountTime),
        byCountNumber(R.string.byCountNumber)
    }

    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.00000").format(it * weightKg * 1f * 25 / 30000)
        }
    }

    override fun filterName(name: String): Boolean {
        //val names = arrayOf("Hi-RDTS", "Hi-BBTTS")
        val names = arrayOf("Hi-LYTS")
        return names.find { name.startsWith(it) } != null
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    fun setIsStart(iS: Boolean) {
        isStart = iS
    }

    fun getModels(): List<String> {
        return Mode.values().map { getString(it.msgId) }
    }

    override val bonded_tip: String get() = "跳绳器已连接成功，开启你的挑战之旅吧！"

    override fun onDiscovered(d: Device, isBonding: Boolean) {
        //开启通知
        sendNotification(UUID.fromString(UUID_SRVC), UUID.fromString(UUID_NOTIFY), true, null)
        getBatteryInfo()
    }

    override fun onDisconnected(d: Device?) {
    }

    override fun onBondedOk(d: BondDeviceData) {//绑定成功后进行断开
        disconnect()
    }

    override fun onBondedFailed(d: BondDeviceData) {

    }

    override fun onFoundDevice(d: Device) {

    }

    private fun getBatteryInfo() {
        writeCharacter(
            UUID.fromString(UUID_SRVC),
            UUID.fromString(UUID_WRITE),
            StringUtils.toByteArray("F55F060202005E", ""),
            object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    LogUtils.d("getBatteryInfo#onRequestFailed:${failType},${value}")
                }
                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    LogUtils.d("getBatteryInfo#onRequestSuccess:${StringUtils.toHex(value)}")
                }
            }
        )
    }

    fun switchMode(m: Mode, cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray("F55F060403", "")
        val data2 = DataUtils.shortToByteBig(m.ordinal.toShort())
        val data3 = ((data + data2).sum() and 0x100).toByte()
        val data4 = data + data2 + data3
        LogUtils.d("switchMode:data4:" + StringUtils.toHex(data4, ""))
        writeCharacter(
            UUID.fromString(UUID_SRVC),
            UUID.fromString(UUID_WRITE),
            data4,
            object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    cb?.onRequestFailed(request, failType, value)
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    cb?.onCharacteristicWrite(request, value)
                    mode = m
                }
            })
    }

    fun changeExercise(str: String, cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray("F55F060203", "")
        val data2 = StringUtils.toByteArray(str, "")
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        writeCharacter(
            UUID.fromString(UUID_SRVC),
            UUID.fromString(UUID_WRITE),
            data4,
            object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    cb?.onRequestFailed(request, failType, value)
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    cb?.onCharacteristicWrite(request, value)
                }
            })
        if ("06" == str && !"--".equals(exerciseCountTxt)) {
            finishExercise()
        }

    }

    fun doWriteCharacteristic(str: String, cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray(str, "")
        val data3 = (data.sum() and 0xff).toByte()
        val data4 = data + data3
        writeCharacter(
            UUID.fromString(UUID_SRVC),
            UUID.fromString(UUID_WRITE),
            data4,
            object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    cb?.onRequestFailed(request, failType, value)
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    cb?.onCharacteristicWrite(request, value)
                }
            })
    }

    fun reset(cb: WriteCharacteristicCallback? = null) {
        val data: ByteArray = StringUtils.toByteArray("F55F0602", "")
        val data2 = DataUtils.shortToByteBig(0x1101)
        val data3 = ((data + data2).sum() and 0xff).toByte()
        val data4 = data + data2 + data3
        writeCharacter(
            UUID.fromString(UUID_SRVC),
            UUID.fromString(UUID_WRITE),
            data4,
            object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    cb?.onRequestFailed(request, failType, value)
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    cb?.onCharacteristicWrite(request, value)
                }
            })
        clearAllExerciseData()
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
        if (isStart) {
            when {
                hexData.startsWith("F55F070F") -> {//运动时长
                    val v = DataUtils.bytes2IntBig(value[6], value[7])
                    (exerciseDuration as MutableLiveData).value = v * 1000L
                    // 运动次数
                    val r = DataUtils.bytes2IntBig(value[8], value[9])
                    (exerciseCount as MutableLiveData).value = r
                }
                hexData.startsWith("F55F0704") -> {// 当前模式
                    val v = DataUtils.bytes2IntBig(value[18], value[18])
                    val m = Mode.values()[v]
                    (modeObsvr as MutableLiveData).value = m
                }
                hexData.startsWith("F55F0702") -> {//电池电量
                    val v = DataUtils.bytes2IntBig(value[5], value[5])
                    (batteryLevel as MutableLiveData).value = v % 100
                }
                hexData.startsWith("F55F0B02") -> {//进低功耗模式
                    val v = DataUtils.bytes2IntBig(value[5], value[6])
                    (batteryLevel as MutableLiveData).value = v

                }
            }
        }

    }

    fun jumpToStatistic() {
        (callUILiveData as MutableLiveData).value = "jumpToStatistic"
    }
}