package com.css.ble.viewmodel

import LogUtils
import android.util.Log
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseWeightVM : BaseViewModel(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        const val TIMEOUT_NEVER = -1L
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
    }
    val TAG: String = javaClass.simpleName
    var bondData: MutableLiveData<WeightBondData> = MutableLiveData<WeightBondData>()
    protected val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }
    protected var decryptKey: IntArray = TianShengKey
    protected var timeOutJob: Job? = null;
    protected var mBluetoothService: ELinkBleServer?
        get() = mBluetoothServiceObsvr.value
        set(v) {
            (mBluetoothServiceObsvr as MutableLiveData).value = v
        }
    val mBluetoothServiceObsvr: LiveData<ELinkBleServer> by lazy { MutableLiveData() }

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {
        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            return this@BaseWeightVM.onFilter(bleValueBean)
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
                if (manufacturerDatax != null && manufacturerDatax.size == 15) {
                    vid = (manufacturerDatax[6].toInt() and 255) shl 8 or (manufacturerDatax[7].toInt() and 255)
                    if (vid == 2) {//匹配成功，第7,8位组成的数据是2
                        val hex = BleStrUtils.byte2HexStr(manufacturerDatax)
                        onBroadCastData(bleValueBean.mac, hex, manufacturerDatax, false)
                    }
                }
            }
        }
    }

    private fun onErrorString(s: String) {
        Log.d(TAG, "onErrorString:$s")
    }

    abstract fun onFilter(bleValueBean: BleValueBean): Boolean
    abstract fun onBroadCastData(mac: String, dataHexStr: String, data: ByteArray, isAilink: Boolean)
    abstract fun onScanStart()
    abstract fun onScanTimeOut()
    abstract fun onScanStop()
    abstract fun onScanTimerOutCancel()
    protected open val timeOut = 5 * 1000L

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
        //mBluetoothService?.setOnScanFilterListener(mOnScanFilterListener)
        mBluetoothService?.setOnCallback(mOnCallbackBle)
    }

    fun onUnBindService() {
        mBluetoothService?.setOnScanFilterListener(null)
        mBluetoothService?.setOnCallback(null)
        mBluetoothService = null;
    }

    private val mOnCallbackBle: OnCallbackBle = object : OnCallbackBle {
        override fun onScanTimeOut() {
            super.onScanTimeOut()
            this@BaseWeightVM.onScanTimeOut()
        }
    }

    protected fun cancelTimeOutTimer() {
        if (timeOutJob != null) {
            LogUtils.d(TAG, "cancelTimeOutTimer")
            timeOutJob!!.cancel()
            timeOutJob = null
            this@BaseWeightVM.onScanTimerOutCancel()
        }
    }

    protected fun startTimeoutTimer(timeOut: Long) {
        if (timeOut == TIMEOUT_NEVER) return
        if (timeOutJob != null) {
            cancelTimeOutTimer()
            LogUtils.e(TAG, "timeOutJob not null,call cancelTimeOutTimer first", 3)
        }
        Log.d(TAG, "startTimeoutTimer")
        timeOutJob = viewModelScope.launch {
            delay(timeOut)
            mOnCallbackBle.onScanTimeOut()
            timeOutJob = null
            stopScanBle() //这里只会执行stopScan，不会执行cancelTimOutTimer
        }
    }

    fun startScanBle() {
        mBluetoothService!!.apply {
            if (!isScanStatus) {
                LogUtils.d(TAG, "startScanBle", 3)
                startTimeoutTimer(timeOut)
                scanLeDevice(0)
                onScanStart()
                mBluetoothService?.setOnScanFilterListener(mOnScanFilterListener)
            } else {
                Log.e(TAG, "startScanBle call repeated!!")
            }
        }
    }

    fun stopScanBle() {
        if (mBluetoothService?.isScanStatus == true) {
            LogUtils.d(TAG, "stopScanBle", 3)
            this.mBluetoothService?.stopScan()
            if (timeOutJob != null) cancelTimeOutTimer()
            onScanStop()
            mBluetoothService?.setOnScanFilterListener(null)
        }
    }

    class BondDeviceInfo {
        var mac: String = ""
        var name: String = ""
        var isAilink: Boolean = false
        var manifactureHex: String = ""
        override fun toString(): String {
            return "BondDeviceInfo(mac='$mac', manifactureHex='$manifactureHex')"
        }
    }


}