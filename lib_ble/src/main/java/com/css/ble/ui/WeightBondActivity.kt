package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.R
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.BleErrorFragment
import com.css.ble.ui.fragment.WeightBondBeginFragment
import com.css.ble.ui.fragment.WeightBondDoingFragment
import com.css.ble.ui.fragment.WeightBondEndFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.ARouterConst

@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTBOND)
class WeightBondActivity : BaseWeightActivity<WeightBondVM, ActivityBleEntryBinding>() {


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewModel.state.value = WeightBondVM.State.begin
    }

    override fun initData() {
        super.initData()
        mViewModel.state.observe(this) {
            when (it) {
                WeightBondVM.State.begin -> {
                    FragmentUtils.changeFragment(WeightBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.found -> {
                    FragmentUtils.changeFragment(WeightBondDoingFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.done -> {
                    FragmentUtils.changeFragment(WeightBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.timeOut -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT).leftTitle(R.string.device_weight).create()
                }
            }
        }
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(this).get(WeightBondVM::class.java)
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityBleEntryBinding {
        return ActivityBleEntryBinding.inflate(layoutInflater, parent, false)
    }
}