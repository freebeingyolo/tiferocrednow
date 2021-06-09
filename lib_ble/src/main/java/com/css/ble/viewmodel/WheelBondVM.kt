package com.css.ble.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.pingwang.bluetoothlib.bean.BleValueBean

/*
AbRoller-2FD0
mac:84:c2:e4:05:2f:d0
notify : 66 06 01 00 64 01
         56 06 00 00 64 01
         65 06 01 00 3c 01
x54		查询当前健腹轮个数
0x55	65535（单位秒）16bit	设置健腹轮倒计时时间
0x56	65535（单位个数）16bit	设置健腹轮倒计数个数
0x57		查询最近7天个数
0x58	207e405020812	设置当前时间
0x59	65535（单位个数）16bit	设置当前健腹轮总数
*/

class WheelBondVM : BaseDeviceVM() {
    private val _state: MutableLiveData<State> by lazy { MutableLiveData<State>(State.begin) }
    val state: MutableLiveData<State> get() = _state
    var filterDevice: BondDeviceInfo? = null

    enum class State {
        begin,
        timeOut,
        found,
        done,
    }

    override fun onFilter(bleValueBean: BleValueBean): Boolean {
        return true
    }

    override fun onBroadCastData(mac: String, dataHexStr: String, data: ByteArray, isAilink: Boolean) {
        Log.d(TAG, "mac:$mac,$dataHexStr,${isAilink},${(filterDevice == null)},${state.value}")
    }

    override fun onScanStart() {
    }

    override fun onScanTimeOut() {
    }

    override fun onScanStop() {
    }

    override fun onScanTimerOutCancel() {

    }

}


