package com.css.ble.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WheelMeasureBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.service.router.ARouterConst
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

@Route(path = ARouterConst.PATH_APP_BLE_WHEELMEASURE)
class WheelMeasureActivity : BaseDeviceActivity<WheelMeasureVM, ActivityBleEntryBinding>(DeviceType.WHEEL) {
    override val vmCls: Class<WheelMeasureVM> = WheelMeasureVM::class.java
    override val vbCls: Class<ActivityBleEntryBinding> = ActivityBleEntryBinding::class.java

    override fun initViewModel(): WheelMeasureVM {
        return WheelMeasureVM
    }

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        FragmentUtils.changeFragment(WheelMeasureBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            .apply { arguments = Bundle().apply { putBoolean("autoConnect", intent.getBooleanExtra("autoConnect", false)) } }
        WonderCoreCache.getLiveData2(CacheKey.BOND_WHEEL_INFO).observe(this) { //解绑自动断开并结束
            if (it == null) {
                mViewModel.disconnect()
                finish()
            }
        }
    }

    override fun initData() {
        super.initData()
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


    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            LogUtils.d("WheelMeasureActivity#onStop")
            mViewModel.stopExercise()
        }
    }

}

