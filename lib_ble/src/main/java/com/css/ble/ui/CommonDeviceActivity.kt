package com.css.ble.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import cn.wandersnail.ble.EasyBLE
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.CommonBondBeginFragment
import com.css.ble.ui.fragment.CommonMeasureBeginFragment
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.CounterVM
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.PushUpVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.WorkMode
import com.css.service.router.ARouterConst
import razerdp.basepopup.BasePopupWindow

@Route(path = ARouterConst.PATH_APP_BLE_COMMON)
class CommonDeviceActivity : BaseDeviceActivity<BaseDeviceScan2ConnVM, ActivityBleEntryBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        deviceType = DeviceType.values()[intent.getIntExtra("deviceType", 0)]
        LogUtils.d("deviceType:$deviceType")
        super.onCreate(savedInstanceState)
    }

    override fun initData() {
        super.initData()
        mViewModel.workModeObsrv.observe(this) {
            when (it) {
                WorkMode.BOND -> {
                    FragmentUtils.changeFragment(
                        CommonBondBeginFragment::class.java,
                        "${javaClass.simpleName}#$deviceType",
                        FragmentUtils.Option.OPT_REPLACE,
                        { CommonBondBeginFragment(deviceType, mViewModel) }
                    )
                }
                WorkMode.MEASURE -> {
                    val f = CommonMeasureBeginFragment.getExplicitFragment(mViewModel, deviceType)
                    FragmentUtils.changeFragment(
                        f::class.java,
                        "${javaClass.simpleName}#$deviceType",
                        FragmentUtils.Option.OPT_REPLACE,
                        { f }
                    )
                }
            }
        }
        mViewModel.workMode = WorkMode.values()[(intent.getIntExtra("mode", 0))]

        startService(Intent(this, BleEnvService::class.java))
        bindService(Intent(this, BleEnvService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder: BleEnvService.MyBinder = service as BleEnvService.MyBinder
                binder.setViewModel(mViewModel)
                LogUtils.d("onServiceConnected:${javaClass::class.java}")
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }, BIND_AUTO_CREATE)
        EasyBLE.getInstance().registerObserver(mViewModel)
    }

    override val vmCls get() = BaseDeviceScan2ConnVM::class.java
    override val vbCls get() = ActivityBleEntryBinding::class.java

    override fun initViewModel(): BaseDeviceScan2ConnVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun onResume() {
        super.onResume()
        //测量界面发生解绑
        if (mViewModel.workMode == WorkMode.MEASURE && BondDeviceData.getDevice(deviceType) == null) {
            mViewModel.disconnect()
            CommonAlertDialog(this).apply {
                type = CommonAlertDialog.DialogType.Image
                imageResources = R.mipmap.icon_tick
                content = getString(R.string.please_bond_first)
                onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        mViewModel.disconnect()
                        ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_DEVICELIST).navigation(context,
                            object : NavCallback() {
                                override fun onArrival(postcard: Postcard?) {
                                    finish()
                                }
                            })
                    }
                }
            }.show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            EasyBLE.getInstance().unregisterObserver(mViewModel)
        }
    }
}