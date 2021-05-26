package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.*
import com.css.ble.utils.BleUtils
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.ARouterConst
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

@Route(path = ARouterConst.PATH_APP_BLE_WEIGHTBOND)
class WeightBondActivity : BaseActivity<WeightBondVM, ActivityBleEntryBinding>() {
    companion object {
        const val GPS_REQUEST_CODE: Int = 0x01
    }

    var mBluetoothService: ELinkBleServer? = null

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

    override fun enabledVisibleToolBar() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        AILinkSDK.getInstance().init(this)
        BleLog.init(false)

        val bindIntent = Intent(this, ELinkBleServer::class.java)
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)
        mViewModel.state.value = WeightBondVM.State.bondbegin
    }

    override fun initData() {
        super.initData()
        BleEnvVM.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled
        BleEnvVM.locationOpened.value = BleUtils.isLocationEnabled(baseContext)
        BleEnvVM.locationPermission.value = BleUtils.isLocationAllowed(baseContext)

        BleEnvVM.openBLE.observe(this) {
            if (it) openBLE()
        }
        BleEnvVM.requestLocationPermission.observe(this) {
            if (it) requestLocationPermission()
        }
        BleEnvVM.openLocation.observe(this) {
            if (it) openLocation()
        }
        mViewModel.state.observe(this) {
            when (it) {
                WeightBondVM.State.bondbegin -> {
                    FragmentUtils.changeFragment(WeightBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.found -> {
                    FragmentUtils.changeFragment(WeightBondDoingFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.bonded -> {
                    FragmentUtils.changeFragment(WeightBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.bondingTimeOut -> {
                    BleErrorFragment.Builder.errorType(ErrorType.SEARCH_TIMEOUT).leftTitle(R.string.device_weight).create()
                }
            }
        }
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
                        BluetoothAdapter.STATE_OFF -> BleEnvVM.bleEnabled.value = false
                        BluetoothAdapter.STATE_ON -> BleEnvVM.bleEnabled.value = true
                    }
                }
                LocationManager.PROVIDERS_CHANGED_ACTION -> {
                    BleEnvVM.locationOpened.value = BleUtils.isLocationEnabled(context!!)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            unbindService(mFhrSCon)
            unregisterReceiver(receiver)
            mViewModel.stopScanBle()
        }
    }

    private fun openBLE() {
        CommonAlertDialog(this).apply {
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

    private fun requestLocationPermission() {
        PermissionUtils.permission(PermissionConstants.LOCATION)
            .rationale { _, shouldRequest ->
                shouldRequest.again(true)
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(granted: MutableList<String>) {
                    BleEnvVM.locationPermission.value = true
                }

                override fun onDenied(
                    deniedForever: MutableList<String>,
                    denied: MutableList<String>
                ) {
                    BleEnvVM.locationPermission.value = false
                }
            })
            .request()
    }

    private fun openLocation() {
        CommonAlertDialog(this).apply {
            type = CommonAlertDialog.DialogType.Confirm
            title = "打开定位"
            leftBtnText = "取消"
            rightBtnText = "确认"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, GPS_REQUEST_CODE)
                }
            }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            GPS_REQUEST_CODE -> {
                BleEnvVM.locationOpened.value = BleUtils.isLocationEnabled(this)
            }
        }
    }

}