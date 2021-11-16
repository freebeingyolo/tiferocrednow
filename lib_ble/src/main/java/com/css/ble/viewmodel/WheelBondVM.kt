package com.css.ble.viewmodel

import LogUtils
import android.bluetooth.BluetoothGattService
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.wandersnail.ble.*
import cn.wandersnail.ble.callback.ScanListener
import cn.wandersnail.commons.observer.Observe
import cn.wandersnail.commons.poster.RunOn
import cn.wandersnail.commons.poster.Tag
import cn.wandersnail.commons.poster.ThreadMode
import com.css.ble.viewmodel.base.BaseWheelVM
import java.util.*

/*
0x02010614FF2301010700000000000000000010FFFFFF00010E094162526F6C6C65722D3246434605120600E803020A00
解析：
LEN     TYPE    VALUE
0x02    0x01    0x06
0x14    0x06    0X2301010700000000000000000010FFFFFF0001
0x0E    0x09    0x4162526F6C6C65722D32464346
0x05    0x12    0x0600E803
0x02    0x0A    0x00

AbRoller-2FD0
AbRoller-2FCF
mac:84:c2:e4:05:2f:d0
mac:84:c2:e4:05:2f:df

notify : 66 06 01 00 64 01
         56 06 00 00 64 01
         65 06 01 00 3c 01
x54		查询当前健腹轮个数
0x55	65535（单位秒）16bit	设置健腹轮倒计时时间
0x56	65535（单位个数）16bit	设置健腹轮倒计数个数
0x57		查询最近7天个数
0x58	207e405020812	设置当前时间
0x59	65535（单位个数）16bit	设置当前健腹轮总数

服务UUID：85c60010-4d69-4b6a-afba-fe94fdd1beef
特征写UUID_1：85c60001-4d69-4b6a-afba-fe94fdd1beef  权限：Write
特征广播UUID_2: 85c60002-4d69-4b6a-afba-fe94fdd1beef  权限：Notify

notify:54 06 00 00 01 01
Handler
1  ELinkBleServer.this.scanLeDevice	//开始搜索
2: ELinkBleServer.this.stopScan() //停止搜索
5:	gatt.discoverServices()    //发现服务
	ELinkBleServer.this.mHandler.sendEmptyMessageDelayed(7, (long)ELinkBleServer.this.connectBleTimeout); //

 case 7	//断开连接
	 mConnectGatt.disconnect()
	 mConnectGatt.close()
	 mCallback.onDisConnected
	 CallbackDisIm.getInstance().onDisConnected

case 9	 ELinkBleServer.this.finish() //结束服务
*/
@Deprecated("use WhellMeasureVM instead")
class WheelBondVM : BaseWheelVM(), EventObserver {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.disconnect) }
    val state: LiveData<State> get() = _state
    private var avaliableDevice: Device? = null
    private val timeOut = 5 * 1000L
    private val fondMethod = FoundByName

    enum class State {
        disconnect,
        scanStart,
        connecting,
        discovering,
        timeOut,
        found,
        done,
        reconnecting,
        connected,
        discovered,
        bonded,
        released,
    }

    fun startScanBle() {
        if (EasyBLE.getInstance().isScanning) return
        EasyBLE.getInstance().scanConfiguration.isOnlyAcceptBleDevice = true
        EasyBLE.getInstance().startScan()
        EasyBLE.getInstance().addScanListener(scanListener)
        startTimeoutTimer(timeOut)
    }

    fun stopScanBle() {
        LogUtils.d("stopScan")
        EasyBLE.getInstance().stopScan()
    }

    override fun disconnect() {
        EasyBLE.getInstance().disconnectAllConnections()
    }

    override fun connect() {

    }

    override fun connectStateTxt(): String {
       return "Not yet implemented"
    }

    override fun onTimerTimeout() {
        _state.value = State.timeOut
        stopScanBle()
        disconnect()
    }


    override fun onTimerCancel() {

    }

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
                _state.value = State.disconnect
            }
            ConnectionState.CONNECTING -> {
                _state.value = State.connecting
            }
            ConnectionState.SCANNING_FOR_RECONNECTION -> {
                _state.value = State.reconnecting
            }
            ConnectionState.CONNECTED -> {
                _state.value = State.connected
            }
            ConnectionState.SERVICE_DISCOVERING -> {
                _state.value = State.discovering
            }
            ConnectionState.SERVICE_DISCOVERED -> {
                _state.value = State.discovered
                val services: List<BluetoothGattService> = EasyBLE.getInstance().getConnection(device)!!.gatt!!.services
                for (service in services) {
                    LogUtils.d("service.uuid:${service.uuid}")
                    if (service.uuid == UUID.fromString(UUID_SRVC)) {
                        foundDevice(device)
                    }
                }
            }
            ConnectionState.RELEASED -> {
                _state.value = State.released
            }
        }
    }

    private fun foundDevice(d: Device) {
        avaliableDevice = d
        cancelTimeOutTimer()
        _state.value = State.found
    }

    private val scanListener = object : ScanListener {
        override fun onScanStart() {
            LogUtils.d("onScanStart")
            _state.value = State.scanStart
        }

        override fun onScanStop() {
            LogUtils.d("onScanStop")
        }

        override fun onScanResult(device: Device, isConnectedBySys: Boolean) {
            if (device.name.startsWith("AbRoller")) {
                LogUtils.d("device:$device")
                EasyBLE.getInstance().stopScanQuietly()
                if (fondMethod == FoundByName) {
                    foundDevice(device)
                } else {
                    //连接配置，举个例随意配置两项
                    val config = ConnectionConfiguration()
                    config.setRequestTimeoutMillis(3000)
                    config.setDiscoverServicesDelayMillis(500)
                    config.setAutoReconnect(false)
                    EasyBLE.getInstance().connect(device, config)
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

}


