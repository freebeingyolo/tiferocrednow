package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.css.ble.bean.WeightBondData
import com.css.ble.bean.BondDeviceData
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean


class WeightMeasureVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>() }
    val state: MutableLiveData<State> get() = _state
    protected override val timeOut = 10 * 1000L

    enum class State {
        begin,
        doing,//开始测量
        receiving,//收到数据
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

            if (status == 0x00 && state.value != State.receiving) {
                _state.value = State.receiving
            } else if (status == 0xFF && state.value != State.done) {
                if (0 == weight) { //数据异常，直接回到测量首页
                    _state.value = State.begin
                    stopScanBle()
                } else {
                    _state.value = State.done
                    WeightBondData.firstWeightInfo ?: let { WeightBondData.firstWeightInfo = bondData.value }
                    WeightBondData.lastWeightInfo = bondData.value
                    stopScanBle()
                }
            }
        }
    }

}