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
    var mCountTime: Int = -1
    var mCountNumber: Int = -1

    override val deviceType: DeviceType = DeviceType.ROPE
    var isStart = false
    val modeObsvr: LiveData<Mode> by lazy { MutableLiveData(Mode.byFree) }
    val modeObsvrStr: LiveData<String> = Transformations.map(modeObsvr) {
        getString(it.msgId)
    }
    val motionState by lazy { MutableLiveData(false) }

    //transformations
  /*  val durationCaption = Transformations.map(modeObsvr){
        if(it == Mode.byCountTime) "倒计时" else "运行时长"
    }
    val countCaption = Transformations.map(modeObsvr){
        if(it == Mode.byCountNumber) "本地训练次数" else "运行时长"
    }

    override val exerciseCountTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--" else {
            if (mode == Mode.byCountNumber) (mCountNumber - it).toString() else it.toString()
        }
    }*/

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

    var mode: Mode
        set(value) {
            (modeObsvr as MutableLiveData).value = value
        }
        get() = modeObsvr.value!!

    enum class Mode(val msgId: Int, val code: Byte) {
        byFree(R.string.byFree, 0x01),
        byCountTime(R.string.byCountTime, 0x02),
        byCountNumber(R.string.byCountNumber, 0x03)
    }

    override fun filterName(name: String): Boolean {
        val names = arrayOf("Hi-RDTS")
        return names.find { name.startsWith(it) } != null
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    fun setIsStart(iS: Boolean) {
        isStart = iS
        takeIf { isStart && batteryLevel.value == -1 }?.let { writeCharacter(Command.QUERY_BATTERY.code()) }
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
        SWITCH_MODE("F55F060403"),
        CHANGE_EXERCISE("F55F060203"),//04-暂停，05-恢复，06-停止
        REAL_DATA("F55F070F04"),
        BATTERY("F55F070202"),
        LOW_POWER_MODE("F55F0B0201")
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
    }

    override fun onBondedOk(d: BondDeviceData) {//绑定成功后进行断开
        disconnect()
    }

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

    enum class MotionState(val str: String) {
        PAUSE("04"),
        RESUME("05"),
        STOP("06"),
    }

    //str:04-暂停，05-恢复，06-停止
    fun changeExercise(motion: MotionState, cb: WriteCharacteristicCallback? = null) {
        writeCharacter(Command.CHANGE_EXERCISE.code(motion.str), object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                cb?.onRequestFailed(request, failType, value)
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                cb?.onCharacteristicWrite(request, value)
            }
        })
        if (MotionState.STOP == motion && exerciseCount.value!! > 0) {
            finishExercise()
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
            }
            return
        }
        when (command) {
            Command.REAL_DATA -> {// 当前模式
                //是否运动
                motionState.value = DataUtils.bytes2IntBig(value[5]) == 1
                //时长
                val d = DataUtils.bytes2IntBig(value[6], value[7])
                (exerciseDuration as MutableLiveData).value = d * 1000L

                // 运动次数
                val r = DataUtils.bytes2IntBig(value[8], value[9])
                (exerciseCount as MutableLiveData).value = r
                //运动模式
                val v = DataUtils.bytes2IntBig(value[18])
                if (v in 0..2) {
                    (modeObsvr as MutableLiveData).value = Mode.values()[v]
                } else {
                    LogUtils.e(TAG, "found wrong data:$hexData")
                }
            }
            Command.BATTERY -> {//电池电量
                val v = DataUtils.bytes2IntBig(value[5])
                (batteryLevel as MutableLiveData).value = v
            }
            Command.LOW_POWER_MODE -> {//进低功耗模式,电量为0
                val v = DataUtils.bytes2IntBig(value[5])
                (batteryLevel as MutableLiveData).value = v
            }
            else -> {}
        }
    }

    fun jumpToStatistic() {
        (callUILiveData as MutableLiveData).value = "jumpToStatistic"
    }
}