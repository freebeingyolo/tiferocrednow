package com.css.ble.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WeightBondBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.ARouterConst

@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTBOND)
class WeightBondActivity : BaseWeightActivity<WeightBondVM, ActivityBleEntryBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        FragmentUtils.changeFragment(WeightBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
    }

    override val vmCls: Class<WeightBondVM> get() = WeightBondVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> get() = ActivityBleEntryBinding::class.java

}