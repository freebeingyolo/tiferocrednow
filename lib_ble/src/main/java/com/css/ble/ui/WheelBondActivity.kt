package com.css.ble.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import cn.wandersnail.ble.EasyBLE
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WheelBondBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.utils.UiUtils
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.service.router.ARouterConst

/**
 * viewmodel不要持有Service的引用
 * @author yuedong
 * @date 2021-06-09
 */
@Route(path = ARouterConst.PATH_APP_BLE_WHEELBOND)
class WheelBondActivity : BaseDeviceActivity<WheelMeasureVM, ActivityBleEntryBinding>(DeviceType.WHEEL) {
    override val vmCls: Class<WheelMeasureVM> get() = WheelMeasureVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> get() = ActivityBleEntryBinding::class.java
    override fun enabledVisibleToolBar() = false

    override fun initViewModel(): WheelMeasureVM = WheelMeasureVM

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        FragmentUtils.changeFragment(WheelBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
    }

    override fun initData() {
        super.initData()
        startService(Intent(this, BleEnvService::class.java))
        bindService(Intent(this, BleEnvService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder: BleEnvService.MyBinder = service as BleEnvService.MyBinder
                binder.setViewModel(mViewModel)
                LogUtils.d("onServiceConnected:${javaClass::class.java}")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }, BIND_AUTO_CREATE)
        EasyBLE.getInstance().registerObserver(mViewModel)
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            LogUtils.d("WheelBondActivity#onStop")
            EasyBLE.getInstance().unregisterObserver(mViewModel)
        }
    }
}