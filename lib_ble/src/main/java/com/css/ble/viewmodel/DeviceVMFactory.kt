package com.css.ble.viewmodel

import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceVM
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

/**
 *@author baoyuedong
 *@time 2021-08-11 15:07
 *@description 设备ViewModel工厂方法类
 */
object DeviceVMFactory {

    private val pool by lazy { ConcurrentHashMap<DeviceType, Any>() }

    fun <T> getViewModel(d: DeviceType): T where T : BaseDeviceVM {
        if (!pool.containsKey(d)) {
            val t = when (d) {
                DeviceType.WEIGHT -> WeightMeasureVM()
                DeviceType.WHEEL -> WheelMeasureVM()
                DeviceType.HORIZONTAL_BAR -> HorizontalBarVM()
                DeviceType.PUSH_UP -> PushUpVM()
                DeviceType.COUNTER -> CounterVM()
                DeviceType.ROPE -> RopeVM()
                else -> throw IllegalStateException("")
            }
            pool[d] = t
        }
        return pool[d] as T
    }

}