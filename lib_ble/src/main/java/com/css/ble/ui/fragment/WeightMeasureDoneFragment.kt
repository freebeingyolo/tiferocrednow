package com.css.ble.ui.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.*
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.ImageUtils
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureDoneFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureDoneBinding>() {
    private var dialog: ProgressDialog? = null

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureDoneBinding {
        return ActivityWeightMeasureDoneBinding.inflate(inflater, parent, false).also {
            mViewModel.bondData.observe(viewLifecycleOwner) { it2 ->
                it.tvWeight.text = it2.weightKgFmt
            }
        }
    }


    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun enabledVisibleToolBar(): Boolean = true

    override fun initData() {
        super.initData()
        dialog = ProgressDialog.show(requireContext(), "", "正在处理数据")
        lifecycleScope.launch {
            delay(2000)
            FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            dialog?.dismiss()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //设置标题栏
        setToolBarLeftText(getString(R.string.device_weight))
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
    override fun onDetach() {
        super.onDetach()
        dialog?.dismiss()
    }

    override fun onVisible() {
        super.onVisible()
        if (BondDeviceData.bondWeight == null) {//如果已经解绑了，回到此界面在回退
            requireActivity().finish()
            showCenterToast("请先绑定设备")
            ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_DEVICELIST).navigation()
        }
    }
}