package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil

class WheelBondVM : BaseViewModel(), BroadcastDataParsing.OnBroadcastDataParsing {

    companion object {
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
        private val TAG: String? = WheelBondVM.javaClass.simpleName
    }

    var mBluetoothService: ELinkBleServer? = null
    private var decryptKey: IntArray = TianShengKey

    enum class State {
        bonding,
        discovered,
        bonded
    }

    var state: LiveData<State> = MutableLiveData<State>().apply { value = State.bonding }


    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            var data = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
            Log.d(TAG, "bleValueBean:mac:${bleValueBean.mac},name:${bleValueBean.name}")
            return if (data.mac.isNotEmpty()) {
                data.mac == bleValueBean.mac
            } else true
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
                            if (this@WheelBondVM.decryptKey == null) {
                                this@WheelBondVM.onErrorString("没有传密钥")
                            }
                            val bytes = AiLinkPwdUtil.decryptKeyInt(this@WheelBondVM.decryptKey, data, true)
                            this@WheelBondVM.onBroadCastData(
                                bleValueBean.mac, BleStrUtils.byte2HexStr(data),
                                bytes,
                                true
                            )
                        }
                    } else {
                        this@WheelBondVM.onErrorString("校验和错误")
                    }
                }
            } else {
                val manufacturerDatax = bleValueBean.manufacturerData
                if (manufacturerDatax != null && manufacturerDatax.size >= 15) {
                    vid = (manufacturerDatax[6].toInt() and 255) shl 8 or manufacturerDatax[7].toInt() and 255
                    if (vid == 2) {
                        val hex = BleStrUtils.byte2HexStr(manufacturerDatax)
                        this@WheelBondVM.onBroadCastData(bleValueBean.mac, hex, manufacturerDatax, false)
                    }
                }
            }
        }
    }
    private var mBroadcastDataParsing: BroadcastDataParsing? = null
    var bleEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value = false }
    var locationAllowed: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value = false }
    var locationOpened: MutableLiveData<Boolean> = MutableLiveData<Boolean>().also { it.value = false }
    val isBleEnvironmentOk
        get() = bleEnabled.value!! && locationAllowed.value!! && locationOpened.value!!


    private fun onBroadCastData(
        mac: String,
        dataHexStr: String,
        data: ByteArray,
        isAilink: Boolean
    ) {
        var d = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
        if (d.mac == "") {
            d.mac = mac
            d.manufacturerDataHex = dataHexStr
            d.type = BondDeviceData.TYPE_WEIGHT

        } else {
            return
        }
        Log.d(TAG, "mac:$mac Hex的data:  $dataHexStr")
        mBroadcastDataParsing?.dataParsing(data, isAilink)
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

    private val mOnCallbackBle: OnCallbackBle = object : OnCallbackBle {

        override fun onScanTimeOut() {
            Log.d(TAG, "onScanTimeOut")
        }

        override fun onConnectionSuccess(mac: String?) {
            Log.d(TAG, "onConnectionSuccess:${mac}")
        }

        override fun onServicesDiscovered(mac: String?) {
            Log.d(TAG, "onServicesDiscovered:${mac}")
        }

        override fun onDisConnected(mac: String?, code: Int) {
            Log.d(TAG, "onDisConnected:${mac}")
        }

        override fun onScanning(data: BleValueBean?) {
            Log.d(TAG, "onDisConnected:${data?.mac},${data?.name}")
        }

        override fun onConnecting(mac: String?) {
            Log.d(TAG, "onDisConnected:${mac}")
        }

        override fun onStartScan() {
            Log.d(TAG, "onStartScan:")
        }

        override fun bleOpen() {
            Log.d(TAG, "bleOpen:")
        }

        override fun bleClose() {
            Log.d(TAG, "bleClose:")
        }
    }

    fun onBindService(service: ELinkBleServer) {
        mBluetoothService = service
        mBluetoothService?.setOnScanFilterListener(mOnScanFilterListener)
        mBluetoothService?.setOnCallback(mOnCallbackBle)
        mBroadcastDataParsing = BroadcastDataParsing(this)
    }

    fun onUnBindService() {
        mBluetoothService?.setOnScanFilterListener(null)
        mBluetoothService?.setOnCallback(null)
        mBluetoothService = null;
    }

    fun startScanBle(timeOut: Long = 0, decryptKey: IntArray = TianShengKey) {
        Log.d(TAG, "startScanBle")
        this.decryptKey = decryptKey
        this.mBluetoothService?.scanLeDevice(timeOut)
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
        Log.d(
            TAG,
            "status$status weight:$weight weightDecimal:$weightDecimal  weightUnit:$weightUnit  adc$adc  algorithmId$algorithmId"
        )

    }


}


