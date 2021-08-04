package com.css.ble.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.CommonBondBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.CounterVM
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.PushUpVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.WorkMode
import com.css.service.router.ARouterConst

@Route(path = ARouterConst.PATH_APP_BLE_COMMON)
class CommonDeviceActivity : BaseDeviceActivity<BaseDeviceScan2ConnVM, ActivityBleEntryBinding>() {
    private val vmMap = mapOf(
        DeviceType.HORIZONTAL_BAR to HorizontalBarVM,
        DeviceType.PUSH_UP to PushUpVM,
        DeviceType.COUNTER to CounterVM,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        deviceType = DeviceType.values()[intent.getIntExtra("deviceType", 0)]
        super.onCreate(savedInstanceState)
    }

    override fun initData() {
        super.initData()
        mViewModel.workMode = WorkMode.values()[(intent.getIntExtra("mode", 0))]
        FragmentUtils.changeFragment(
            CommonBondBeginFragment::class.java,
            "${javaClass.simpleName}#$deviceType",
            FragmentUtils.Option.OPT_REPLACE,
            { CommonBondBeginFragment(deviceType, mViewModel) }
        )
        startService(Intent(this, BleEnvService::class.java))
        bindService(Intent(this, BleEnvService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder: BleEnvService.MyBinder = service as BleEnvService.MyBinder
                binder.setViewModel(mViewModel,mViewModel)
            }
            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }, BIND_AUTO_CREATE)
    }

    override val vmCls get() = BaseDeviceScan2ConnVM::class.java
    override val vbCls get() = ActivityBleEntryBinding::class.java

    override fun initViewModel(): BaseDeviceScan2ConnVM {
        return vmMap[deviceType]!!
    }
}