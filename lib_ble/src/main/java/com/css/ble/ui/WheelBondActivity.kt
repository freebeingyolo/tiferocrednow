package com.css.ble.ui

import android.os.Bundle
import cn.wandersnail.ble.EasyBLE
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WheelBondBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.utils.UiUtils
import com.css.ble.viewmodel.WheelBondVM
import com.css.service.router.ARouterConst

/**
 * viewmodel不要持有Service的引用
 * @author yuedong
 * @date 2021-06-09
 */
@Route(path = ARouterConst.PATH_APP_BLE_WHEELBOND)
class WheelBondActivity : BaseDeviceActivity<WheelBondVM, ActivityBleEntryBinding>(DeviceType.WHEEL) {
    override val vmCls: Class<WheelBondVM> get() = WheelBondVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> get() = ActivityBleEntryBinding::class.java
    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        FragmentUtils.changeFragment(WheelBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
    }

    override fun initData() {
        super.initData()
        if (!EasyBLE.getInstance().isInitialized) {
            EasyBLE.getInstance().initialize(UiUtils.getApplication())
        }
        EasyBLE.getInstance().registerObserver(mViewModel);
    }


    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            EasyBLE.getInstance().unregisterObserver(mViewModel)
            EasyBLE.getInstance().disconnectAllConnections()
        }
    }

}