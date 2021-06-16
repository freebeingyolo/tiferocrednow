package com.css.ble.viewmodel

import LogUtils
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.css.ble.bean.BondDeviceData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WheelMeasureVM : BaseWheelVM(), EventObserver {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    val state: LiveData<State> get() = _state

    enum class State {
        begin,
        connecting,
        timeOut,
        disconnected,
        reconnecting,
        connected,
        discovering,
        discovered,
        released,

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
        config.setAutoReconnect(true)
        val mac = BondDeviceData.bondWheel!!.mac
        EasyBLE.getInstance().connect(mac, config)
        startTimeoutTimer(10 * 1000)
    }

    fun stopConnect() {
        cancelTimeOutTimer()
        EasyBLE.getInstance().disconnectAllConnections()
    }

    override fun onScanTimeOut() {
        _state.value = State.timeOut
        EasyBLE.getInstance().disconnectAllConnections()
    }

    override fun onScanTimerOutCancel() {
    }

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
                _state.value = State.disconnected
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
                val services: List<BluetoothGattService> = EasyBLE.getInstance().getConnection(device)!!.gatt!!.services
                for (service in services) {
                    if (service.uuid == UUID.fromString(UUID_SRVC)) {
                        discovered(device)
                    }
                }
            }
            ConnectionState.RELEASED -> {
                _state.value = State.released
            }
        }
    }

    private fun discovered(device: Device) {
        _state.value = State.discovered
        cancelTimeOutTimer()
        viewModelScope.launch {
            //开启通知
            sendNotification(UUID_SRVC2, UUID_NOTIFY2, true, object : NotificationChangeCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    LogUtils.d("discovered#sendNotification#onRequestFailed：$failType")
                }

                override fun onNotificationChanged(request: Request, isEnabled: Boolean) {
                    LogUtils.d("discovered#discovered#onNotificationChanged#${request.type}#$isEnabled")
                }
            })
            delay(200)
            //获取电量
            var data = StringUtils.toByteArray("F1-F1-FF-01-00-00-7E","-")
            writeCharacter(UUID_SRVC2, UUID_WRITE2, data, object : WriteCharacteristicCallback {
                override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                    LogUtils.d("discovered#onCharacteristicWrite#onRequestFailed：$failType")
                }

                override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                    LogUtils.d("discovered#onCharacteristicWrite：" + StringUtils.toHex(value, " "))
                }
            })
            delay(200)

        }
    }

    fun startExercise() {

    }

    fun pauseExercise() {

    }

    fun resumeExercise() {

    }

    fun finishExercise() {

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
        LogUtils.d("onCharacteristicChanged：" + StringUtils.toHex(value, " "))
    }

    private fun sendNotification(serviceUUID: UUID, characterUUID: UUID, isEnabled: Boolean, cb: NotificationChangeCallback) {
        //开启通知
        if (connection.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getSetNotificationBuilder(serviceUUID, characterUUID, isEnabled)
        builder.setCallback(cb)
        builder.setPriority(Int.MAX_VALUE) //设置请求优先级
        builder.build().execute(connection)
    }

    private fun writeCharacter(serviceUUID: UUID, characterUUID: UUID, data: ByteArray, cb: WriteCharacteristicCallback) {
        //开启通知
        if (connection.connectionState != ConnectionState.SERVICE_DISCOVERED) return
        val builder = RequestBuilderFactory().getWriteCharacteristicBuilder(serviceUUID, characterUUID, data)
        builder.setCallback(cb)
        builder.setPriority(Int.MAX_VALUE) //设置请求优先级
        /*//根据需要设置写入配置
        builder.setWriteOptions(
            WriteOptions.Builder()
                .setPackageSize(20)
                .setPackageWriteDelayMillis(5)
                .setRequestWriteDelayMillis(10)
                .setWaitWriteResult(true)
                .setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                .build()
        )*/
        connection.execute(builder.build())
    }

    private val connection: Connection get() = EasyBLE.getInstance().getConnection(BondDeviceData.bondWheel!!.mac)!!

}