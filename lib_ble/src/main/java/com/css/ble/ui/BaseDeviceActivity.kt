package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.css.base.uibase.BaseActivity
import com.css.ble.bean.DeviceType
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.BaseDeviceVM
import com.css.ble.viewmodel.BleEnvVM
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseDeviceActivity<VM : BaseDeviceVM, VB : ViewBinding>(protected val deviceType: DeviceType) : BaseActivity<VM, VB>() {
    abstract val vmCls: Class<VM>
    abstract val vbCls: Class<VB>

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): VB {
        val method = vbCls.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        return method.invoke(null, inflater, parent, false) as VB
    }

    override fun initViewModel(): VM {
        return ViewModelProvider(this).get(vmCls)
    }

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
        //广播
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)
    }


    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            unregisterReceiver(receiver)
            unbindService(mFhrSCon)
            mViewModel.stopScanBle()
        }
    }

    override fun enabledVisibleToolBar() = false

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("WeightBondVM", "action：" + intent!!.action)
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

    fun refusePermissionToSetting() {
        //引导用户到设置中去进行设置
        val intent = Intent()
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }
}