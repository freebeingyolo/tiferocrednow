package com.css.ble.ui

import LogUtils
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.CommonMeasureBeginFragment
import com.css.ble.ui.fragment.HorizontalBarMeasureBeginFragment
import com.css.ble.ui.fragment.WheelMeasureBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.service.router.ARouterConst
import razerdp.basepopup.BasePopupWindow

@Route(path = ARouterConst.PATH_APP_BLE_TEST)
class TestActivity : BaseDeviceActivity<HorizontalBarVM, ActivityBleEntryBinding>(DeviceType.HORIZONTAL_BAR) {
    override val vmCls: Class<HorizontalBarVM> = HorizontalBarVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun initViewModel(): HorizontalBarVM {
        return HorizontalBarVM
    }

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val f = CommonMeasureBeginFragment.getExplicitFragment(HorizontalBarVM, deviceType)
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

