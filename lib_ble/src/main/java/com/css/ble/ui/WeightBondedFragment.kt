package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.*
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

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightBoundedBinding {
        return FragmentWeightBoundedBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding!!.apply {
            mac.text = mViewModel.cachedData.mac
            macHex.text = mViewModel.cachedData.manufacturerDataHex
            unBond.setOnClickListener {
                WonderCoreCache.removeKey(WonderCoreCache.BOND_WEIGHT_INFO)
                var data = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, WeightBondVM.BondDeviceInfo::class.java)
                ToastUtils.showShort("removeKey-->${data.mac}")
                var toFragment = WeightBondFragment.newInstance()
                startFragment(toFragment)
            }
        }

    }
}