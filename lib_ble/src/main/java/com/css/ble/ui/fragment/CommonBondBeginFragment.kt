package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutCommonBondBeginBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.State
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class CommonBondBeginFragment(d: DeviceType, val model: BaseDeviceScan2ConnVM) :
    BaseDeviceFragment<BaseDeviceScan2ConnVM, LayoutCommonBondBeginBinding>(d) {

    override val vmCls get() = BaseDeviceScan2ConnVM::class.java
    override val vbCls get() = LayoutCommonBondBeginBinding::class.java

    override fun initViewModel(): BaseDeviceScan2ConnVM = model

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
        mViewModel.stateObsrv.observe(viewLifecycleOwner) {
            when (it) {
                State.found -> {
                    mViewModel.bindDevice({ _, _ ->
                        FragmentUtils.changeFragment(
                            CommonBondEndFragment::class.java,
                            "${javaClass.simpleName}#$deviceType",
                            FragmentUtils.Option.OPT_REPLACE,
                            { CommonBondEndFragment(deviceType, mViewModel) }
                        )
                    },
                        { _, msg, _ ->
                            showToast(msg)
                            onBackPressed()
                        })
                }
                State.timeOut -> {
                    BleErrorFragment.Builder
                        .errorType(ErrorType.SEARCH_TIMEOUT)
                        .leftTitle(BondDeviceData.displayName(deviceType))
                        .onDestroy {mViewModel.disconnect()  }
                        .create()
                }
            }
        }
    }
}