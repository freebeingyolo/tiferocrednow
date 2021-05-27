package com.css.ble.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.LayoutWeightBondBeginBinding
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.WeightBondVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightBondBeginFragment : BaseFragment<WeightBondVM, LayoutWeightBondBeginBinding>() {
    private var startTime = 0L
    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondBeginBinding {
        return LayoutWeightBondBeginBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
        startTime = System.currentTimeMillis()
    }

    override fun initData() {
        super.initData()
        mViewModel.bondData.observe(viewLifecycleOwner, object : Observer<WeightBondData> {
            override fun onChanged(t: WeightBondData?) {
                if (mViewModel.state.value != WeightBondVM.State.found) {
                    mViewModel.state.value = WeightBondVM.State.found
                }
            }
        })

        mViewModel.mBluetoothServiceObsvr.observe(viewLifecycleOwner) {
            if (it != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    if (System.currentTimeMillis() - startTime < 200) delay(startTime + 200 - System.currentTimeMillis())
                    if (BleEnvVM.isBleEnvironmentOk) {
                        mViewModel.startScanBle()
                    } else {
                        BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(R.string.device_weight).create()
                    }
                }
            }
        }
    }
}