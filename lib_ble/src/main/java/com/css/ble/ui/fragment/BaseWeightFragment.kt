package com.css.ble.ui.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.utils.BleUtils
import com.css.ble.utils.QuickTransUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.service.utils.ImageUtils

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseWeightFragment<VM : BaseViewModel, VB : ViewBinding> : BaseFragment<VM, VB>() {
    protected var checkEnvDone = false
    val deviceType = DeviceType.WEIGHT

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //设置标题栏
        setToolBarLeftText(BondDeviceData.displayName(DeviceType.WEIGHT))
    }

    override fun onVisible() {
        super.onVisible()
        setToolBarLeftText(BondDeviceData.displayName(DeviceType.WEIGHT))
    }

    fun setUpJumpToDeviceInfo() {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(deviceType.name)
                    }
                }
            }
        })
    }

    protected fun checkBleEnv() {
        checkEnvDone = false
        BleEnvVM.bleEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled
        if (!BleEnvVM.bleEnabled) {
            QuickTransUtils.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) { _, requstCode, resultCode, data ->
                if (resultCode != Activity.RESULT_OK) {
                    checkEnvDone = true //拒绝
                } else {
                    BleEnvVM.bleEnabled = true
                    checkBleEnv()
                }
            }
            return
        }

        BleEnvVM.locationOpened = BleUtils.isLocationEnabled(requireContext())
        if (!BleEnvVM.locationOpened) {
            CommonAlertDialog(requireContext()).apply {
                type = CommonAlertDialog.DialogType.Confirm
                title = "打开定位"
                leftBtnText = "取消"
                rightBtnText = "确认"
                listener = object : DialogClickListener.DefaultLisener() {
                    override fun onLeftBtnClick(view: View) {
                        super.onLeftBtnClick(view)
                        checkEnvDone = true //拒绝
                    }

                    override fun onRightBtnClick(view: View) {
                        super.onRightBtnClick(view)
                        QuickTransUtils.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) { _, requstCode, resultCode, data ->
                            if (resultCode != Activity.RESULT_OK) {
                                checkEnvDone = true //拒绝
                            } else {
                                BleEnvVM.locationOpened = true //使用activity打开，不等定位打开广播，直接置true
                                checkBleEnv()
                            }
                        }
                    }
                }
            }.show()
            return
        }
        BleUtils.isLocationAllowed(requireContext(), object : PermissionUtils.FullCallback {
            override fun onDenied(deniedForever: MutableList<String>, denied: MutableList<String>) {
                BleEnvVM.locationPermission = false
                checkEnvDone = true //拒绝
            }

            override fun onGranted(granted: MutableList<String>) {
                BleEnvVM.locationPermission = true
                checkEnvDone = true
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        checkEnvDone = false
    }
}