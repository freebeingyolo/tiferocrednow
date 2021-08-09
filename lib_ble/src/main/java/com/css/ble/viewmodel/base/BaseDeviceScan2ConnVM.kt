package com.css.ble.viewmodel.base

import LogUtils
import android.bluetooth.BluetoothGattService
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.*
import cn.wandersnail.ble.callback.ScanListener
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.poster.RunOn
import cn.wandersnail.commons.poster.Tag
import cn.wandersnail.commons.poster.ThreadMode
import cn.wandersnail.commons.util.StringUtils
import com.css.base.net.api.repository.CourseRepository
import com.css.base.net.api.repository.DeviceRepository
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.IBleConnect
import com.css.ble.viewmodel.IBleScan
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.res.R
import com.css.service.bus.LiveDataBus.BusMutableLiveData
import com.css.service.data.CourseData
import kotlinx.coroutines.Dispatchers
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
        connecting,
        reconnecting,
        connected,
        discovering,
        discovered,
        found,
    }

    /*** abstractable start ****/
    abstract fun filterName(name: String): Boolean
    abstract fun filterUUID(uuid: UUID): Boolean
    /*** abstractable start ****/

    /*** overridable start ****/
    abstract val bonded_tip: String
    open val foundMethod: FoundWay = FoundWay.UUID
    open val bondTimeout = 6 * 1000L
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
    private var connection: Connection? = null
    val stateObsrv: LiveData<State> by lazy { BusMutableLiveData(State.disconnected) }
    var state: State
        set(value) {
            (stateObsrv as MutableLiveData).value = value
            BondDeviceData.getDeviceStateLiveData().value = Pair(deviceType.alias, connectStateTxt.value)
        }
        get() = stateObsrv.value!!

    private val _recommentationData by lazy { BusMutableLiveData<List<CourseData>>() }
    val recommentationData: LiveData<List<CourseData>> get() = _recommentationData


    //Transformations
    val isConnecting = Transformations.map(stateObsrv) {
        it >= State.connecting && it < State.discovered
    }
    val connectStateTxt = Transformations.map(stateObsrv) {
        if (it >= State.discovered) {
            getString(R.string.device_connected)
        } else {
            if (it == State.disconnected) getString(R.string.device_disconnected)
            else getString(R.string.device_connecting)
        }
    }

    val exerciseCount: LiveData<Int> by lazy { MutableLiveData(-1) } //锻炼个数
    val exerciseCountTxt = Transformations.map(exerciseCount) { if (it == -1) "--" else it.toString() }
    val exerciseKcalTxt = Transformations.map(exerciseCount) {
        if (it == -1) "--"
        else DecimalFormat("##.#####").format(it * 0.00175f)
    }
    val exerciseDuration: LiveData<Long> by lazy { MutableLiveData(-1) }
    val exerciseDurationTxt = Transformations.map(exerciseDuration) { if (it == -1L) "--" else formatTime(it) }
    val batteryLevel: LiveData<Float> by lazy { MutableLiveData(-1f) }

    val batteryLevelTxt = Transformations.map(batteryLevel) {
        if (it == -1f) "--" else
            String.format("%d%%", (it * 100).toInt())
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

    override fun onTimerTimeout() {
        state = State.timeOut
        disconnect()
    }

    override fun onTimerCancel() {

    }

    private val scanListener = object : ScanListener {
        override fun onScanStart() {
            LogUtils.d("onScanStart")
            state = State.scanStart
        }

        override fun onScanStop() {
            LogUtils.d("onScanStop")
        }

        override fun onScanResult(device: Device, isConnectedBySys: Boolean) {
            if (filterName(device.name)) {
                LogUtils.d("device:$device")
                EasyBLE.getInstance().stopScanQuietly()
                if (foundMethod == FoundWay.NAME) {
                    foundDevice(device)
                }
                //连接配置，举个例随意配置两项
                val config = ConnectionConfiguration()
                config.setRequestTimeoutMillis(5000)
                config.setDiscoverServicesDelayMillis(300)
                config.setAutoReconnect(false)
                connection = EasyBLE.getInstance().connect(device, config)!!
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
        EasyBLE.getInstance().scanConfiguration.isOnlyAcceptBleDevice = true
        EasyBLE.getInstance().scanConfiguration.rssiLowLimit = -100
        EasyBLE.getInstance().scanConfiguration.scanPeriodMillis = bondTimeout.toInt()
        EasyBLE.getInstance().startScan()
        EasyBLE.getInstance().addScanListener(scanListener)
        startTimeoutTimer(bondTimeout)
    }

    override fun stopScanBle() {
        LogUtils.d("stopScan")
        if (EasyBLE.getInstance().isScanning) {
            EasyBLE.getInstance().stopScan()
            this.state = State.disconnected
        }
    }

    private fun foundDevice(d: Device) {
        LogUtils.d("foundDevice:$avaliableDevice --> device:$d)")
        if (avaliableDevice == null) {
            avaliableDevice = d
            cancelTimeOutTimer()
            state = State.found
        }
    }


    @Tag("onConnectionStateChanged")
    @Observe
    @RunOn(ThreadMode.MAIN)
    final override fun onConnectionStateChanged(@NonNull device: Device) {
        LogUtils.d("onConnectionStateChanged:${device.connectionState},${device.name},${deviceType}")
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> {
                state = State.disconnected
                cancelTimeOutTimer()
                avaliableDevice = null
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
            ConnectionState.RELEASED -> {
                state = State.disconnected
            }
        }
    }

    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
        if (device != connection!!.device) return
        LogUtils.d("onCharacteristicChanged：" + StringUtils.toHex(value, " "))
    }


    //连接
    override fun connect() {
        //连接配置，举个例随意配置两项
        val config = ConnectionConfiguration()
        config.setRequestTimeoutMillis(connectTimeout.toInt())
        config.setDiscoverServicesDelayMillis(300)
        config.setAutoReconnect(false)
        val mac = BondDeviceData.getDevice(deviceType)!!.mac
        connection = EasyBLE.getInstance().connect(mac, config)!!
        startTimeoutTimer(connectTimeout)
    }

    //断开连接
    override fun disconnect() {
        cancelTimeOutTimer()
        if (state > State.disconnected) {
            connection?.disconnect()
            state = State.disconnected
            connection = null
        }
    }

    fun bindDevice(
        success: ((String?, Any?) -> Unit)?,
        failed: ((Int, String?, d: Any?) -> Unit)?
    ) {
        val d = BondDeviceData(
            avaliableDevice!!.address,
            "",
            deviceType
        )
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.bindDevice(d.buidUploadParams())
                    takeIf { ret.isSuccess }.let { BondDeviceData.setDevice(deviceType, BondDeviceData(ret.data!!)) }
                    ret
                }
            },
            { msg, _ ->
                //val bondRst = EasyBLE.getInstance().createBond(it.address)
                //LogUtils.d("bondRst:$bondRst")
                success?.invoke(msg, d)
                avaliableDevice = null
            },
            { code, msg, d ->
                avaliableDevice = null
                state = State.disconnected
                failed?.invoke(code, msg, d)
            }
        )
    }

    fun fetchRecommentation() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = CourseRepository.queryVideo("玩法推荐", deviceType.alias)
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
