package com.css.ble.ui.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.base.BaseWonderFragment
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.utils.BleUtils
import com.css.ble.utils.QuickTransUtils
import com.css.ble.viewmodel.BleEnvVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseWeightFragment<VM : BaseViewModel, VB : ViewBinding> : BaseFragment<VM, VB>() {
    protected var checkEnvDone = false

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //设置标题栏
        setToolBarLeftText(BondDeviceData.displayName(BondDeviceData.TYPE_WEIGHT))
    }

    override fun onVisible() {
        super.onVisible()
        setToolBarLeftText(BondDeviceData.displayName(BondDeviceData.TYPE_WEIGHT))
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