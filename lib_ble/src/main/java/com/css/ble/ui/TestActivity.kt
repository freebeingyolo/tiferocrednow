package com.css.ble.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.CommonMeasureBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.service.router.ARouterConst

@Route(path = ARouterConst.PATH_APP_BLE_TEST)
class TestActivity : BaseDeviceActivity<HorizontalBarVM, ActivityBleEntryBinding>(DeviceType.HORIZONTAL_BAR) {
    override val vmCls: Class<HorizontalBarVM> = HorizontalBarVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun initViewModel(): HorizontalBarVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val f = CommonMeasureBeginFragment.getExplicitFragment(mViewModel, deviceType)
        FragmentUtils.changeFragment(
            f::class.java,
            "${javaClass.simpleName}#$deviceType",
            FragmentUtils.Option.OPT_REPLACE,
            { f }
        )
    }

    override fun onResume() {
        super.onResume()
    }


    override fun initData() {
        super.initData()
    }

}

