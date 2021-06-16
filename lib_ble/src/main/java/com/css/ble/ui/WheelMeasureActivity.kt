package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import cn.wandersnail.ble.EasyBLE
import cn.wandersnail.ble.ScanConfiguration
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityAbrollerBinding
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WeightMeasureBeginFragment
import com.css.ble.ui.fragment.WeightMeasureEndDeailFragment
import com.css.ble.ui.fragment.WheelMeasureBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.utils.UiUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.service.router.ARouterConst

@Route(path = ARouterConst.PATH_APP_BLE_WHEELMEASURE)
class WheelMeasureActivity : BaseDeviceActivity<WheelMeasureVM, ActivityBleEntryBinding>(DeviceType.WHEEL) {
    override val vmCls: Class<WheelMeasureVM> = WheelMeasureVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        FragmentUtils.changeFragment(WheelMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
    }

    override fun initData() {
        super.initData()
        EasyBLE.getInstance().initialize(UiUtils.getApplication())
        EasyBLE.getInstance().registerObserver(mViewModel)
    }


    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            EasyBLE.getInstance().unregisterObserver(mViewModel)
            EasyBLE.getInstance().disconnectAllConnections()
            EasyBLE.getInstance().release()
        }
    }
}