package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.View
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutHorizontalbarBinding
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM

/**
 *@author chanpal
 *@time 2021-11-01
 *@description 跳绳器
 */
class RopeMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) : HorizontalBarMeasureBeginFragment(d, vm) {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding?.exerciseCountlUnit?.text = "个"
        mViewBinding?.exerciseRecord?.text = "本次训练计数"
    }
}