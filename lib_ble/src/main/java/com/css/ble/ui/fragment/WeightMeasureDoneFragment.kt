package com.css.ble.ui.fragment

import android.app.ProgressDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.*
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.*
import razerdp.basepopup.BasePopupWindow

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
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun enabledVisibleToolBar(): Boolean = true

    override fun initData() {
        super.initData()
        uploadData(mViewModel.bondData.value!!.weightKg)
    }

    private fun uploadData(weight: Float) {//上传体重数据
        val mLoadingDialog: ProgressDialog = ProgressDialog.show(requireContext(), "", "正在处理数据")
        mViewModel.uploadWeightData(
            weight,
            { _, _ ->
                mLoadingDialog.dismiss()
                FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            },
            { _, msg, _ ->
                mLoadingDialog.dismiss()
                showToast(msg)
                FragmentUtils.changeFragment(WeightMeasureEndDeailFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            })

    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun onVisible() {
        super.onVisible()
        if (BondDeviceData.getDevice(DeviceType.WEIGHT) == null) {//如果已经解绑了，回到此界面在回退
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
}