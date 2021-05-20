package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.FragmentWeightBoundedBinding
import com.css.ble.ui.WeightBondActivity
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.utils.WonderCoreCache


/**
 * @author yuedong
 * @date 2021-05-12
 */
class WeightBondedFragment : BaseFragment<WeightBondVM, FragmentWeightBoundedBinding>() {

    companion object {
        fun newInstance(): WeightBondedFragment {
            return WeightBondedFragment()
        }
    }

    override fun enabledVisibleToolBar() = true

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentWeightBoundedBinding {
        return FragmentWeightBoundedBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding!!.apply {
            var data = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
            mac.text = data.mac
            macHex.text = data.manufacturerDataHex
            unBond.setOnClickListener {
                WonderCoreCache.removeKey(WonderCoreCache.BOND_WEIGHT_INFO)
                var data = WonderCoreCache.getData(
                    WonderCoreCache.BOND_WEIGHT_INFO,
                    WeightBondVM.BondDeviceInfo::class.java
                )
                ToastUtils.showShort("removeKey-->${data.mac}")
                activity?.let { it1 -> WeightBondActivity.starActivity(it1) }
            }
        }

    }
}