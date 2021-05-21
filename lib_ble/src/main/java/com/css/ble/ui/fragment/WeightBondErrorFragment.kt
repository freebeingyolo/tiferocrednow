package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.LayoutBondErrorBinding
import com.css.ble.viewmodel.WeightBondVM

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightBondErrorFragment : BaseFragment<WeightBondVM, LayoutBondErrorBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutBondErrorBinding {
        return LayoutBondErrorBinding.inflate(inflater, parent, false)
    }

    override fun enabledVisibleToolBar() = true

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding!!.error.text = when {
            !mViewModel.bleEnabled.value!! -> "蓝牙未打开"
            !mViewModel.locationOpened.value!! -> "定位未打开"
            !mViewModel.locationPermission.value!! -> "定位权限未允许"
            else -> "未知错误"
        }
    }
}