package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.net.aicare.algorithmutil.AlgorithmUtil
import cn.net.aicare.algorithmutil.BodyFatData
import com.blankj.utilcode.util.LogUtils
import com.css.ble.bean.WeightBondData
import com.css.ble.bean.WeightDetailsBean
import com.css.ble.bean.WeightInfo
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleStrUtils
import com.pinwang.ailinkble.AiLinkPwdUtil


class WeightMeasureVM : BleEnvVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        var TAG = "BleServiceFragment"
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
    }
    val bleSvcLiveData: MutableLiveData<ELinkBleServer> by lazy { MutableLiveData<ELinkBleServer>() }
    private val mBluetoothService: ELinkBleServer?
        get() = bleSvcLiveData.value

    private var decryptKey: IntArray = TianShengKey
    val bondData: MutableLiveData<WeightBondData> by lazy { MutableLiveData<WeightBondData>() }


    val state: MutableLiveData<State> by lazy { MutableLiveData<State>() }
    private val weightInfo: MutableLiveData<WeightInfo> by lazy { MutableLiveData<WeightInfo>() }

    enum class State {
        begin,
        doing,
        timeout,
        done
    }

    fun getBodyFatData(): BodyFatData {
        var userInfo = WonderCoreCache.getUserInfo()
        val sex = userInfo.setInt
        val age = userInfo.age.toInt()
        val weight_kg = weightInfo.value!!.weight
        val height_cm = userInfo.stature.toInt()
        val adc = weightInfo.value!!.adc
        var data: BodyFatData = AlgorithmUtil.getBodyFatData(AlgorithmUtil.AlgorithmType.TYPE_AICARE, sex, age, weight_kg, height_cm, adc);
        return data
    }

    fun getBodyFatDataList2(): List<Map<String, Any?>> {
        var data: BodyFatData = getBodyFatData();
        var datas = mutableListOf<Map<String, Any?>>()
        var clazz = data.javaClass
        for (m in clazz.declaredFields) {
            m.isAccessible = true
            var map = mutableMapOf<String, Any?>()
            map["key"] = m.name
            map["judge"] = ""
            map["value"] = m.get(data)
            datas.add(map)
        }
        return datas
    }

    fun getBodyFatDataList(): List<WeightDetailsBean> {
        var data: BodyFatData = getBodyFatData();
        var datas = mutableListOf<WeightDetailsBean>()
        var clazz = data.javaClass
        for (m in clazz.declaredFields) {
            m.isAccessible = true
            var map = WeightDetailsBean(
                m.name,
                "",
                m.get(data).toString()
            )
            datas.add(map)
        }
        return datas
    }

    fun onBindService(service: ELinkBleServer) {
        bleSvcLiveData.value = service
        mBluetoothService?.setOnScanFilterListener(mOnScanFilterListener)
        mBluetoothService?.setOnCallback(mOnCallbackBle)
    }


    fun onUnBindService() {
        mBluetoothService?.setOnScanFilterListener(null)
        mBluetoothService?.setOnCallback(null)
        bleSvcLiveData.value = null;
    }

    private val mOnScanFilterListener: OnScanFilterListener = object : OnScanFilterListener {

        override fun onFilter(bleValueBean: BleValueBean): Boolean {
            var d: BondDeviceData = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
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
            state.value = State.timeout
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
    fun startScanBle(timeOut: Long = 0) {
        LogUtils.d(TAG, "startScanBle")
        if (mBluetoothService != null && !mBluetoothService!!.isScanStatus)
            this.mBluetoothService?.scanLeDevice(timeOut)
    }

    fun stopScanBle() {
        Log.d(TAG, "stopScanBle")
        this.mBluetoothService?.stopScan()
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


}