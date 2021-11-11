package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.View
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM

/**
 *@author baoyuedong
 *@time 2021-08-06 11:34
 *@description
 */
class CounterMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) : HorizontalBarMeasureBeginFragment(d, vm) {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding?.modeContainer?.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        //退出页面进行数据上传
        mViewModel.finishExercise()
    }
}