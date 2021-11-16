package com.css.ble.viewmodel.base

import LogUtils
import android.bluetooth.BluetoothGattService
import android.os.Looper
import androidx.annotation.NonNull
import androidx.lifecycle.*
import cn.wandersnail.ble.*
import cn.wandersnail.ble.callback.NotificationChangeCallback
import cn.wandersnail.ble.callback.ScanListener
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.poster.RunOn
import cn.wandersnail.commons.poster.Tag
import cn.wandersnail.commons.poster.ThreadMode
import cn.wandersnail.commons.util.StringUtils
import com.css.base.net.api.repository.CourseRepository
import com.css.base.net.api.repository.DeviceRepository
import com.css.ble.bean.BondDeviceData
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.IBleConnect
import com.css.ble.viewmodel.IBleScan
import com.css.res.R
import com.css.service.bus.LiveDataBus.BusMutableLiveData
import com.css.service.data.CourseData
import com.css.service.data.DeviceData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 10:46
 *@description  设备扫描ViewModel基类
 */
abstract class BaseDeviceScan2ConnVM : BaseDeviceVM(), IBleScan, IBleConnect, EventObserver {

    enum class FoundWay {
        NAME,
        UUID
    }

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
        exercise
    }

    /*** abstractable start ****/
    abstract fun filterName(name: String): Boolean
    abstract fun filterUUID(uuid: UUID): Boolean
    abstract fun onFoundDevice(d: Device)
    abstract fun onDiscovered(d: Device, isBonding: Boolean)
    abstract fun onDisconnected(d: Device?)
    abstract fun onBondedOk(d: BondDeviceData)
    abstract fun onBondedFailed(d: BondDeviceData)

    /*** abstractable end ****/

    /*** overridable start ****/
    abstract val bonded_tip: String
    open val foundMethod: FoundWay = FoundWay.NAME
    open val bondTimeout = 5 * 1000L
    open val connectTimeout = 5 * 1000L

    /*** overridable end ****/
    enum class WorkMode { BOND, MEASURE }

    var workMode: WorkMode
        get() = workModeObsrv.value!!
        set(v) {
            (workModeObsrv as MutableLiveData).value = v
        }
    val workModeObsrv: LiveData<WorkMode> by lazy { BusMutableLiveData(WorkMode.BOND) }

    private var avaliableDevice: Device? = null
    protected var connection: Connection? = null
    val stateObsrv: LiveData<State> by lazy { BusMutableLiveData(State.disconnected) }
    var state: State
        set(value) {
            (stateObsrv as MutableLiveData).value = value
            BondDeviceData.getDeviceConnectStateLiveData().value = Pair(deviceType.alias, connectStateTxt(value))
        }
        get() = stateObsrv.value!!

    val recommentationMap by lazy {
        HashMap<String, MutableLiveData<List<CourseData>>>().apply {
            put("教学视频", BusMutableLiveData())
            put("健身音乐", BusMutableLiveData())
        }
    }

    //Transformations
    val isConnecting = Transformations.map(stateObsrv) {
        it >= State.connecting && it < State.discovered
    }

    val connectStateTxt = Transformations.map(stateObsrv) {
        connectStateTxt(it)
    }

    override fun connectStateTxt(): String {
        return (connectStateTxt(stateObsrv.value!!))
    }

    private fun connectStateTxt(it: State): String {
        return if (it >= State.discovered) {
            getString(R.string.device_connected)
        } else {
            if (it == State.disconnected) getString(R.string.device_disconnected)
            else getString(R.string.device_connecting)
        }
    }

    val exerciseCount: LiveData<Int> by lazy { MutableLiveData(-1) } //锻炼个数
    val exerciseCountTxt = Transformations.map(exerciseCount) { if (it == -1) "--" else it.toString() }
    open val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else {
            DecimalFormat("0.00000").format(1f * weightKg * it * 25 / 30000)
        }
    }

    val exerciseDuration: LiveData<Long> by lazy { MutableLiveData(-1) }
    val exerciseDurationTxt = Transformations.map(exerciseDuration) { if (it == -1L) "--" else formatTime(it) }
    val batteryLevel: LiveData<Int> by lazy { MutableLiveData(-1) }

    val batteryLevelTxt = Transformations.map(batteryLevel) {
        if (it == -1) "--" else
            String.format("%d%%", it)
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
        //sb.append(String.format("%02d:", hour))
        sb.append(String.format("%02d:", minute))
        sb.append(String.format("%02d", second))
        return sb.toString()
    }

    override fun onTimerTimeout() { //超时分两种，一种是扫描超时；一种是连接超时
        state = State.timeOut
        if (EasyBLE.getInstance().isScanning) {
            stopScanBle()
        } else {
            disconnect()
        }
    }

    override fun onTimerCancel() {}

    private val scanListener = object : ScanListener {
        override fun onScanStart() {
            LogUtils.d("onScanStart,isMain:${Looper.myLooper() == Looper.getMainLooper()}")
        }

        override fun onScanStop() {
            LogUtils.d("onScanStop")
            cancelTimeOutTimer()
            EasyBLE.getInstance().removeScanListener(this)
        }

        override fun onScanResult(device: Device, isConnectedBySys: Boolean) {
            if (filterName(device.name)) {
                LogUtils.d("device:$device")
                EasyBLE.getInstance().stopScan()
                if (foundMethod == FoundWay.NAME) {
                    foundDevice(device)
                }
                //连接配置，举个例随意配置两项
                val config = ConnectionConfiguration()
                config.setRequestTimeoutMillis(5000)
                config.setDiscoverServicesDelayMillis(300)
                config.setAutoReconnect(false)
                connection = EasyBLE.getInstance().connect(device, config, this@BaseDeviceScan2ConnVM)!!
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

    override fun startScanBle() {
        if (EasyBLE.getInstance().isScanning) return
        LogUtils.d("startScanBle,state:${state},isScanning:${EasyBLE.getInstance().isScanning}")
        EasyBLE.getInstance().scanConfiguration.isOnlyAcceptBleDevice = true
        EasyBLE.getInstance().scanConfiguration.rssiLowLimit = -100
        EasyBLE.getInstance().scanConfiguration.scanPeriodMillis = bondTimeout.toInt()//永不超时
        EasyBLE.getInstance().startScan()
        EasyBLE.getInstance().addScanListener(scanListener)
        startTimeoutTimer(bondTimeout)
        state = State.scanStart
    }

    override fun stopScanBle() {
        LogUtils.d("stopScan,isScanning:${EasyBLE.getInstance().isScanning}")
        if (EasyBLE.getInstance().isScanning) {
            EasyBLE.getInstance().stopScan()
            cancelTimeOutTimer()
            this.state = State.disconnected
        }
    }

    private fun foundDevice(d: Device) {
        if (avaliableDevice != null) throw IllegalStateException("avaliableDevice is not null")
        if (avaliableDevice == null) {
            avaliableDevice = d
            cancelTimeOutTimer()
            state = State.found
            onFoundDevice(d)
        }
    }

    private fun onDisconnectedX(device: Device?) {
        cancelTimeOutTimer()
        avaliableDevice = null
        onDisconnected(device)
        resetData()
    }

    @Tag("onConnectionStateChanged")
    @Observe
    @RunOn(ThreadMode.MAIN)
    override fun onConnectionStateChanged(@NonNull device: Device) {
        LogUtils.d("onConnectionStateChanged:${device.connectionState},${device.name},${deviceType}")
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> {
                state = State.disconnected
                onDisconnectedX(device)
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
                    if (foundMethod == FoundWay.UUID) {
                        val services: List<BluetoothGattService> = connection!!.gatt!!.services
                        loop@ for (service in services) {
                            if (filterUUID(service.uuid)) {
                                foundDevice(device)
                                break@loop
                            }
                            for (ch in service.characteristics) {
                                if (filterUUID(ch.uuid)) {
                                    foundDevice(device)
                                    break@loop
                                }
                            }
                        }
                    }
                    onDiscovered(device, true)
                } else {
                    cancelTimeOutTimer()
                    onDiscovered(device, false)
                }
            }
            ConnectionState.RELEASED -> {
                release()
                state = State.disconnected
            }
        }
    }

    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        LogUtils.d("onCharacteristicChanged:" + StringUtils.toHex(value, ""))
    }

    @Observe
    override fun onNotificationChanged(@NonNull request: Request, isEnabled: Boolean) {
        LogUtils.d("onNotificationChanged#${request.type}#$isEnabled")
    }

    //连接
    override fun connect() {
        //连接配置，举个例随意配置两项
        if (connection == null) {
            val config = ConnectionConfiguration()
            config.setRequestTimeoutMillis(connectTimeout.toInt())
            config.setRequestTimeoutMillis(5000)
            config.setDiscoverServicesDelayMillis(300)
            config.setAutoReconnect(false)
            val mac = BondDeviceData.getDevice(deviceType)!!.mac
            if (!BleUtils.verifyMacValid(mac)) {
                showToast("非法mac地址，请重新绑定设备，mac:${mac}")
                return
            }
            connection = EasyBLE.getInstance().connect(mac, config, this@BaseDeviceScan2ConnVM)
        } else {
            connection?.reconnect()
        }
        startTimeoutTimer(connectTimeout)
    }

    //断开连接
    override fun disconnect() {
        cancelTimeOutTimer()
        if (state != State.disconnected) {
            if (connection == null || connection!!.connectionState == ConnectionState.DISCONNECTED) {
                state = State.disconnected
                onDisconnectedX(connection?.device)
            } else {
                connection?.disconnect()
            }
            LogUtils.d("disconnect, ${state},isScanning:${EasyBLE.getInstance().isScanning} connectionState:${connection?.connectionState}")
        }
    }

    override fun release() {
        cancelTimeOutTimer()
        if (connection != null) {
            LogUtils.d("release", 5)
            connection?.release()
            //state = State.disconnected
            connection = null
        }
    }

    fun bindDevice(
        success: ((String?, DeviceData?) -> Unit)?,
        failed: ((Int, String?, d: DeviceData?) -> Unit)?
    ) {
        val device = BondDeviceData(
            avaliableDevice!!.address,
            "",
            avaliableDevice!!.name,
            deviceType
        )
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.bindDevice(device.buidUploadParams())
                    if (ret.isSuccess) {
                        val d = BondDeviceData(ret.data!!).apply { this.deviceConnect = connectStateTxt() }
                        BondDeviceData.setDevice(deviceType, d)
                    }
                    ret
                }
            },
            { msg, d ->
                //val bondRst = EasyBLE.getInstance().createBond(it.address)
                //LogUtils.d("bondRst:$bondRst")
                success?.invoke(msg, d)
                onBondedOk(device)
                avaliableDevice = null
            },
            { code, msg, d ->
                avaliableDevice = null
                disconnect()
                onBondedFailed(device)
                failed?.invoke(code, msg, d)
            }
        )
    }

    fun fetchRecommentation(scene: String = "教学视频") {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = CourseRepository.queryVideo(scene, deviceType.alias)//"教学视频"
                    ret
                }
            },
            { msg, d ->
                recommentationMap[scene]!!.value = d
            },
            { code, msg, d ->

            }
        )
    }

    fun sendNotification(serviceUUID: UUID, characterUUID: UUID, isEnabled: Boolean, cb: NotificationChangeCallback?) {
        //开启通知
        if (connection?.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getSetNotificationBuilder(serviceUUID, characterUUID, isEnabled)
        builder.setCallback(cb)
        connection?.execute(builder.build())
    }

    override fun notifyWeightKgChange(wKg: Float) {//通知体重变更
        (exerciseCount as MutableLiveData).value = exerciseCount.value
    }

    fun writeCharacter(
        serviceUUID: UUID,
        characterUUID: UUID,
        data: ByteArray,
        cb: WriteCharacteristicCallback?,
        tag: String? = null
    ) {

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
        //开启通知
        if (connection?.connectionState != ConnectionState.SERVICE_DISCOVERED) {
            cb?.onRequestFailed(builder.build(), -1, null)
            return
        }
        connection?.execute(builder.build())
    }


    open fun finishExercise(
        success: ((String?, Any?) -> Unit)? = null,
        failed: ((Int, String?, Any?) -> Unit)? = null
    ) {
        finishExercise(
            time = (exerciseDuration.value!! / 1000).toInt(),
            num = exerciseCountTxt.value!!.toInt(),
            calory = (exerciseKcalTxt.value!!).toFloat(),
            d = deviceType.alias,
            success,
            failed
        )
    }

    fun finishExercise(
        time: Int, num: Int, calory: Float, d: String,
        success: ((String?, Any?) -> Unit)? = null,
        failed: ((Int, String?, Any?) -> Unit)? = null
    ) {
        if (time <= 0 || num <= 0) {
            return LogUtils.d("the time is ${time}} the num is ${num},ignore this uploading")
        }
        netLaunch({
            withContext(Dispatchers.IO) {
                var ret = DeviceRepository.addPushUps(time, num, calory, d)
                var retry = 0
                while (!ret.isSuccess) {
                    delay(100)
                    retry++
                    ret = DeviceRepository.addPushUps(time, num, calory, d)
                    if (retry >= 2) break
                }
                ret
            }
        },
            { msg, d ->
                success?.invoke(msg, d)
            },
            { code, msg, d ->
                showCenterToast(msg)
                failed?.invoke(code, msg, d)
            }
        )
    }


    /****/
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

    fun clearAllExerciseData() {
        (exerciseCount as MutableLiveData).value = 0
        (exerciseDuration as MutableLiveData).value = 0
    }

    open fun resetData() {
        (exerciseCount as MutableLiveData).value = -1
        (exerciseDuration as MutableLiveData).value = -1
        (batteryLevel as MutableLiveData).value = -1
    }
}
