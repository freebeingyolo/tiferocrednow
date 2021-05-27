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


class WeightMeasureVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>() }
    val state: MutableLiveData<State> get() = _state

    enum class State {
        begin,
        doing,
        timeout,
        done
    }

    fun initOrReset() {
        _state.value = State.begin
        bondData = MutableLiveData<WeightBondData>()
    }

    override fun onFilter(bleValueBean: BleValueBean): Boolean {
        var d: BondDeviceData? = BondDeviceData.bondWeight
        return if (d == null) true else d.mac == bleValueBean.mac
    }

    override fun onBroadCastData(mac: String, dataHexStr: String, data: ByteArray, isAilink: Boolean) {
        Log.d(TAG, "mac:$mac Hex的data:  $dataHexStr " + (Looper.myLooper() == Looper.getMainLooper()))
        mBroadcastDataParsing.dataParsing(data, isAilink)
    }

    override fun onScanTimeOut() {
        _state.value = State.timeout
    }

    override fun onScanStart() {
        _state.value = State.doing
    }

    override fun onScanStop() {
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
        WeightBondData().let {
            it.setValue(
                status, tempUnit, weightUnit, weightDecimal,
                weightStatus, weightNegative, weight, adc, algorithmId, tempNegative, temp
            )
            bondData.value = it
            Log.d(TAG, "getWeightData:$it")

            if (timeOutJob != null) cancelTimeOutTimer()
            if (status == 0x00 && state.value != State.doing) {
                _state.value = State.doing
            } else if (status == 0xFF && state.value != State.done) {
                _state.value = State.done
                WeightBondData.firstWeightInfo ?: let { WeightBondData.firstWeightInfo = bondData.value }
                WeightBondData.lastWeightInfo = bondData.value
                stopScanBle()
            }
        }
    }

}