package com.css.ble.ui.fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
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
import kotlinx.coroutines.selects.whileSelect
import razerdp.basepopup.BasePopupWindow

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureBeginFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureBeginBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureBeginBinding {
        return ActivityWeightMeasureBeginBinding.inflate(inflater, parent, false).also {
            it.tvToMeasure.setOnClickListener {
                mViewModel.state.value = WeightMeasureVM.State.doing
            }
            WeightBondData.lastWeightInfoObsvr.let { it2 ->
                it2.observe(this) { it3 ->
                    it.tips.text = it3?.weightKgFmt("你上一次的体重是:%.1f kg")
                }
                it2.value?.let { it3 ->
                    it.tips.text = it3.weightKgFmt("你上一次的体重是:%.1f kg")
                }
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
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        var view = LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(WonderCoreCache.BOND_WEIGHT_INFO)
                    }
                }
            }
        })
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
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
        mViewModel.stopScanBle()
    }
}