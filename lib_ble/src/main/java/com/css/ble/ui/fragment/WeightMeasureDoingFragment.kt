package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.css.base.view.ToolBarView
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityWeightMeasureDoingBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WeightMeasureVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.css.ble.viewmodel.WeightMeasureVM.State
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureDoingFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureDoingBinding>() {
    private var startTime = 0L



    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureDoingBinding {
        return ActivityWeightMeasureDoingBinding.inflate(inflater, parent, false).also {
            mViewModel.bondData.observe(viewLifecycleOwner) { it2 ->
                it.tvWeightNum.text = it2.weightKgFmt("%.1f kg")
            }
        }
    }

    override fun initViewModel(): WeightMeasureVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //setUpJumpToDeviceInfo()
    }

    override fun initData() {
        super.initData()
        mViewModel.state.value = WeightMeasureVM.State.doing
        mViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                State.begin -> {
                    FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                State.done -> {

                    if (WonderCoreCache.getData(CacheKey.FIRST_WEIGHT_INFO, BondDeviceData::class.java) == null) {
                        //去掉WeightMeasureBeginFragment
                        FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                    }
                    FragmentUtils.changeFragment(WeightMeasureDoneFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                State.timeout -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT).leftTitle(BondDeviceData.displayName(DeviceType.WEIGHT))
                        .create()
                }
            }
        }

        //检查环境并搜搜
        checkBleEnv()
        lifecycleScope.launch {
            while (!checkEnvDone) delay(100)
            if (BleEnvVM.isBleEnvironmentOk) {
                //至少停留200ms
                if (System.currentTimeMillis() - startTime < 200) delay(startTime + 200 - System.currentTimeMillis())
                mViewModel.startScanBle()
            } else {
                BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(DeviceType.WEIGHT)).create()
            }
        }
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun enabledVisibleToolBar(): Boolean = true

    override fun onVisible() {
        super.onVisible()
        startTime = System.currentTimeMillis()

    }

    override fun onInVisible() {
        super.onInVisible()
        mViewModel.stopScanBle()
    }
}