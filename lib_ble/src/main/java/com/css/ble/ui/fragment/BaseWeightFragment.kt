package com.css.ble.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.base.BaseWonderFragment
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.BleEnvVM

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseWeightFragment<VM : BaseViewModel, VB : ViewBinding> : BaseWonderFragment<VM, VB>() {
    protected var checkEnvDone = false

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //设置标题栏
        setToolBarLeftText(getString(R.string.device_weight))
    }

    protected fun checkBleEnv() {
        checkEnvDone = false
        BleEnvVM.bleEnabled = BluetoothAdapter.getDefaultAdapter().isEnabled
        if (!BleEnvVM.bleEnabled) {
            CommonAlertDialog(requireContext()).apply {
                type = CommonAlertDialog.DialogType.Confirm
                title = "打开蓝牙"
                leftBtnText = "取消"
                rightBtnText = "确认"
                listener = object : DialogClickListener.DefaultLisener() {
                    override fun onLeftBtnClick(view: View) {
                        super.onLeftBtnClick(view)
                        checkEnvDone = true //拒绝
                    }

                    override fun onRightBtnClick(view: View) {
                        super.onRightBtnClick(view)
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(enableBtIntent, 0x101)
                    }
                }
            }.show()
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
                        val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 0x100)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0x100, 0x101 -> checkBleEnv()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        checkEnvDone = false
    }
}