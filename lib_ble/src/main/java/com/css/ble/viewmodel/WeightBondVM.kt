package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.WeightBondData
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil

class WeightBondVM : BleEnvVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
        private const val TAG: String = "WeightBond#WeightBondVM"
    }

    val bondData: MutableLiveData<WeightBondData> by lazy { MutableLiveData<WeightBondData>() }
    val bondDevice: MutableLiveData<BondDeviceInfo> by lazy { MutableLiveData<BondDeviceInfo>() }
    var mBluetoothService: ELinkBleServer? = null

    private val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }
    private var decryptKey: IntArray = TianShengKey

    enum class State {
        bondbegin,
        bondingTimeOut,
        found,
        bonded,
        bleEnvError
    }

    val cachedData: BondDeviceData
        get() = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)


    val state: MutableLiveData<State> by lazy { MutableLiveData<State>() }

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            //Log.d(TAG, "bleValueBean:mac:${bleValueBean.mac},name:${bleValueBean.name}")
            var d: BondDeviceData = cachedData
            return if (d.mac.isNullOrEmpty()) true else d.mac == bleValueBean.mac
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
        }
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
            bondData.value = this
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
    fun startScanBle(timeOut: Long = 0) {
        LogUtils.d(TAG, "startScanBle")
        if (!mBluetoothService!!.isScanStatus)
            this.mBluetoothService?.scanLeDevice(timeOut)
    }

    fun stopScanBle() {
        Log.d(TAG, "stopScanBle")
        this.mBluetoothService?.stopScan()
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


