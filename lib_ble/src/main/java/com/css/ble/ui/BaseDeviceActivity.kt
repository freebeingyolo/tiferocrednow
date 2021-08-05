package com.css.ble.ui

import LogUtils
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.css.base.uibase.BaseActivity
import com.css.ble.bean.DeviceType
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.base.BaseDeviceVM
import com.css.ble.viewmodel.BleEnvVM

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseDeviceActivity<VM : BaseDeviceVM, VB : ViewBinding>() : BaseActivity<VM, VB>() {
    abstract val vmCls: Class<VM>
    abstract val vbCls: Class<VB>
    var deviceType: DeviceType = DeviceType.WEIGHT

    constructor(d: DeviceType) : this() {
        this.deviceType = d
    }


    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): VB {
        val method = vbCls.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        return method.invoke(null, inflater, parent, false) as VB
    }

    override fun initViewModel(): VM {
        return ViewModelProvider(this).get(vmCls)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //广播
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            unregisterReceiver(receiver)
        }
    }

    override fun enabledVisibleToolBar() = false

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LogUtils.d("action：" + intent!!.action)
            when (intent!!.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )) {
                        BluetoothAdapter.STATE_OFF -> BleEnvVM.bleEnabled = false
                        BluetoothAdapter.STATE_ON -> BleEnvVM.bleEnabled = true
                    }
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    BleEnvVM.locationOpened = BleUtils.isLocationEnabled(context!!)
                }
            }
        }
    }
}