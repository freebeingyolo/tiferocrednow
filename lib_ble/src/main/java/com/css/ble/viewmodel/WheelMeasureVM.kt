package com.css.ble.viewmodel

import LogUtils
import android.bluetooth.BluetoothGattService
import android.os.Looper
import android.view.View
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import cn.wandersnail.ble.*
import cn.wandersnail.ble.callback.NotificationChangeCallback
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.poster.RunOn
import cn.wandersnail.commons.poster.Tag
import cn.wandersnail.commons.poster.ThreadMode
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.utils.DataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*

/**
 * 连接超时:无法连接搜索到设备
 * @author yuedong
 * @date 2021-05-12
 */
object WheelMeasureVM : BaseWheelVM(), EventObserver {
    private var exerciseDurationJob: Job? = null
    val stateObsrv: LiveData<State> by lazy { MutableLiveData(State.disconnected) }
    val batteryLevel: LiveData<Float> by lazy { MutableLiveData(-1f) }
    val exerciseCount: LiveData<Int> by lazy { MutableLiveData(-1) }
    val exerciseDuration: LiveData<Long> by lazy { MutableLiveData(-1) }

    //Transformations
    val isConnecting = Transformations.map(stateObsrv) {
        it >= State.connecting && it < State.discovered
    }
    val connectStateTxt = Transformations.map(stateObsrv) {
        if (it >= State.discovered) {
            ActivityUtils.getTopActivity().getString(R.string.device_connected)
        } else {
            if (it == State.disconnected) ActivityUtils.getTopActivity().getString(R.string.device_disconnected)
            else ActivityUtils.getTopActivity().getString(R.string.device_connecting)
        }
    }
    val exerciseDurationTxt = Transformations.map(exerciseDuration) { if (it == -1L) "--" else formatTime(it) }
    val batteryLevelTxt = Transformations.map(batteryLevel) {
        if (it == -1f) "--" else
            String.format("%d%%", (it * 100).toInt())
    }
    val exerciseCountTxt = Transformations.map(exerciseCount) { if (it == -1) "--" else it.toString() }
    val exerciseKcalTxt = Transformations.map(exerciseCount) { if (it == -1) "--" else DecimalFormat("##.#####").format(it * 0.00175f) }

    private fun formatTime(ms: Long): String {
        val ss = 1000
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24
        val day = ms / dd
        val hour = (ms - day * dd) / hh
        val minute = (ms - day * dd - hour * hh) / mi
        val second = (ms - day * dd - hour * hh - minute * mi) / ss
        val milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss
        val sb = StringBuffer()
        sb.append(String.format("%02d", hour))
        sb.append(String.format(":%02d", minute))
        sb.append(String.format(":%02d", second))
        return sb.toString()
    }

    enum class State {
        disconnected,
        timeOut,
        connecting,
        reconnecting,
        connected,
        discovering,
        discovered,

        //训练
        exercise_start,
        exercise_pause,
        exercise_finish,
    }

    fun connect() {
        //连接配置，举个例随意配置两项
        val config = ConnectionConfiguration()
        config.setRequestTimeoutMillis(1000)
        config.setDiscoverServicesDelayMillis(500)
        config.setAutoReconnect(false)
        val mac = BondDeviceData.bondWheel!!.mac
        EasyBLE.getInstance().connect(mac, config)
        startTimeoutTimer(10 * 1000)
    }

    fun disconnect() {
        cancelTimeOutTimer()
        EasyBLE.getInstance().disconnectAllConnections()
    }

    override fun onScanTimeOut() {
        state = State.timeOut
        EasyBLE.getInstance().disconnectAllConnections()
    }

    val stateStr get() = if (state.ordinal < State.discovered.ordinal) "未连接" else "已连接"

    override fun onScanTimerOutCancel() {
    }

    var state: State
        set(value) {
            (stateObsrv as MutableLiveData).value = value
            if (value == State.disconnected) stopExercise()
        }
        get() = stateObsrv.value!!


    /**
     * 使用[Observe]确定要接收消息，[RunOn]指定在主线程执行方法，设置[Tag]防混淆后找不到方法
     */
    @Tag("onConnectionStateChanged")
    @Observe
    @RunOn(ThreadMode.MAIN)
    override fun onConnectionStateChanged(@NonNull device: Device) {
        LogUtils.d("onConnectionStateChanged:${device.connectionState},${device.name}")
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> {
                state = State.disconnected
            }
            ConnectionState.CONNECTING -> {
                state = State.connecting
            }
            ConnectionState.SCANNING_FOR_RECONNECTION -> {
                state = State.reconnecting
            }
            ConnectionState.CONNECTED -> {
                state = State.connected
            }
            ConnectionState.SERVICE_DISCOVERING -> {
                state = State.discovering
            }
            ConnectionState.SERVICE_DISCOVERED -> {
                val services: List<BluetoothGattService> = EasyBLE.getInstance().getConnection(device)!!.gatt!!.services
                for (service in services) {
                    if (service.uuid == UUID.fromString(UUID_SRVC)) {
                        discovered(device)
                    }
                }
            }
            ConnectionState.RELEASED -> {
                state = State.disconnected
            }
        }
    }

    private fun discovered(device: Device) {
        (stateObsrv as MutableLiveData).value = State.discovered
        cancelTimeOutTimer()
        viewModelScope.launch {
            //上位码
            writeCharacter(UUID_SRVC2, UUID_WRITE2, StringUtils.toByteArray("F1F1FF0100007E", ""), object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    LogUtils.d("discovered#shangweima#onRequestFailed：$failType")
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    LogUtils.d("discovered#shangweima：" + StringUtils.toHex(value, " "))
                }
            })
            //开启通知
            sendNotification(UUID_SRVC2, UUID_NOTIFY2, true, object : NotificationChangeCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    LogUtils.d("discovered#sendNotification#onRequestFailed：$failType")
                }

                override fun onNotificationChanged(request: Request, isEnabled: Boolean) {
                    LogUtils.d("discovered#discovered#onNotificationChanged#${request.type}#$isEnabled")
                }
            })
            //获取电量
            getBattlerLevel()
        }
    }

    fun startExercise() {
        startExerciseTimer()
        (stateObsrv as MutableLiveData).value = State.exercise_start
        //重置锻炼次数
        setExerciseCount(0)
        (exerciseCount as MutableLiveData).value = 0
    }

    fun pauseExercise() {
        (stateObsrv as MutableLiveData).value = State.exercise_pause
        exerciseDurationJob?.cancel()
    }

    fun resumeExercise() {
        startExerciseTimer(false)
        (stateObsrv as MutableLiveData).value = State.exercise_start
    }

    fun stopExercise() {
        if (state > State.discovered) {
            exerciseDurationJob?.cancel()
            exerciseDurationJob = null
            (exerciseCount as MutableLiveData).value = -1
            (exerciseDuration as MutableLiveData).value = -1
            (stateObsrv as MutableLiveData).value = State.discovered
        }
    }


    private fun startExerciseTimer(reset: Boolean = true) {
        if (reset) (exerciseDuration as MutableLiveData).value = 0
        exerciseDurationJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                (exerciseDuration as MutableLiveData).postValue(exerciseDuration.value!! + 1000)
            }
        }
    }

    override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
        super.onRequestFailed(request, failType, value)
    }

    override fun onConnectFailed(device: Device, failType: Int) {
        super.onConnectFailed(device, failType)
        ToastUtils.showShort("连接失败:$failType")
    }

    override fun onConnectTimeout(device: Device, type: Int) {
        super.onConnectTimeout(device, type)
        val msg = when (type) {
            Connection.TIMEOUT_TYPE_CANNOT_DISCOVER_DEVICE -> "无法搜索到设备"
            Connection.TIMEOUT_TYPE_CANNOT_CONNECT -> "无法连接设备"
            Connection.TIMEOUT_TYPE_CANNOT_DISCOVER_SERVICES -> "无法发现蓝牙服务"
            else -> throw IllegalStateException("onConnectTimeout unknown error:$device -- $type")
        }
        ToastUtils.showShort("连接超时:$msg")
    }

    /**
     * 使用[Observe]确定要接收消息，方法在[EasyBLEBuilder.setMethodDefaultThreadMode]指定的线程执行
     */
    @Observe
    override fun onNotificationChanged(@NonNull request: Request, isEnabled: Boolean) {
        LogUtils.d("onNotificationChanged#${request.type}#$isEnabled")
    }

    /**
     * 如果[EasyBLEBuilder.setObserveAnnotationRequired]设置为false时，无论加不加[Observe]注解都会收到消息。
     * 设置为true时，必须加[Observe]才会收到消息。
     * 默认为false，方法默认执行线程在[EasyBLEBuilder.setMethodDefaultThreadMode]指定
     */
    override fun onCharacteristicWrite(request: Request, value: ByteArray) {
        LogUtils.d("onCharacteristicWrite：" + StringUtils.toHex(value, " "))
    }

    override fun onCharacteristicRead(request: Request, value: ByteArray) {
        LogUtils.d("onCharacteristicRead：" + StringUtils.toHex(value, " "))
    }

    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
        LogUtils.d("onCharacteristicChanged：" + StringUtils.toHex(value, " ") + (Looper.myLooper() == Looper.getMainLooper()))
        if (value.size > 3) {
            (batteryLevel as MutableLiveData).value = 1f / value[value.size - 1]
            when (value[0]) {
                0x54.toByte() -> { //查询当前健腹轮个数
                    ActivityUtils.getTopActivity().runOnUiThread {
                        val count = DataUtils.bytes2IntBig(value[2], value[3], value[4])
                        (exerciseCount as MutableLiveData).value = count
                    }
                }
            }
        }
    }

    private fun getBattlerLevel() {
        //获取锻炼个数
        writeCharacter(UUID_SRVC2, UUID_WRITE2, byteArrayOf(0x54), object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                LogUtils.d("discovered#onCharacteristicWrite#onRequestFailed：$failType")
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("discovered#onCharacteristicWrite：" + StringUtils.toHex(value, " "))
            }
        }, "getBattlerLevel")
    }

    private fun getExerciseCount() {
        //获取锻炼个数
        writeCharacter(UUID_SRVC2, UUID_WRITE2, byteArrayOf(0x54), object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                LogUtils.d("discovered#onCharacteristicWrite#onRequestFailed：$failType")
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("discovered#onCharacteristicWrite：" + StringUtils.toHex(value, " "))
            }
        })
    }


    //设置锻炼次数
    private fun setExerciseCount(count: Short) {
        val a = arrayListOf<Byte>().apply { add(0x59);addAll(DataUtils.shortToByteBig(count).toList()) }
        writeCharacter(UUID_SRVC2, UUID_WRITE2, a.toByteArray(), object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                LogUtils.d("setExerciseNum#onCharacteristicWrite#onRequestFailed：$failType")
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("setExerciseNum#onCharacteristicWrite：" + StringUtils.toHex(value, " "))
            }
        })
    }

    private fun sendNotification(serviceUUID: UUID, characterUUID: UUID, isEnabled: Boolean, cb: NotificationChangeCallback) {
        //开启通知
        if (connection.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getSetNotificationBuilder(serviceUUID, characterUUID, isEnabled)
        builder.setCallback(cb)
        connection.execute(builder.build())
    }

    private fun writeCharacter(
        serviceUUID: UUID,
        characterUUID: UUID,
        data: ByteArray,
        cb: WriteCharacteristicCallback,
        tag: String? = null
    ) {
        //开启通知
        if (connection.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getWriteCharacteristicBuilder(serviceUUID, characterUUID, data)
        builder.setCallback(cb)
        builder.setTag(tag)
        //根据需要设置写入配置
        /*builder.setWriteOptions(
            WriteOptions.Builder()
                .setPackageSize(20)
                .setPackageWriteDelayMillis(5)
                .setRequestWriteDelayMillis(100)
                .setWaitWriteResult(true)
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .build()
        )*/
        connection.execute(builder.build())
    }

    private val connection: Connection get() = EasyBLE.getInstance().getConnection(BondDeviceData.bondWheel!!.mac)!!

}