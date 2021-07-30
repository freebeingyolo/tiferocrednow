package com.css.ble.ui

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WeightMeasureBeginFragment
import com.css.ble.ui.fragment.WeightMeasureEndDeailFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache


@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
class WeightMeasureActivity : BaseWeightActivity<WeightMeasureVM, ActivityBleEntryBinding>() {
    override val vmCls: Class<WeightMeasureVM> = WeightMeasureVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO,WeightBondData::class.java)== null) {
            FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
        } else {
            mViewModel.bondData.value = WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO,WeightBondData::class.java)
            FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
        }
    }
}