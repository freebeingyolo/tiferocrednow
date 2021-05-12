package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.viewmodel.WeightBondVM
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

class BleEntryActivity : BaseActivity<WeightBondVM, ActivityBleEntryBinding>() {

    private val mFhrSCon: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            BleLog.d(TAG, "服务与界面建立连接成功")
            val mBluetoothService = (service as ELinkBleServer.BluetoothBinder).service
            mViewModel.onBindService(mBluetoothService)

            if (mViewModel.isBleEnvironmentOk) {
                mViewModel.startScanBle()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            BleLog.d(TAG, "服务与界面连接断开")
            mViewModel.onUnBindService()
        }

    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(this).get(WeightBondVM::class.java)
    }

    override fun initViewBinding(): ActivityBleEntryBinding {
        return ActivityBleEntryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AILinkSDK.getInstance().init(this)
        BleLog.init(true)
        val bindIntent = Intent(this@BleEntryActivity, ELinkBleServer::class.java)
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)

        mViewBinding.lifecycleOwner = this
        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver, filter)
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                        BluetoothAdapter.STATE_OFF -> mViewModel.bleEnabled.value = false
                        BluetoothAdapter.STATE_ON -> mViewModel.bleEnabled.value = true
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) unregisterReceiver(receiver)
    }

}