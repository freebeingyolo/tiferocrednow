package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.WeightBondData
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil
import kotlinx.coroutines.*

class WeightBondVM : BaseViewModel(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
        private const val TAG: String = "WeightBond#WeightBondVM"
    }

    val bondData: LiveData<WeightBondData>
        get() = _bondData

    private var _bondData: MutableLiveData<WeightBondData> = MutableLiveData<WeightBondData>();

    private var filterDevice: BondDeviceInfo? = null
    val bondDevice: MutableLiveData<BondDeviceInfo> by lazy { MutableLiveData<BondDeviceInfo>() }
    private var mBluetoothService: ELinkBleServer?
        get() = mBluetoothServiceObsvr.value
        set(value) {
            (mBluetoothServiceObsvr as MutableLiveData).value = value
        }
    val mBluetoothServiceObsvr: LiveData<ELinkBleServer> by lazy { MutableLiveData() }

    private val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }
    private var decryptKey: IntArray = TianShengKey
    private var timeOutJob: Job? = null;

    enum class State {
        bondbegin,
        bondingTimeOut,
        found,
        bonded,
    }

    val state: MutableLiveData<State> by lazy { MutableLiveData<State>() }

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            return filterDevice?.run { mac == bleValueBean.mac } ?: true
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
                            if (this@WeightBondVM.decryptKey == null) {
                                this@WeightBondVM.onErrorString("没有传密钥")
                            }
                            val bytes = AiLinkPwdUtil.decryptKeyInt(this@WeightBondVM.decryptKey, data, true)
                            this@WeightBondVM.onBroadCastData(
                                bleValueBean.mac, BleStrUtils.byte2HexStr(data),
                                bytes,
                                true
                            )
                        }
                    } else {
                        this@WeightBondVM.onErrorString("校验和错误")
                    }
                }
            } else {
                val manufacturerDatax = bleValueBean.manufacturerData
                if (manufacturerDatax != null && manufacturerDatax.size >= 15) {
                    vid = (manufacturerDatax[6].toInt() and 255) shl 8 or (manufacturerDatax[7].toInt() and 255)
                    if (vid == 2) {//匹配成功
                        val hex = BleStrUtils.byte2HexStr(manufacturerDatax)
                        this@WeightBondVM.onBroadCastData(bleValueBean.mac, hex, manufacturerDatax, false)
                    }
                }
            }
        }
    }

    private fun onBroadCastData(
        mac: String,
        dataHexStr: String,
        data: ByteArray,
        isAilink: Boolean
    ) {
        Log.d(TAG, "mac:$mac Hex的data:  $dataHexStr " + (Looper.myLooper() == Looper.getMainLooper()))
        BondDeviceInfo().apply {
            this.mac = mac
            this.manifactureHex = dataHexStr
            bondDevice.value = this
            if (filterDevice == null) filterDevice = this
        }
        if(timeOutJob != null) cancelTimeOutTimer() //搜索到设备就取消超时
        mBroadcastDataParsing.dataParsing(data, isAilink)
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
        WeightBondData().apply {
            setValue(
                status, tempUnit, weightUnit, weightDecimal,
                weightStatus, weightNegative, weight, adc, algorithmId, tempNegative, temp
            )
            _bondData.value = this
        }
    }

    private fun onErrorString(s: String) {
        Log.d(TAG, "onErrorString:$s")
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

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"])
    fun startScanBle() {
        Log.d(TAG, "startScanBle")
        if (!mBluetoothService!!.isScanStatus) this.mBluetoothService?.scanLeDevice(0)
        filterDevice = null
        startTimeoutTimer(3 * 1000)
    }

    private fun startTimeoutTimer(timeOut: Long) {
        timeOutJob?.cancel()
        timeOutJob = viewModelScope.launch(Dispatchers.Main) {
            delay(timeOut)
            stopScanBle()
            state.value = State.bondingTimeOut
        }
    }

    private fun cancelTimeOutTimer() {
        timeOutJob?.cancel()
        timeOutJob = null
    }

    fun stopScanBle() {
        Log.d(TAG, "stopScanBle")
        this.mBluetoothService?.stopScan()
        filterDevice = null
        cancelTimeOutTimer()
        _bondData = MutableLiveData<WeightBondData>()
    }

    private val mOnCallbackBle: OnCallbackBle = object : OnCallbackBle {
        override fun onScanTimeOut() {
            super.onScanTimeOut()
            Log.d(TAG, "onScanTimeOut")
            state.value = State.bondingTimeOut
        }
    }

    class BondDeviceInfo {
        var mac: String = ""
        var name: String = ""
        var manifactureHex: String = ""

        override fun toString(): String {
            return "BondDeviceInfo(mac='$mac', manifactureHex='$manifactureHex')"
        }
    }

}


