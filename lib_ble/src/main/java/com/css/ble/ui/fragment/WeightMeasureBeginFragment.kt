package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.CacheKey
import com.css.service.utils.ImageUtils
import com.css.service.utils.WonderCoreCache
import razerdp.basepopup.BasePopupWindow

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
        var view = LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(DeviceType.WEIGHT.name)
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
        if (WonderCoreCache.getData(CacheKey.BOND_WEIGHT_INFO,BondDeviceData::class.java) == null) {//如果已经解绑了，回到此界面在回退
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
        mViewBinding!!.weightbonddata = WonderCoreCache.getData(CacheKey.LAST_WEIGHT_INFO,WeightBondData::class.java)
    }
}