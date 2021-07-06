package com.css.ble.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.viewbinding.ViewBinding
import com.css.ble.bean.DeviceType
import com.css.ble.viewmodel.BaseWeightVM
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

/**
 * @author yuedong
 * @date 2021-06-14
 */
abstract class BaseWeightActivity<VM : BaseWeightVM, VB : ViewBinding> : BaseDeviceActivity<VM, VB>(DeviceType.WEIGHT) {
    private var mBluetoothService: ELinkBleServer? = null
    private val mFhrSCon: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "服务与界面建立连接成功")
            mBluetoothService = (service as ELinkBleServer.BluetoothBinder).service
            mViewModel.onBindService(mBluetoothService!!)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "服务与界面连接断开")
            mViewModel.onUnBindService()
            mBluetoothService = null
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        AILinkSDK.getInstance().init(this)
        BleLog.init(true)
        //服务
        val bindIntent = Intent(this, ELinkBleServer::class.java)
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            unbindService(mFhrSCon)
            mViewModel.stopScanBle()
            mViewModel.disconnectAll()
        }
    }
}