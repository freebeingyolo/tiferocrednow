package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.base.BaseWonderActivity
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.BaseWeightVM
import com.css.ble.viewmodel.BleEnvVM
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

/**
 * @author yuedong
 * @date 2021-05-27
 */
abstract class BaseWeightActivity<VM : BaseWeightVM, VB : ViewBinding> : BaseActivity<VM, VB>() {
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
        BleLog.init(false)
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

    protected fun openBLE() {
        CommonAlertDialog(baseContext).apply {
            type = CommonAlertDialog.DialogType.Confirm
            title = "打开蓝牙"
            leftBtnText = "取消"
            rightBtnText = "确认"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    BluetoothAdapter.getDefaultAdapter().enable()
                }
            }
        }.show()
    }

    protected fun requestLocationPermission() {
        PermissionUtils.permission(PermissionConstants.LOCATION)
            .rationale { _, shouldRequest ->
                shouldRequest.again(true)
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(granted: MutableList<String>) {
                    BleEnvVM.locationPermission = true
                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    BleEnvVM.locationPermission = false
                }
            })
            .request()
    }

    protected fun openLocation() {
        CommonAlertDialog(this).apply {
            type = CommonAlertDialog.DialogType.Confirm
            title = "打开定位"
            leftBtnText = "取消"
            rightBtnText = "确认"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, 0x100)
                }
            }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            0x100 -> {
                BleEnvVM.locationOpened = BleUtils.isLocationEnabled(this)
            }
        }
    }

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