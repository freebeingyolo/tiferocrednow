package com.css.ble.framework

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.css.base.ActivityHolder
import com.pingwang.bluetoothlib.listener.OnCallbackBle
import com.pingwang.bluetoothlib.listener.OnScanFilterListener
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.server.ELinkBleServer.BluetoothBinder

class BleController private constructor() {

    companion object {
        private val TianShengKey = intArrayOf(0x54493049, 0x4132794E, 0x53783148, 0x476c6531)
        val Instance: BleController
            get() = Holder.controller
    }

    private var decryptKey: IntArray = TianShengKey;

    object Holder {
        val controller: BleController = BleController()
    }

    private var mBluetoothService: ELinkBleServer? = null


    fun startScanBle(timeOut: Long, decryptKey: IntArray) {
        this.decryptKey = decryptKey
        if (this.mBluetoothService != null) {
//            this.mBluetoothService?.scanLeDevice(timeOut)
        }
    }

    fun bindService() {
        var ctx = ActivityHolder.currentActivity()
        var bindIntent = Intent(ctx, ELinkBleServer::class.java)
        ctx.bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)
    }

    private val mFhrSCon: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            this@BleController.mBluetoothService = (service as BluetoothBinder).service
            this@BleController.mBluetoothService?.setOnScanFilterListener(this@BleController.mOnScanFilterListener)
            this@BleController.mBluetoothService?.setOnCallback(this@BleController.mOnCallbackBle)
            this@BleController.onServiceSuccess()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            this@BleController.mBluetoothService = null
            this@BleController.onServiceErr()
        }
    }

    open fun onServiceSuccess() {}
    open fun onServiceErr() {}
    open fun unbindServices() {}
    open fun onScanTimeOut() {}


    private var mOnScanFilterListener = object : OnScanFilterListener {

    }
    private val mOnCallbackBle: OnCallbackBle = object : OnCallbackBle {
        override fun onScanTimeOut() {
            this@BleController.onScanTimeOut()
        }
    }
}