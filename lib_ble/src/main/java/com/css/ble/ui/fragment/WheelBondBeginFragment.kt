package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutWheelBondBeginBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WheelBondVM
import com.css.ble.viewmodel.WheelBondVM.State.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WheelBondBeginFragment : BaseDeviceFragment<WheelBondVM, LayoutWheelBondBeginBinding>(DeviceType.WHEEL) {
    override val vmCls get() = WheelBondVM::class.java
    override val vbCls get() = LayoutWheelBondBeginBinding::class.java


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding!!.startBond.setOnClickListener {
            checkBleEnv()
            lifecycleScope.launch {
                while (!checkEnvDone) delay(100)
                if (BleEnvVM.isBleEnvironmentOk) {
                    mViewModel.startScanBle()
                } else {
                    BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(deviceType)).create()
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                found -> {
                    mViewModel.bondDevice()
                    FragmentUtils.changeFragment(WheelBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                timeOut -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT)
                        .leftTitle(BondDeviceData.displayName(deviceType))
                        .create()
                }
            }
        }
    }
}