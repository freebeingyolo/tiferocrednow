package com.css.ble.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.FragmentWeightBondBinding
import com.css.ble.databinding.LayoutWeightBondFoundBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.ARouterConst
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightBondDoingFragment : BaseFragment<WeightBondVM, LayoutWeightBondFoundBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondFoundBinding {
        return LayoutWeightBondFoundBinding.inflate(inflater, parent, false).apply {
            research.setOnClickListener {
                mViewModel.stopScanBle()
                mViewModel.state.value = WeightBondVM.State.bondbegin
            }
            bond.setOnClickListener {
                var d = BondDeviceData(
                    mViewModel.bondDevice.value!!.mac,
                    mViewModel.bondDevice.value!!.manifactureHex,
                    BondDeviceData.TYPE_WEIGHT
                )
                WonderCoreCache.saveData(WonderCoreCache.BOND_WEIGHT_INFO, d)
                mViewModel.state.value = WeightBondVM.State.bonded
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initData() {
        super.initData()
        mViewModel.bondData.observe(viewLifecycleOwner) {
            mViewBinding!!.foundWeight.text = String.format("%.1fkg", it.weightKg)
        }
    }

    override fun enabledVisibleToolBar() = true
}