package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
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
class WeightBondBeginFragment : BaseWeightFragment<WeightBondVM, LayoutWeightBondBeginBinding>() {
    private var startTime = 0L

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondBeginBinding {
        return LayoutWeightBondBeginBinding.inflate(inflater, parent, false)
    }
    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        startTime = System.currentTimeMillis()
    }

    override fun initData() {
        super.initData()
        checkBleEnv()
        mViewModel.mBluetoothServiceObsvr.observe(viewLifecycleOwner) { it ->
            if (it != null) {
                lifecycleScope.launch(Dispatchers.Main) {
                    while (!checkEnvDone) delay(100)
                    if (BleEnvVM.isBleEnvironmentOk) {
                        if (System.currentTimeMillis() - startTime < 200) delay(startTime + 200 - System.currentTimeMillis())
                        mViewModel.startScanBle()
                    } else {
                        BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(BondDeviceData.TYPE_WEIGHT)).create()
                    }
                }
            }
        }
    }

}