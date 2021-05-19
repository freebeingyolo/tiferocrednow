package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.*
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.databinding.ActivityBleEntryBinding
import com.css.ble.ui.fragment.WeightMeasureFragment
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.router.ARouterConst
import com.pingwang.bluetoothlib.AILinkSDK
import com.pingwang.bluetoothlib.server.ELinkBleServer
import com.pingwang.bluetoothlib.utils.BleLog

@Route(path = ARouterConst.PATH_APP_BLE)
class WeightMeasureActivity : BaseActivity<WeightMeasureVM, ActivityBleEntryBinding>() {
    var mCurFragment: Fragment? = null

    private val mFhrSCon: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "服务与界面建立连接成功")
            var mBluetoothService = (service as ELinkBleServer.BluetoothBinder).service
            mViewModel.onBindService(mBluetoothService!!)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "服务与界面连接断开")
            mViewModel.onUnBindService()
        }
    }

    fun <T : Fragment> changeFragment(
        cls: Class<T>,
        addToBackStack: Boolean = true,
        option: BleEntryActivity.Option = BleEntryActivity.Option.OP_ADD
    ): T {
        val newFragmentTag = cls.simpleName
        val ft = supportFragmentManager.beginTransaction()
        if (mCurFragment != null && !mCurFragment!!.isHidden) {
            ft.hide(mCurFragment!!)
        }
        var fragment = supportFragmentManager.findFragmentByTag(newFragmentTag)
        if (fragment == null) {
            fragment = cls.newInstance()
            if (!fragment.isAdded) {
                when (option) {
                    BleEntryActivity.Option.OP_ADD -> ft.add(R.id.content, fragment, newFragmentTag)
                    BleEntryActivity.Option.OP_REPLACE -> ft.replace(R.id.content, fragment, newFragmentTag)
                }
                if (addToBackStack) ft.addToBackStack(newFragmentTag)
            }
        } else {
            ft.show(fragment)
        }
        ft.commit()
        mCurFragment = fragment
        return fragment as T
    }


    override fun enabledVisibleToolBar(): Boolean {
        return false
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        AILinkSDK.getInstance().init(this)
        BleLog.init(false)

        val bindIntent = Intent(this, ELinkBleServer::class.java)
        bindService(bindIntent, mFhrSCon, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)

        changeFragment(WeightMeasureFragment::class.java)
    }

    override fun initData() {
        super.initData()
        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled()
        mViewModel.locationOpened.value = BleUtils.isLocationEnabled(baseContext)
        mViewModel.locationPermission.value = BleUtils.isLocationAllowed(baseContext)
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(this).get(WeightMeasureVM::class.java)
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
        if (isFinishing) {
            unbindService(mFhrSCon)
            unregisterReceiver(receiver)
        }
    }

}