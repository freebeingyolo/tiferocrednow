package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.view.ToolBarView
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureBeginFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureBeginBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureBeginBinding {
        return ActivityWeightMeasureBeginBinding.inflate(inflater, parent, false).also { it ->
            it.tvToMeasure.setOnClickListener {
                mViewModel.state.value = WeightMeasureVM.State.doing
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                WeightMeasureVM.State.doing -> {
                    FragmentUtils.changeFragment(WeightMeasureDoingFragment::class.java, FragmentUtils.Option.OPT_ADD)
                }
            }
        }
        mViewBinding!!.lifecycleOwner = this
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setUpJumpToDeviceInfo()
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun initViewModel(): WeightMeasureVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun enabledVisibleToolBar(): Boolean = true

    override fun onVisible() {
        super.onVisible()
        mViewBinding!!.weightbonddata = WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO,WeightBondData::class.java)
    }
}