package com.css.ble.ui.fragment

import android.os.Looper
import android.util.Log
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil

/**
 * @author yuedong
 * @date 2021-05-18
 */
abstract class BleServiceHelper : BroadcastDataParsing.OnBroadcastDataParsing {
    var mBluetoothService: ELinkBleServer? = null
    private var decryptKey: IntArray = TianShengKey

    companion object {
        var TAG = "BleServiceFragment"
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
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

    abstract fun filter(bleValueBean: BleValueBean):Boolean
    abstract fun onErrorString(s: String)
    abstract fun onScanTimeOut()

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            return filter(bleValueBean)
//            var d: BondDeviceData = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
//            return if (d.mac.isNullOrEmpty()) true else d.mac == bleValueBean.mac
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
                            if (this@BleServiceHelper.decryptKey == null) {
                                this@BleServiceHelper.onErrorString("没有传密钥")
                            }
                            val bytes = AiLinkPwdUtil.decryptKeyInt(this@BleServiceHelper.decryptKey, data, true)
                            this@BleServiceHelper.onBroadCastData(
                                bleValueBean.mac, BleStrUtils.byte2HexStr(data),
                                bytes,
                                true
                            )
                        }
                    } else {
                        this@BleServiceHelper.onErrorString("校验和错误")
                    }
                }
            } else {
                val manufacturerDatax = bleValueBean.manufacturerData
                if (manufacturerDatax != null && manufacturerDatax.size >= 15) {
                    vid = (manufacturerDatax[6].toInt() and 255) shl 8 or (manufacturerDatax[7].toInt() and 255)
                    if (vid == 2) {//匹配成功
                        val hex = BleStrUtils.byte2HexStr(manufacturerDatax)
                        this@BleServiceHelper.onBroadCastData(bleValueBean.mac, hex, manufacturerDatax, false)
                    }
                }
            }
        }
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
            this@BleServiceHelper.onScanTimeOut()
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

}