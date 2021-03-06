package com.css.ble.ui

import LogUtils
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import cn.wandersnail.ble.EasyBLE
import cn.wandersnail.ble.EventObserver
import cn.wandersnail.commons.util.ToastUtils
import com.css.ble.bean.BondDeviceData
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.base.BaseDeviceVM
import com.css.service.bus.LiveDataBus

/**
 * @author yuedong
 * @date 2021-06-17
 */
class BleEnvService : LifecycleService() {
    private var mViewModels = mutableListOf<BaseDeviceVM>()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return MyBinder()
    }

    inner class MyBinder : Binder() {

        fun setViewModel(vm: BaseDeviceVM) {
            mViewModels.takeIf { !it.contains(vm) }?.add(vm)
            LogUtils.d("setViewModel:mViewModels.size：${mViewModels.size}")
        }

        fun removeViewModel(vm: BaseDeviceVM, obsvr: EventObserver) {
            mViewModels.remove(vm)
            EasyBLE.getInstance().unregisterObserver(obsvr)
        }
    }

    override fun onCreate() {
        super.onCreate()
        //广播
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(receiver, filter)

        BleEnvVM.bleObsrv.observe(this) { //蓝牙关闭，断开连接
            if (it == false) {
                ToastUtils.showShort("蓝牙断开")
                mViewModels.forEach { it2 -> it2.disconnect() }
            }
        }
        BondDeviceData.getDeviceLiveDataMerge().observe(this) { it ->//解绑时断开
            val k1 = it.first
            val v1 = it.second
            if (v1 == null) {
                val iterator = mViewModels.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (item.deviceType.cacheKey == k1) {
                        item.release()
                        iterator.remove()
                    }
                }
            }
        }
        //app退出是断开连接
        LiveDataBus.get().with<Boolean>("AppExit").observe(this) {
            LogUtils.d("AppExit:do disconnecting job")
            mViewModels.forEach { it.release() }
            mViewModels.clear()
        }
        LogUtils.d("BleDeviceService#onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        LogUtils.d("BleDeviceService#onDestroy")
    }


    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LogUtils.d("action：" + intent!!.action)
            when (intent.action) {
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