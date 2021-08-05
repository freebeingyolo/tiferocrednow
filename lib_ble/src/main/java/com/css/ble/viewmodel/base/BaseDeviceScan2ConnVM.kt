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
import com.css.base.net.api.repository.DeviceRepository
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.IBleConnect
import com.css.ble.viewmodel.IBleScan
import com.css.res.R
import com.css.service.bus.LiveDataBus.BusMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 10:46
 *@description  设备扫描ViewModel基类
 */
abstract class BaseDeviceScan2ConnVM(val deviceType: DeviceType) :
    BaseDeviceVM(), IBleScan, IBleConnect, EventObserver {

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
    open val bonded_tip: Int = R.string.weight_bonded_tip
    open val foundMethod: FoundWay = FoundWay.UUID
    open val bondTimeout = 6 * 1000L
    open val connectTimeout = 5 * 1000L

    /*** overridable end ****/
    enum class WorkMode { BOND, MEASURE }

    var workMode = WorkMode.BOND

    private var avaliableDevice: Device? = null
    private var connection: Connection? = null
    val stateObsrv: LiveData<State> by lazy { BusMutableLiveData(State.disconnected) }
    var state: State
        set(value) {
            (stateObsrv as MutableLiveData).value = value
        }
        get() = stateObsrv.value!!

    //Transform

    //Transformations
    val isConnecting = Transformations.map(stateObsrv) {
        it >= State.connecting && it < State.discovered
    }
    val connectStateTxt = Transformations.map(stateObsrv) {
        if (it >= State.discovered) {
            R.string.device_connected
        } else {
            if (it == State.disconnected) R.string.device_disconnected
            else R.string.device_connecting
        }
    }
    
    override fun onTimerTimeout() {
        state = State.timeOut
        connection?.disconnect()
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
        if (avaliableDevice == null) {
            avaliableDevice = d
            cancelTimeOutTimer()
            state = State.found
        }
    }


    @Tag("onConnectionStateChanged")
    @Observe
    @RunOn(ThreadMode.MAIN)
    override fun onConnectionStateChanged(@NonNull device: Device) {
        LogUtils.d("onConnectionStateChanged:${device.connectionState},${device.name}")
        when (device.connectionState) {
            ConnectionState.DISCONNECTED -> {
                state = State.disconnected
                cancelTimeOutTimer()
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
        }
    }

    fun bindDevice(
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        val d = BondDeviceData(
            avaliableDevice!!.address,
            "",
            deviceType
        )
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val ret = DeviceRepository.bindDevice(d.deviceCategory, d.displayName, d.mac)
                    takeIf { ret.isSuccess }.let { BondDeviceData.setDevice(deviceType, BondDeviceData(ret.data!!)) }
                    ret
                }
            },
            { msg, _ ->
                //val bondRst = EasyBLE.getInstance().createBond(it.address)
                //LogUtils.d("bondRst:$bondRst")
                success(msg, d)
                avaliableDevice = null
            },
            { code, msg, d ->
                avaliableDevice = null
                state = State.disconnected
                failed(code, msg, d)
            }
        )
    }

    fun fetchRecommentation() {
    }

    val easterEggs:EasterEggs by lazy { EasterEggs() }
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
