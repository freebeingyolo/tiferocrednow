package com.css.ble.viewmodel

import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cn.wandersnail.ble.Device
import cn.wandersnail.commons.util.StringUtils
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import java.util.*

/**
 *@author baoyuedong
 *@time 2021-08-03 17:23
 *@description 单杠
 */
object HorizontalBarVM : BaseDeviceScan2ConnVM() {
    override val deviceType: DeviceType = DeviceType.HORIZONTAL_BAR

    val UUID_SRVC = "0000ffb0-0000-1000-8000-00805f9b34fb"
    val UUID_WRITE = "0000ffb1-0000-1000-8000-00805f9b34fb"
    val UUID_NOTIFY = "0000ffb2-0000-1000-8000-00805f9b34fb"
    val modeObsvr: LiveData<Mode> by lazy { MutableLiveData(Mode.byTime60) }
    val modeObsvrStr: LiveData<String> = Transformations.map(modeObsvr) {
        when (it) {
            Mode.byCount -> getString(R.string.byCount)
            Mode.byTime30 -> String.format(getString(R.string.byTime), 30)
            Mode.byTime60 -> String.format(getString(R.string.byTime), 60)
            Mode.byTime90 -> String.format(getString(R.string.byTime), 90)
        }
    }
    //transformations


    var mode: Mode
        set(value) {
            (modeObsvr as MutableLiveData).value = value
        }
        get() = modeObsvr.value!!

    enum class Mode {
        byCount,
        byTime30,
        byTime60,
        byTime90
    }

    override fun filterName(name: String): Boolean {
        return name.startsWith("Hi-DG")
    }

    override fun filterUUID(uuid: UUID): Boolean {
        return uuid.toString() == UUID_SRVC
    }

    fun getModels(): List<String> {
        return listOf(
            getString(R.string.byCount),
            String.format(getString(R.string.byTime), 30),
            String.format(getString(R.string.byTime), 60),
            String.format(getString(R.string.byTime), 90)
        )
    }

    override val bonded_tip: String get() = "单杠已连接成功，开启你的健康之旅吧！"

    override fun onCharacteristicChanged(device: Device, service: UUID, characteristic: UUID, value: ByteArray) {
        super.onCharacteristicChanged(device, service, characteristic, value)
    }

}