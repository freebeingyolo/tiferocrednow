package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutPlayRecommendItemBinding
import com.css.ble.databinding.LayoutWheelBondBeginBinding
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.ble.viewmodel.WheelMeasureVM.State
import com.css.service.data.CourseDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WheelBondBeginFragment : BaseDeviceFragment<WheelMeasureVM, LayoutWheelBondBeginBinding>(DeviceType.WHEEL) {
    override val vmCls get() = WheelMeasureVM::class.java
    override val vbCls get() = LayoutWheelBondBeginBinding::class.java

    override fun initViewModel(): WheelMeasureVM = WheelMeasureVM

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding!!.model = mViewModel
        mViewBinding!!.lifecycleOwner = viewLifecycleOwner
        mViewBinding!!.startBond.setOnClickListener {
            checkBleEnv()
            lifecycleScope.launch {
                while (!checkEnvDone) delay(100)
                if (BleEnvVM.isBleEnvironmentOk) {
                    mViewModel.startScanBle()
                } else {
                    BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType)
                        .leftTitle(BondDeviceData.displayName(deviceType))
                        .create()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mViewModel.stopScanBle()
        //mViewModel.disconnect()
    }

    override fun initData() {
        super.initData()
        mViewModel.state = State.disconnected
        mViewModel.stateObsrv.observe(viewLifecycleOwner) {
            when (it) {
                State.found -> {
                    mViewModel.bindDevice({ _, _ ->
                        FragmentUtils.changeFragment(WheelBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                    }, { _, msg, _ ->
                        showToast(msg)
                        FragmentUtils.changeFragment(WheelBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                    })
                }
                State.timeOut -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT).leftTitle(BondDeviceData.displayName(deviceType)).create()
                }
            }
        }
    }
}