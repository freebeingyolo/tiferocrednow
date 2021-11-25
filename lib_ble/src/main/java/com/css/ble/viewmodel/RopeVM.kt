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
 *@description 燃动跳绳
 */
open class RopeVM : BaseDeviceScan2ConnVM() {
    var mCountTime: Int = -1    //单位分钟
    var mCountNumber: Int = -1  //单位个数

    override val deviceType: DeviceType = DeviceType.ROPE
    var isStart = false
    val modeObsvr: LiveData<Mode> by lazy { MutableLiveData(Mode.byFree) }
    val modeObsvrStr = Transformations.map(modeObsvr) {
        getString(it.msgId)
    }
    var mode: Mode
        set(value) = run { (modeObsvr as MutableLiveData).value = value }
        get() = modeObsvr.value!!
    val deviceStateObsvr: LiveData<DeviceState> by lazy { MutableLiveData(DeviceState.DISCONNECT) }

    var deviceState
        set(value) {
            (deviceStateObsvr as MutableLiveData).value = value
            state = state //only notify observers update
        }
        get() = deviceStateObsvr.value!!

    enum class DeviceState(val str: String) {
        DISCONNECT("未连接"),
        CONNECTING("连接中"),
        SHUTDOWN("已关机"),
        MOTION_STOP("已连接"), //连接且已开机
        MOTION_RESUME("运动中"),
        MOTION_PAUSE("运动暂停"),
    }

    override fun connectStateTxt(it: State): String {
        return deviceState.str
    }

    override val connectStateTxt by lazy {
        Transformations.map(deviceStateObsvr) { it.str }
    }

    val durationCaption = Transformations.map(modeObsvr) {
        if (it == Mode.byCountTime) "倒计时" else "运行时长"
    }

    val countCaption = Transformations.map(modeObsvr) {
        if (it == Mode.byCountNumber) "剩余训练次数" else "本地训练次数"
    }

    override val exerciseCountTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--" else {
            if (mode == Mode.byCountNumber) (mCountNumber - it).toString() else it.toString()
        }
    }

    override val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.00000").format(1f * weightKg * it * 25 / 30000)
        }
    }

    override val exerciseDurationTxt = Transformations.map(exerciseDuration) {
        if (it == -1L) "--" else
            formatTime(if (mode == Mode.byCountTime) (mCountTime * 60 * 1000 - it) else it)
    }


    enum class Mode(val msgId: Int, val code: Byte) {
        byFree(R.string.byFree, 0x01),
        byCountTime(R.string.byCountTime, 0x02),
        byCountNumber(R.string.byCountNumber, 0x03)
    }

    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-RDTS")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    fun setIsStart(iS: Boolean) {
        isStart = iS
        //重新去查询电量
        takeIf { isStart && batteryLevel.value == -1 }?.let { writeCharacter(Command.QUERY_BATTERY.code()) }
    }

    fun sendGetBatteryCmd() {
        takeIf { batteryLevel.value == -1 }?.let { writeCharacter(Command.QUERY_BATTERY.code()) }
    }

    fun getModels(): List<String> {
        return Mode.values().map { getString(it.msgId) }
    }

    override val bonded_tip: String get() = String.format("%s已连接成功，开启你的挑战之旅吧！", getString(deviceType.nameId))

    override fun onDiscovered(d: Device, isBonding: Boolean) {
        //开启通知
        sendNotification(true)
        //查询电量
        writeCharacter(Command.CONNECTION_STATE.code(0x01))  //蓝牙连接状态：已连接
        writeCharacter(Command.SET_TIME.codeInt((System.currentTimeMillis() / 1000L).toInt()))  //查询
        writeCharacter(Command.QUERY.code())//查询
        deviceState = DeviceState.SHUTDOWN
    }

    override fun onConnecting(d: Device) {
        super.onConnecting(d)
        deviceState = DeviceState.CONNECTING
    }
    /*sealed class Command2() {
        data class QUERY(val base: String = "F55F06021001") : Command2()
        data class QUERY_BATTERY(val base: String = "F55F06020200") : Command2()
        data class RESET(val base: String = "F55F06021101") : Command2()
        data class CONNECTION_STATE(val base: String = "F55F10030100") : Command2()
        data class SET_TIME(val base: String = "F55F06060100") : Command2()
        data class SWITCH_MODE(val base: String = "F55F060403") : Command2()
        data class CHANGE_EXERCISE(val base: String = "F55F060203", val motionState: String) : Command2()
        class REAL_DATA(val base: String = "F55F070F") : Command2()
        class BATTERY(val base: String = "F55F0702") : Command2()
        class LOW_POWER_MODE(val base: String = "F55F0B02") : Command2()
    }*/

    enum class Command(val base: String) {
        QUERY("F55F06021001"),//查询
        QUERY_BATTERY("F55F06020200"),//查询电量
        RESET("F55F06021101"),//重置
        CONNECTION_STATE("F55F10030100"),//下发连接状态,01-已连接，00-断开
        SET_TIME("F55F06060100"), //设置时间
        SWITCH_MODE("F55F060403"),//切换模式
        CHANGE_EXERCISE("F55F060203"),//04-暂停，05-恢复，06-停止
        REAL_DATA("F55F070F04"),//真实数据
        BATTERY("F55F070202"),//电量
        LOW_POWER_MODE("F55F0B0201")//低功耗模式
        ;

        companion object {
            fun toCommand(code: String, range: IntRange? = null): Command? {
                val code2 = (range?.let { code.substring(it) } ?: code).toUpperCase(Locale.ROOT)
                for (v in values()) {
                    if (v.base.startsWith(code2)) return v
                }
                return null
            }
        }

        fun code(vararg extra: Byte) = run {//拼接组装
            var data: ByteArray = StringUtils.toByteArray(base, "")
            extra.let { data += extra }
            val data3 = (data.sum() and 0xff).toByte()
            val data4 = data + data3
            data4
        }

        fun code(extra: String, separator: String = "") = run {
            val bytes = StringUtils.toByteArray(extra, separator)
            code(*bytes)
        }

        fun codeInt(i: Int) = run {
            val bytes = DataUtils.intToByteBig(i)
            code(*bytes)
        }

        fun codeShort(i: Short) = run {
            val bytes = DataUtils.shortToByteBig(i)
            code(*bytes)
        }
    }


    override fun onDisconnected(d: Device?) {
        deviceState = DeviceState.DISCONNECT
    }

    override fun onBondedOk(d: BondDeviceData) {}

    override fun onBondedFailed(d: BondDeviceData) {

    }

    override fun disconnect() {
        super.disconnect()
        if (state >= State.discovered) writeCharacter(Command.CONNECTION_STATE.code("00"))
    }

    override fun onFoundDevice(d: Device) {

    }

    fun switchMode(m: Mode, cb: WriteCharacteristicCallback? = null) {
        val extra: Short = when (m) {
            Mode.byFree -> 0
            Mode.byCountNumber -> mCountNumber.toShort()
            Mode.byCountTime -> (mCountTime * 60).toShort()
        }
        writeCharacter(Command.SWITCH_MODE.code(m.code, *DataUtils.shortToByteBig(extra)),
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

    enum class MotionState(val codeW: String, val msg: String, val codeR: Byte) {
        PAUSE("04", "暂停训练", 0x01),
        RESUME("05", "开始训练", 0x02),
        STOP("06", "停止训练", 0x00);

        val deviceState get() = DeviceState.valueOf("MOTION_${name}")

        companion object {
            fun deviceState(codeR: Byte): DeviceState? {
                for (m in values()) {
                    if (m.codeR == codeR) return DeviceState.valueOf("MOTION_${m.name}")
                }
                return null
            }
        }
    }

    //str:04-暂停，05-恢复，06-停止
    fun changeExercise(motion: MotionState, cb: WriteCharacteristicCallback? = null) {
        writeCharacter(Command.CHANGE_EXERCISE.code(motion.codeW), object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
                showToast("${motion.msg}失败")
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
                deviceState = motion.deviceState
            }
        })
        if (MotionState.STOP == motion && exerciseCount.value!! > 0) {
            uploadExerciseData()
        }
    }


    fun reset(cb: WriteCharacteristicCallback? = null) {
        writeCharacter(Command.RESET.code(), object : WriteCharacteristicCallback {
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

    @Observe
    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
        val hexData = StringUtils.toHex(value, "")
        val command = Command.toCommand(hexData, 0..7)
        if (!isStart) {
            if (command == Command.BATTERY) {
                val v = DataUtils.bytes2IntBig(value[5])
                (batteryLevel as MutableLiveData).value = v
                deviceState = DeviceState.MOTION_STOP
            }
            return
        }
        when (command) {
            Command.REAL_DATA -> {// 当前模式
                //运动模式
                val v = DataUtils.bytes2IntBig(value[18])
                if (v in 1..3) {
                    mode = Mode.values()[v - 1]
                } else {
                    LogUtils.e(TAG, "found wrong data:$hexData")
                }
                //模式对应的值，
                val s = DataUtils.bytes2IntBig(value[16], value[17])
                when (mode) {
                    Mode.byCountTime -> mCountTime = s / 60
                    Mode.byCountNumber -> mCountNumber = s
                }
                //是否运动
                MotionState.deviceState(value[5])?.let { deviceState = it } ?: let {
                    LogUtils.e(TAG, "found wrong data:$hexData")
                }

                // 运动次数
                val r = DataUtils.bytes2IntBig(value[8], value[9])
                (exerciseCount as MutableLiveData).value = r
                //时长
                val d = DataUtils.bytes2IntBig(value[6], value[7])
                (exerciseDuration as MutableLiveData).value = d * 1000L
            }
            Command.BATTERY -> {//电池电量
                val v = DataUtils.bytes2IntBig(value[5])
                (batteryLevel as MutableLiveData).value = v
                deviceState = DeviceState.MOTION_STOP
            }
            Command.LOW_POWER_MODE -> {//进低功耗模式
                deviceState = DeviceState.SHUTDOWN
            }
            else -> {}
        }
    }

    fun jumpToStatistic() {
        (callUILiveData as MutableLiveData).value = "jumpToStatistic"
    }
}