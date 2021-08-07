package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityHorizontalbarBinding
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM

/**
 *@author baoyuedong
 *@time 2021-08-06 11:34
 *@description
 */
class HorizontalBarMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) :
    CommonMeasureBeginFragment<ActivityHorizontalbarBinding>(d, vm) {

    override val vbCls: Class<ActivityHorizontalbarBinding> get() = ActivityHorizontalbarBinding::class.java

    override fun initData() {
        super.initData()
        mViewBinding!!.model = mViewModel as HorizontalBarVM

    }

}