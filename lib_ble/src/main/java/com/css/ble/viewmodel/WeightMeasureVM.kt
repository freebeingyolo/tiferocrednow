package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.HistoryRepository
import com.css.base.net.api.repository.UserRepository
import com.css.base.utils.LiveDataBus
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.WeightBondData
import com.css.service.data.LoginUserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean


class WeightMeasureVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    val state: MutableLiveData<State> get() = _state
    override val timeOut = 10 * 1000L
    private val weigthDataTemp: WeightBondData by lazy { WeightBondData() }
    private val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }
    var bondData: MutableLiveData<WeightBondData> = MutableLiveData<WeightBondData>()

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

    override fun onScanFilter(bleValueBean: BleValueBean): Boolean {
        val d: BondDeviceData? = BondDeviceData.bondWeight
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
        weigthDataTemp.apply {
            setValue(
                status, tempUnit, weightUnit, weightDecimal,
                weightStatus, weightNegative, weight, adc, algorithmId, tempNegative, temp
            )
            Log.d(TAG, "getWeightData:$weigthDataTemp")
            if (status == 0x00 && state.value != State.receiving) {
                _state.value = State.receiving
                //cancelTimeOutTimer()
                //startTimeoutTimer(5 * 1000)
            } else if (status == 0xFF && state.value != State.done) {
                if (0 == weight) { //数据异常，直接回到测量首页
                    _state.value = State.begin
                    stopScanBle()
                } else {
                    bondData.value = this
                    _state.value = State.done
                    WeightBondData.firstWeightInfo ?: let { WeightBondData.firstWeightInfo = bondData.value }
                    WeightBondData.lastWeightInfo = bondData.value
                    stopScanBle()
                }
            }
        }

    }


}