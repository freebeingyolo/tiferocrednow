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
import cn.wandersnail.ble.callback.ScanListener
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.poster.RunOn
import cn.wandersnail.commons.poster.Tag
import cn.wandersnail.commons.poster.ThreadMode
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.css.base.net.api.repository.CourseRepository
import com.css.base.net.api.repository.DeviceRepository
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.utils.BleUtils
import com.css.ble.utils.DataUtils
import com.css.ble.viewmodel.base.BaseWheelVM
import com.css.service.bus.LiveDataBus
import com.css.service.data.CourseData
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.*


/**
 * @author yuedong
 * @date 2021-05-1
 * @description 因为WheelMeasureVM需要绑定之后就能测量，绑定测量共用同一个单例
 */
class WheelMeasureVM : BaseWheelVM(), EventObserver {
    //测量
    private val connectTimeout = 5 * 1000L
    private var exerciseDurationJob: Job? = null
    val stateObsrv: LiveData<State> by lazy { LiveDataBus.BusMutableLiveData(State.disconnected) }
    val batteryLevel: LiveData<Int> by lazy { MutableLiveData(-1) }
    val exerciseCount: LiveData<Int> by lazy { MutableLiveData(-1) } //锻炼个数
    val exerciseDuration: LiveData<Long> by lazy { MutableLiveData(-1) }
    private var exerciseLiveCount: Int = 0 //锻炼长存计数
    private var exercisePauseCount = 0 //暂停期间的计数

    //Transformations
    val isConnecting = Transformations.map(stateObsrv) {
        it >= State.connecting && it < State.discovered
    }
    val connectStateTxt = Transformations.map(stateObsrv) {
        connectStateTxt(it)
    }

    val leftButtonTxt = Transformations.map(stateObsrv) {
        when (it) {
            State.exercise_start -> getString(R.string.pause_exercise)
            State.exercise_pause -> getString(R.string.resume_exercise)
            State.disconnected -> getString(R.string.connect_device)
            State.connecting -> getString(R.string.cancel_connect)
            State.discovered -> getString(R.string.start_exercise)
            else -> getString(R.string.connect_device)
        }
    }

    val leftButtonBackground = Transformations.map(stateObsrv) {
        when (it) {
            State.exercise_start -> getDrawable(R.drawable.bg_34394d_radius45)
            State.exercise_pause -> getDrawable(R.drawable.bg_f66e00_radius45)
            State.disconnected -> getDrawable(R.drawable.bg_f66e00_radius45)
            State.connecting -> getDrawable(R.drawable.bg_34394d_radius45)
            State.discovered -> getDrawable(R.drawable.bg_f66e00_radius45)
            else -> getDrawable(R.drawable.bg_f66e00_radius45)
        }
    }

    val rightButtonVisibility = Transformations.map(stateObsrv) {
        if (it >= State.exercise_start) View.VISIBLE else View.GONE
    }

    private fun connectStateTxt(it: State) = if (it >= State.discovered) {
        getString(R.string.device_connected)
    } else {
        if (it == State.disconnected) getString(R.string.device_disconnected)
        else getString(R.string.device_connecting)
    }

    val exerciseDurationTxt = Transformations.map(exerciseDuration) { if (it == -1L) "--" else formatTime(it) }
    val batteryLevelTxt = Transformations.map(batteryLevel) {
        if (it == -1) "--" else
            String.format("%d%%", it)
    }
    val exerciseCountTxt = Transformations.map(exerciseCount) { if (it == -1) "--" else it.toString() }
    val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.00000").format(1f * weightKg * it * 3.9)
        }
    }

    //玩法推荐
    private val _recommentationData by lazy { MutableLiveData<List<CourseData>>() }
    val recommentationData: LiveData<List<CourseData>> get() = _recommentationData

    //绑定
    private val bondTimeout = 5 * 1000L
    private val fondMethod = FoundByName
    private var avaliableDevice: Device? = null
    private var workMode = WorkMode.BOND

    enum class WorkMode { BOND, MEASURE }
    enum class State {
        disconnected,
        timeOut,
        scanStart,
        found,
        connecting,
        reconnecting,
        connected,
        discovering,
        discovered,

        //训练
        exercise_start,
        exercise_pause,
        //exercise_finish,
    }

    fun fetchRecommentation() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = CourseRepository.queryVideo("教学视频", "健腹轮")
                    ret
                }
            },
            { msg, d ->
                _recommentationData.value = d
            },
            { code, msg, d ->

            }
        )
    }


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

    fun startScanBle() {
        if (EasyBLE.getInstance().isScanning) return
        EasyBLE.getInstance().scanConfiguration.isOnlyAcceptBleDevice = true
        EasyBLE.getInstance().startScan()
        EasyBLE.getInstance().addScanListener(scanListener)
        startTimeoutTimer(bondTimeout)
        workMode = WorkMode.BOND
    }

    fun stopScanBle() {
        LogUtils.d("stopScan,isScanning:${EasyBLE.getInstance().isScanning}")
        if (EasyBLE.getInstance().isScanning) {
            EasyBLE.getInstance().stopScan()
            state = State.disconnected
        }
    }

    private val scanListener = object : ScanListener {
        override fun onScanStart() {
            LogUtils.d("onScanStart")
            state = State.scanStart
        }

        override fun onScanStop() {
            LogUtils.d("onScanStop")
            cancelTimeOutTimer()
            EasyBLE.getInstance().removeScanListener(this)
        }

        override fun onScanResult(device: Device, isConnectedBySys: Boolean) {
            if (device.name.startsWith("AbRoller")) {
                LogUtils.d("device:$device}")
                EasyBLE.getInstance().stopScan()
                if (fondMethod == FoundByName) {
                    foundDevice(device)
                } else {
                    val config = ConnectionConfiguration()
                    config.setRequestTimeoutMillis(5000)
                    config.setDiscoverServicesDelayMillis(300)
                    config.setAutoReconnect(false)
                    connection = EasyBLE.getInstance().connect(device, config, this@WheelMeasureVM)!!
                }
            }
        }

        override fun onScanError(errorCode: Int, errorMsg: String) {
            LogUtils.d("onScanError:$errorCode,$errorMsg", 5)
            when (errorCode) {
                ScanListener.ERROR_LACK_LOCATION_PERMISSION -> {//缺少定位权限
                }
                ScanListener.ERROR_LOCATION_SERVICE_CLOSED -> {//位置服务未开启
                }
                ScanListener.ERROR_SCAN_FAILED -> {
                }
            }
        }
    }

    private fun foundDevice(d: Device) {
        if (avaliableDevice == null) {
            avaliableDevice = d
            cancelTimeOutTimer()
            state = State.found
        }
    }

    fun bindDevice(
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        val d = BondDeviceData(
            avaliableDevice!!.address,
            "",
            avaliableDevice!!.name,
            deviceType
        )
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.bindDevice(d.buidUploadParams())
                    if (ret.isSuccess) {
                        val data1 = BondDeviceData(ret.data!!).apply { deviceConnect = connectStateTxt() }
                        BondDeviceData.setDevice(DeviceType.WHEEL, data1)
                    }
                    ret
                }
            },
            { msg, data ->
                //val bondRst = EasyBLE.getInstance().createBond(it.address)
                //LogUtils.d("bondRst:$bondRst")
                success(msg, d)
                connect()
                avaliableDevice = null
            },
            { code, msg, d ->
                avaliableDevice = null
                disconnect()
                failed(code, msg, d)
            }
        )
    }


    override fun connect() {
        LogUtils.d("connect#connectionState:${connection?.connectionState}")
        if (connection != null && connection!!.connectionState != ConnectionState.DISCONNECTED) return
        //连接配置，举个例随意配置两项
        if (connection == null) {
            val config = ConnectionConfiguration()
            config.setRequestTimeoutMillis(connectTimeout.toInt())
            config.setDiscoverServicesDelayMillis(300)
            config.setAutoReconnect(false)
            val mac = BondDeviceData.getDevice(deviceType)!!.mac
            if (!BleUtils.verifyMacValid(mac)) {
                showToast("非法mac地址，请重新绑定设备，mac:${mac}")
                return
            }
            connection = EasyBLE.getInstance().connect(mac, config, this@WheelMeasureVM)!!
        } else {
            connection!!.reconnect()
        }
        workMode = WorkMode.MEASURE
        startTimeoutTimer(connectTimeout)
    }

    override fun connectStateTxt(): String {
        return connectStateTxt(stateObsrv.value!!)
    }

    override fun disconnect() {
        cancelTimeOutTimer()
        if (state > State.disconnected) {
            LogUtils.d("disconnect,${state},${connection == null}", 7)
            connection?.disconnect() ?: let { state = State.disconnected }
            connection = null
        }
    }

    override fun onTimerTimeout() {
        LogUtils.d("onScanTimeOut")
        state = State.timeOut
        connection?.disconnect()
    }

    override fun onTimerCancel() {
    }

    var state: State
        set(value) {
            (stateObsrv as MutableLiveData).value = value
            BondDeviceData.getDeviceConnectStateLiveData().value = Pair(deviceType.alias, connectStateTxt(value))
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
        LogUtils.d("onConnectionStateChanged:${device.connectionState},${device.name},${deviceType}")
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> {
                state = State.disconnected
                cancelTimeOutTimer()
                resetData()
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
                state = State.discovered
                if (workMode == WorkMode.BOND) {
                    if (fondMethod == FoundByUuid) {
                        val services: List<BluetoothGattService> =
                            EasyBLE.getInstance().getConnection(device)!!.gatt!!.services
                        for (service in services) {
                            if (service.uuid == UUID.fromString(UUID_SRVC)) {
                                foundDevice(device)
                                break
                            }
                        }
                    }
                    discovered(device)
                } else {
                    discovered(device)
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
            writeCharacter(
                UUID_SRVC2,
                UUID_WRITE2,
                StringUtils.toByteArray("F1F1FF0100007E", ""),
                object : WriteCharacteristicCallback {
                    override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                        LogUtils.d("discovered#shangweima#onRequestFailed：$failType")
                    }

                    override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                        LogUtils.d("discovered#shangweima：" + StringUtils.toHex(value))
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
        exercisePauseCount = 0
    }

    fun pauseExercise() {
        (stateObsrv as MutableLiveData).value = State.exercise_pause
        exerciseDurationJob?.cancel()
    }

    fun resumeExercise() {
        startExerciseTimer(false)
        (stateObsrv as MutableLiveData).value = State.exercise_start
        exercisePauseCount = exerciseLiveCount - exerciseCount.value!!
    }

    fun finishExercise(
        success: ((String?, Any?) -> Unit)? = null,
        failed: ((Int, String?, Any?) -> Unit)? = null
    ) {
        val time = (exerciseDuration.value!! / 1000).toInt()
        val num = exerciseCountTxt.value!!.toInt()
        val calory = (exerciseKcalTxt.value!!).toFloat()
        val deviceType = deviceType.alias
        if (time == 0 && num == 0) {
            return LogUtils.d("the time and the num is zero,ignore this uploading")
        }
        netLaunch({
            withContext(Dispatchers.IO) {
                val ret = DeviceRepository.addAbroller(time, num, calory, deviceType)
                ret
            }
        },
            { msg, d ->
                stopExercise()
                success?.invoke(msg, d)
            },
            { code, msg, d ->
                showCenterToast(msg)
                failed?.invoke(code, msg, d)
            }
        )
    }

    fun stopExercise() {
        if (state > State.discovered) {
            (stateObsrv as MutableLiveData).value = State.discovered
        }
        exerciseDurationJob?.cancel()
        exerciseDurationJob = null
        (exerciseCount as MutableLiveData).value = -1
        (exerciseDuration as MutableLiveData).value = -1
        exercisePauseCount = 0
        cancelTimeOutTimer()
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
        LogUtils.d("onCharacteristicWrite：" + StringUtils.toHex(value))
    }

    override fun onCharacteristicRead(request: Request, value: ByteArray) {
        LogUtils.d("onCharacteristicRead：" + StringUtils.toHex(value))
    }

    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
        LogUtils.d(
            "onCharacteristicChanged：" + StringUtils.toHex(value) + (Looper.myLooper() == Looper.getMainLooper())
        )
        if (value.size > 3) {
            (batteryLevel as MutableLiveData).value = (1f / value[value.size - 1] * 100).toInt()
            when (value[0]) {
                0x54.toByte() -> { //查询当前健腹轮个数
                    viewModelScope.launch(Dispatchers.Main) {
                        val count = DataUtils.bytes2IntBig(value[2], value[3], value[4])
                        exerciseLiveCount = count
                        if (state == State.exercise_start) {
                            (exerciseCount as MutableLiveData).value = count - exercisePauseCount
                        }
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
                LogUtils.d("discovered#onCharacteristicWrite：" + StringUtils.toHex(value))
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
                LogUtils.d("discovered#onCharacteristicWrite：" + StringUtils.toHex(value))
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
                LogUtils.d("setExerciseNum#onCharacteristicWrite：" + StringUtils.toHex(value))
            }
        })
    }

    private fun sendNotification(
        serviceUUID: UUID,
        characterUUID: UUID,
        isEnabled: Boolean,
        cb: NotificationChangeCallback
    ) {
        //开启通知
        if (connection?.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getSetNotificationBuilder(serviceUUID, characterUUID, isEnabled)
        builder.setCallback(cb)
        connection?.execute(builder.build())
    }

    private fun writeCharacter(
        serviceUUID: UUID,
        characterUUID: UUID,
        data: ByteArray,
        cb: WriteCharacteristicCallback,
        tag: String? = null
    ) {
        //开启通知
        if (connection?.connectionState != ConnectionState.SERVICE_DISCOVERED) return
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
        connection?.execute(builder.build())
    }

    fun resetData() {
        (exerciseCount as MutableLiveData).value = -1
        (exerciseDuration as MutableLiveData).value = -1
        (batteryLevel as MutableLiveData).value = -1
    }

    private var connection: Connection? = null


    val easterEggs: EasterEggs by lazy { EasterEggs() }

    inner class EasterEggs {
        //volatile适用于改的所有操作或者写的所有操作在同一线程
        private var count = 0
        private var clickTime = 0L

        fun click() {
            if (count == 0) clickTime = System.currentTimeMillis()
            count++
            if (count > 5) {//1s之内点击次数大于5，触发disconnect
                if (System.currentTimeMillis() - clickTime < 1000) {
                    disconnect()
                }
                count = 0
            }

        }
    }
}