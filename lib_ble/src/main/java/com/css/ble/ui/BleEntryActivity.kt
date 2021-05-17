package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.PATH_APP_BLE
import com.css.service.utils.SystemBarHelper
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

@Route(path = PATH_APP_BLE)
class BleEntryActivity : BaseActivity<WeightBondVM, ActivityBleEntryBinding>() {

    private val mFhrSCon: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            BleLog.d(TAG, "服务与界面建立连接成功")
            val mBluetoothService = (service as ELinkBleServer.BluetoothBinder).service
            mViewModel.onBindService(mBluetoothService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            BleLog.d(TAG, "服务与界面连接断开")
            mViewModel.onUnBindService()
        }

    }

    override fun enabledVisibleToolBar(): Boolean = false

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.WHITE
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        AILinkSDK.getInstance().init(this)
        BleLog.init(true)
        val bindIntent = Intent(this@BleEntryActivity, ELinkBleServer::class.java)
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)

        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.content, DeviceListFragment.newInstance()).commit()
    }

    override fun initData() {
        super.initData()
        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled()
        mViewModel.locationOpened.value = BleUtils.isLocationEnabled(baseContext)
        mViewModel.locationPermission.value = BleUtils.isLocationAllowed(baseContext)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(this).get(WeightBondVM::class.java)
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityBleEntryBinding {
        return ActivityBleEntryBinding.inflate(layoutInflater, parent, false)
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )) {
                        BluetoothAdapter.STATE_OFF -> mViewModel.bleEnabled.value = false
                        BluetoothAdapter.STATE_ON -> mViewModel.bleEnabled.value = true
                    }
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    mViewModel.locationOpened.value = BleUtils.isLocationEnabled(context!!)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) unregisterReceiver(receiver)
    }

}