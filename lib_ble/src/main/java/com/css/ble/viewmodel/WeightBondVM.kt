package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.css.ble.bean.WeightBondData
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean

class WeightBondVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    val bondDevice: MutableLiveData<BondDeviceInfo> by lazy { MutableLiveData<BondDeviceInfo>() }
    val state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    private var filterDevice: BondDeviceInfo? = null

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
        Log.d(TAG, "mac:$mac,${isAilink},${(filterDevice == null)},${state.value}")
        BondDeviceInfo().apply {
            this.mac = mac
            this.manifactureHex = dataHexStr
            bondDevice.value = this
            if (filterDevice == null) filterDevice = this
            if (state.value == State.begin) {
                state.value = State.found
                if (timeOutJob != null) cancelTimeOutTimer() //搜索到设备就取消超时
            }
        }
        mBroadcastDataParsing.dataParsing(data, isAilink)
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

    class BondDeviceInfo {
        var mac: String = ""
        var name: String = ""
        var manifactureHex: String = ""
        override fun toString(): String {
            return "BondDeviceInfo(mac='$mac', manifactureHex='$manifactureHex')"
        }
    }
}


