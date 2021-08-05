package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityAbrollerBinding
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.State
import com.css.service.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class CommonMeasureBeginFragment(d: DeviceType, val model: BaseDeviceScan2ConnVM):
    BaseDeviceFragment<BaseDeviceScan2ConnVM, ActivityAbrollerBinding>(d) {

    override fun initViewModel(): BaseDeviceScan2ConnVM = model
    override val vmCls: Class<BaseDeviceScan2ConnVM> get() = BaseDeviceScan2ConnVM::class.java
    override val vbCls: Class<ActivityAbrollerBinding> get() = ActivityAbrollerBinding::class.java

    override fun initData() {
        super.initData()
        mViewModel.fetchRecommentation()
    }

    private fun refreshBottom(s: State) {

    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(DeviceType.WHEEL.name)
                    }
                }
            }
        })

    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun enabledVisibleToolBar(): Boolean = true


    fun startConnect() {
        //检查环境并搜搜
        checkBleEnv()
        lifecycleScope.launch {
            while (!checkEnvDone) delay(100)
            if (BleEnvVM.isBleEnvironmentOk) {
                if (mViewModel.state == State.disconnected) {
                    mViewModel.connect()
                }
            } else {
                BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(deviceType)).create()
            }
        }
    }

}