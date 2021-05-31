package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.R
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.BleErrorFragment
import com.css.ble.ui.fragment.WeightMeasureBeginFragment
import com.css.ble.ui.fragment.WeightMeasureDoingFragment
import com.css.ble.ui.fragment.WeightMeasureDoneFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.ble.viewmodel.WeightMeasureVM.State
import com.css.service.router.ARouterConst


@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
class WeightMeasureActivity : BaseWeightActivity<WeightMeasureVM, ActivityBleEntryBinding>() {


    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewModel.state.value = State.begin
    }

    override fun initData() {
        super.initData()
        mViewModel.state.observe(this) {
            when (it) {
                State.begin -> {
                    FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                State.doing -> {
                    FragmentUtils.changeFragment(WeightMeasureDoingFragment::class.java, FragmentUtils.Option.OPT_ADD)
                }
                State.done -> {
                    FragmentUtils.changeFragment(WeightMeasureDoneFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                State.timeout -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT).leftTitle(R.string.device_weight).create()
                }
            }
        }
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(this).get(WeightMeasureVM::class.java)
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityBleEntryBinding {
        return ActivityBleEntryBinding.inflate(layoutInflater, parent, false)
    }
}