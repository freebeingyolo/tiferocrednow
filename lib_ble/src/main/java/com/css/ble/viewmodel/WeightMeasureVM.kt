package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.WeightBondData
import com.css.ble.bean.BondDeviceData
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WeightMeasureVM : BaseViewModel(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        var TAG = "WeightMeasureVM"
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
    }

    private var mBluetoothService: ELinkBleServer?
        get() = mBluetoothServiceObsvr.value
        set(value) {
            (mBluetoothServiceObsvr as MutableLiveData).value = value
        }
    val mBluetoothServiceObsvr: LiveData<ELinkBleServer> by lazy { MutableLiveData() }

    private var decryptKey: IntArray = TianShengKey
    private var _bondData: MutableLiveData<WeightBondData> = MutableLiveData<WeightBondData>()
    val bondData: LiveData<WeightBondData> get() = _bondData

    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>() }
    val state: MutableLiveData<State> get() = _state
    private var timeOutJob: Job? = null;

    enum class State {
        begin,
        doing,
        timeout,
        done
    }

    fun initOrReset() {
        _state.value = State.begin
        _bondData = MutableLiveData<WeightBondData>()
    }


    fun onBindService(service: ELinkBleServer) {
        mBluetoothService = service
        mBluetoothService?.setOnScanFilterListener(mOnScanFilterListener)
        mBluetoothService?.setOnCallback(mOnCallbackBle)
    }


    fun onUnBindService() {
        mBluetoothService?.setOnScanFilterListener(null)
        mBluetoothService?.setOnCallback(null)
        mBluetoothService = null;
    }

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            var d: BondDeviceData? = BondDeviceData.bondWeight
            return if (d == null) true else d.mac == bleValueBean.mac
        }

        override fun onScanRecord(bleValueBean: BleValueBean) {
            val vid: Int
            if (bleValueBean.isBroadcastModule) {
                val cid = bleValueBean.getCid()
                vid = bleValueBean.getVid()
                val pid = bleValueBean.getPid()
                val manufacturerData = bleValueBean.manufacturerData
                if (manufacturerData != null && manufacturerData.size >= 20) {
                    val sum = manufacturerData[9]
                    val data = ByteArray(10)
                    System.arraycopy(manufacturerData, 10, data, 0, data.size)
                    val newSum = cmdSum(data)
                    if (newSum == sum) {
                        if (cid == 1 && vid == 16 && pid == 2) {
                            if (decryptKey == null) {
                                onErrorString("没有传密钥")
                            }
                            val bytes = AiLinkPwdUtil.decryptKeyInt(decryptKey, data, true)
                            onBroadCastData(
                                bleValueBean.mac, BleStrUtils.byte2HexStr(data),
                                bytes,
                                true
                            )
                        }
                    } else {
                        onErrorString("校验和错误")
                    }
                }
            } else {
                val manufacturerDatax = bleValueBean.manufacturerData
                if (manufacturerDatax != null && manufacturerDatax.size >= 15) {
                    vid = (manufacturerDatax[6].toInt() and 255) shl 8 or (manufacturerDatax[7].toInt() and 255)
                    if (vid == 2) {//匹配成功
                        val hex = BleStrUtils.byte2HexStr(manufacturerDatax)
                        onBroadCastData(bleValueBean.mac, hex, manufacturerDatax, false)
                    }
                }
            }
        }
    }

    fun onErrorString(s: String) {

    }

    private fun cmdSum(data: ByteArray): Byte {
        var sum: Byte = 0
        val size = data.size
        for (i in 0 until size) {
            val datum = data[i]
            sum = (sum + datum).toByte()
        }
        return sum
    }

    private val mOnCallbackBle: OnCallbackBle = object : OnCallbackBle {
        override fun onScanTimeOut() {
            super.onScanTimeOut()
            _state.value = State.timeout
        }
    }

    private val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }

    private fun onBroadCastData(
        mac: String,
        dataHexStr: String,
        data: ByteArray,
        isAilink: Boolean
    ) {
        Log.d(TAG, "mac:$mac Hex的data:  $dataHexStr " + (Looper.myLooper() == Looper.getMainLooper()))
        mBroadcastDataParsing.dataParsing(data, isAilink)
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"])
    fun startScanBle() {
        Log.d(TAG, "startScanBle")
        _state.value = State.doing
        if (!mBluetoothService!!.isScanStatus) {
            this.mBluetoothService?.scanLeDevice(0)
            startTimeoutTimer(3 * 1000)
        }
    }

    private fun startTimeoutTimer(timeOut: Long) {
        timeOutJob?.cancel()
        timeOutJob = viewModelScope.launch(Dispatchers.Main) {
            delay(timeOut)
            stopScanBle()
            _state.value = State.timeout
        }
    }

    private fun cancelTimeOutTimer() {
        timeOutJob?.cancel()
        timeOutJob = null
    }


    fun stopScanBle() {
        Log.d(TAG, "stopScanBle")
        this.mBluetoothService?.stopScan()
        if (timeOutJob != null) cancelTimeOutTimer()
    }

    override fun getWeightData(
        status: Int,
        tempUnit: Int,
        weightUnit: Int,
        weightDecimal: Int,
        weightStatus: Int,
        weightNegative: Int,
        weight: Int,
        adc: Int,
        algorithmId: Int,
        tempNegative: Int,
        temp: Int
    ) {
        WeightBondData().let {
            it.setValue(
                status, tempUnit, weightUnit, weightDecimal,
                weightStatus, weightNegative, weight, adc, algorithmId, tempNegative, temp
            )
            _bondData.value = it
            Log.d(TAG, "getWeightData:$it")

            if (timeOutJob != null) cancelTimeOutTimer()
            if (status == 0x00 && state.value != State.doing) {
                _state.value = State.doing
            } else if (status == 0xFF && state.value != State.done) {
                _state.value = State.done
                WeightBondData.firstWeightInfo ?: let { WeightBondData.firstWeightInfo = _bondData.value }
                WeightBondData.lastWeightInfo = _bondData.value
                stopScanBle()
            }
        }
    }

}