package com.css.ble.ui.fragment

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.ActivityWeightMeasureDoingBinding
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.ImageUtils
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import razerdp.basepopup.BasePopupWindow

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureDoingFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureDoingBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureDoingBinding {
        return ActivityWeightMeasureDoingBinding.inflate(inflater, parent, false).also {
            mViewModel.bondData.observe(viewLifecycleOwner) { it2 ->
                it.tvWeightNum.text = it2.weightKgFmt("%.1f kg")
            }
        }
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun initData() {
        super.initData()
        mViewModel.state.value = WeightMeasureVM.State.doing
        mViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                WeightMeasureVM.State.begin -> {
                    FragmentUtils.changeFragment(WeightMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightMeasureVM.State.done -> {
                    FragmentUtils.changeFragment(WeightMeasureDoneFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightMeasureVM.State.timeout -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT)
                        .leftTitle(BondDeviceData.displayName(BondDeviceData.TYPE_WEIGHT)).create()
                }
            }
        }

        //检查环境并搜搜
        checkBleEnv()
        lifecycleScope.launch {
            while (!checkEnvDone) delay(100)
            if (BleEnvVM.isBleEnvironmentOk) {
                mViewModel.startScanBle()
            } else {
                BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType)
                    .leftTitle(BondDeviceData.displayName(BondDeviceData.TYPE_WEIGHT)).create()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }
    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }
    override fun enabledVisibleToolBar(): Boolean = true

    override fun onVisible() {
        super.onVisible()
        if (BondDeviceData.bondWeight == null) {//如果已经解绑了，回到此界面在回退
            CommonAlertDialog(requireContext()).apply {
                type = CommonAlertDialog.DialogType.Image
                imageResources = R.mipmap.icon_tick
                content = getString(R.string.please_bond_first)
                onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        requireActivity().finish()
                        ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_DEVICELIST).navigation()
                    }
                }
            }.show()
        }
    }

    override fun onInVisible() {
        super.onInVisible()
        mViewModel.stopScanBle()
    }
}