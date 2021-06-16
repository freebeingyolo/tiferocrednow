package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutWeightBondFoundBinding
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
class WheelBondDoingFragment : BaseDeviceFragment<WheelBondVM, LayoutWeightBondFoundBinding>(DeviceType.WHEEL) {
    override val vmCls get() = WheelBondVM::class.java
    override val vbCls get() = LayoutWeightBondFoundBinding::class.java


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

    }

    override fun initData() {
        super.initData()

    }
}