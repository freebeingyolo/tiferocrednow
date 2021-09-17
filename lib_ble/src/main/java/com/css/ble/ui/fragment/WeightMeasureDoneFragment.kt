package com.css.ble.ui.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import com.css.base.view.ToolBarView
import com.css.ble.databinding.*
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureDoneFragment : BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureDoneBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureDoneBinding {
        return ActivityWeightMeasureDoneBinding.inflate(inflater, parent, false).also {
            mViewModel.bondData.observe(viewLifecycleOwner) { it2 ->
                it.tvWeight.text = it2.weightKgFmt
            }
        }
    }


    override fun initViewModel(): WeightMeasureVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun enabledVisibleToolBar(): Boolean = true

    override fun initData() {
        super.initData()

        val mLoadingDialog: ProgressDialog = ProgressDialog.show(requireContext(), "", "正在处理数据")
        mViewModel.uploadWeightData(
            mViewModel.bondData.value!!.weightKg,
            mViewModel.bondData.value!!.adc,
            { _, _ ->
                mLoadingDialog.dismiss()
                FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            },
            { _, msg, _ ->
                mLoadingDialog.dismiss()
                showCenterToast(msg)
                FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            })
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //setUpJumpToDeviceInfo()
    }

}