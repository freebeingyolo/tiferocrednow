package com.css.ble.viewmodel

import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.HistoryRepository
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.viewmodel.base.BaseWeightVM
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.pingwang.bluetoothlib.BroadcastDataParsing
import com.pingwang.bluetoothlib.bean.BleValueBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class WeightMeasureVM : BaseWeightVM(), BroadcastDataParsing.OnBroadcastDataParsing {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    val state: MutableLiveData<State> get() = _state
    override val timeOut = 10 * 1000L
    private val weigthDataTemp: WeightBondData by lazy { WeightBondData() }
    private val mBroadcastDataParsing by lazy { BroadcastDataParsing(this) }
    val bondData: MutableLiveData<WeightBondData> by lazy { MutableLiveData<WeightBondData>() }

    enum class State {
        begin,
        doing,//开始测量
        receiving,//收到数据
        timeout,
        done
    }

    override fun onScanFilter(bleValueBean: BleValueBean): Boolean {
        val d: BondDeviceData? = BondDeviceData.getDevice(DeviceType.WEIGHT)
        return if (d == null) true else d.mac == bleValueBean.mac
    }

    override fun onBroadCastData(mac: String, dataHexStr: String, data: ByteArray, isAilink: Boolean) {
        Log.d(TAG, "mac:$mac Hex的data:  $dataHexStr " + (Looper.myLooper() == Looper.getMainLooper()))
        mBroadcastDataParsing.dataParsing(data, isAilink)
    }

    override fun onTimerTimeout() {
        _state.value = State.timeout
    }
    override fun onTimerCancel() {
    }

    override fun onScanStart() {
        _state.value = State.doing
    }

    override fun onScanStop() {
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
                    if (WonderCoreCache.getData(CacheKey.FIRST_WEIGHT_INFO, WeightBondData::class.java) == null) {
                        WonderCoreCache.saveData(CacheKey.FIRST_WEIGHT_INFO, bondData.value)
                    }
                    WonderCoreCache.saveData(CacheKey.LAST_WEIGHT_INFO, bondData.value)
                    stopScanBle()
                }
            }
        }

    }

    fun uploadWeightData(
        weight: Float,
        adc: Int,
        success: (msg: String?, d: Any?) -> Unit,
        failed: (Int, String?, d: Any?) -> Unit
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val t1 = System.currentTimeMillis()
                    val uid = WonderCoreCache.getLoginInfo()!!.userInfo.userId
                    val ret = HistoryRepository.uploadMeasureWeight(uid, weight, adc)
                    delay(System.currentTimeMillis() + 1000 - t1)
                    ret
                }
            },
            { msg, d ->
                success(msg, d)
            },
            { code, msg, d ->
                failed(code, msg, d)
            }
        )
    }
}