package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.css.ble.bean.WeightBondData
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import com.tencent.bugly.crashreport.CrashReport

class WeightBondVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    companion object {
        const val WEIGHT_UPPER = 180;
        const val WEIGHT_LOWER = 0;
    }

    val state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    var filterDevice: BondDeviceInfo? = null
    private val filterDeviceTemp: BondDeviceInfo by lazy { BondDeviceInfo() }
    private val weigthDataTemp: WeightBondData by lazy { WeightBondData() }

    enum class State {
        begin,
        timeOut,
        found,
        done,
    }

    override fun onFilter(bleValueBean: BleValueBean): Boolean {
        return filterDevice?.run { mac == bleValueBean.mac } ?: true
    }

    override fun onBroadCastData(
        mac: String,
        dataHexStr: String,
        data: ByteArray,
        isAilink: Boolean
    ) {
        Log.d(TAG, "mac:$mac,$dataHexStr,${isAilink},${(filterDevice == null)},${state.value}")
        filterDeviceTemp.apply {
            this.mac = mac
            this.manifactureHex = dataHexStr
            this.isAilink = isAilink
        }
        mBroadcastDataParsing.dataParsing(data, isAilink)
        mBroadcastDataParsing.reset()
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
        weigthDataTemp.apply {
            setValue(
                status, tempUnit, weightUnit, weightDecimal,
                weightStatus, weightNegative, weight, adc, algorithmId, tempNegative, temp
            )
            if (weightKg < WEIGHT_LOWER || weightKg > WEIGHT_UPPER) { //错误的体重信息
                LogUtils.e(TAG, "错误设备：DeviceInfo:${filterDeviceTemp},WeightInfo:$this")
                val throwable = Throwable("错误设备：DeviceInfo:${filterDeviceTemp},WeightInfo:$this")
                CrashReport.postCatchedException(throwable)
                return
            }
            LogUtils.d(TAG, "getWeightData#$this")
            bondData.value = this
            if (filterDevice == null) {
                filterDevice = filterDeviceTemp
                state.value = State.found
                if (timeOutJob != null) cancelTimeOutTimer() //搜索到设备就取消超时
            }
        }
    }

    //绑定时，重置BroadcastDataParsing中的两个变量，即使本次与上次数据重复也透传
    private fun BroadcastDataParsing.reset() {
        val clz = javaClass
        clz.getDeclaredField("mOldNumberId").let {
            it.isAccessible = true
            it.set(this, -1)
        }
        clz.getDeclaredField("mOldStatus").let {
            it.isAccessible = true
            it.set(this, -1)
        }
    }

    override fun onScanTimeOut() {
        Log.d(TAG, "onScanTimeOut")
        state.value = State.timeOut
    }

    override fun onScanStart() {

    }

    override fun onScanStop() {
        filterDevice = null
    }

    override fun onScanTimerOutCancel() {

    }


}


