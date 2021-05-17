package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.databinding.ActivityWeightBondBinding
import com.css.ble.databinding.LayoutFindbonddeviceBinding
import com.css.ble.databinding.LayoutSearchTimeoutBinding
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache

class WeightBondActivity : BaseActivity<WeightBondVM, ActivityWeightBondBinding>() {
    companion object {
        const val TAG: String = "WeightBondFragment"
        const val GPS_REQUEST_CODE = 100;

        fun starActivity(context: Context) {
            val intent = Intent(context, WeightBondActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("蓝牙体脂秤")
        mViewBinding?.apply {
            tips.setOnClickListener { tipsClick(it) }
        }

        checkAndRequestBleEnv()

        if (mViewModel.cachedData.mac.isNullOrEmpty()) {

        }
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        observerVM()
    }

    private fun observerVM() {
        mViewModel.bleEnabled.observe(this) {
            updateBleCondition()
        }
        mViewModel.locationPermission.observe(this) {
            updateBleCondition()
        }
        mViewModel.locationOpened.observe(this) {
            updateBleCondition()
        }

        mViewModel.bondDevice.observe(this) {
            ToastUtils.showShort("发现一台设备：" + it.mac)
            mViewBinding.content.text = it.mac + "||" + it.manifactureHex
        }

        mViewModel.bondData.observe(this) {
            ToastUtils.showShort("得到设备数据：${it.weight} ${it.adc}")
            mViewBinding!!.tips.text = it.weight.toString()
            mViewBinding!!.vgBonding.apply {
                var childViews = Array(childCount) {
                    getChildAt(it)
                }
                removeAllViews()
                var v: LayoutFindbonddeviceBinding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.layout_findbonddevice,
                    this,
                    false
                )
                v.mac.text = mViewModel.bondDevice.value!!.mac
                v.weight.text = it.weight.toString() + "-" + it.weightUnit.toString()
                v.bond.setOnClickListener {
                    var d = BondDeviceData(
                        mViewModel.bondDevice.value!!.mac,
                        mViewModel.bondDevice.value!!.manifactureHex,
                        BondDeviceData.TYPE_WEIGHT
                    )
                    WonderCoreCache.saveData(WonderCoreCache.BOND_WEIGHT_INFO, d)
                    onBackPressed()
                }
                v.research.setOnClickListener {
                    mViewBinding!!.vgBonding.removeAllViews()
                    for (v in childViews) {
                        mViewBinding!!.vgBonding.addView(v)
                    }
                    startScan()
                }
                addView(v.root)
            }

            //发现绑定设备，停止搜索
            mViewModel.stopScanBle()
        }

        mViewModel.state.observe(this) {
            when (it) {
                WeightBondVM.State.bondingTimeOut -> onScanTimeOut()
            }
        }
    }

    private fun onScanTimeOut() {
        ToastUtils.showShort("onScanTimeOut")
        mViewBinding!!.vgBonding.apply {
            var childViews = Array(childCount) {
                getChildAt(it)
            }
            removeAllViews()
            var v: LayoutSearchTimeoutBinding =
                LayoutSearchTimeoutBinding.inflate(layoutInflater, this, false)
            v.research.setOnClickListener {
                mViewBinding!!.vgBonding.removeAllViews()
                for (v in childViews) {
                    mViewBinding!!.vgBonding.addView(v)
                }
                mViewBinding!!.tips.text = ""
                mViewBinding!!.content.text = ""
                startScan()
            }
            addView(v.root)
        }
    }

    private fun updateBleCondition() {
        when {
            !mViewModel.bleEnabled.value!! -> {
                mViewBinding!!.tips.text = "蓝牙未打开"
                mViewBinding!!.tips.setTextColor(Color.RED)
            }
            !mViewModel.locationPermission.value!! -> {
                mViewBinding!!.tips.text = "定位权限未允许"
                mViewBinding!!.tips.setTextColor(Color.RED)
            }
            !mViewModel.locationOpened.value!! -> {
                mViewBinding!!.tips.text = "定位未打开"
                mViewBinding!!.tips.setTextColor(Color.RED)
            }
            else -> {
                mViewBinding!!.tips.text = "蓝牙环境ok"
                mViewBinding!!.tips.setTextColor(Color.GREEN)
            }
        }
        if (mViewModel.isBleEnvironmentOk) {
            Log.d(
                TAG,
                "mViewModel.bleService.isScanStatus:${mViewModel.mBluetoothService!!.isScanStatus()}"
            )
            startScan()
        } else {
            ToastUtils.showShort("已经停止绑定设备，请检查蓝牙环境")
            mViewModel.stopScanBle()
        }

    }

    fun startScan() {
        mViewModel.startScanBle(10 * 1000);
    }


    //点击检查蓝牙环境
    private fun tipsClick(view: View?) {
        checkAndRequestBleEnv()
    }

    private fun checkAndRequestBleEnv() {
        if (!mViewModel.bleEnabled.value!!) {
            BluetoothAdapter.getDefaultAdapter().enable()
            return
        }
        if (!mViewModel.locationPermission.value!!) {
            PermissionUtils.permission(PermissionConstants.LOCATION)
                .rationale { _, shouldRequest ->
                    shouldRequest.again(true)
                }
                .callback(object : PermissionUtils.FullCallback {
                    override fun onGranted(granted: MutableList<String>) {
                        mViewModel.locationPermission.value = true
                    }

                    override fun onDenied(
                        deniedForever: MutableList<String>,
                        denied: MutableList<String>
                    ) {
                        mViewModel.locationPermission.value = false
                    }
                })
                .request()
            return
        }
        if (!mViewModel.locationOpened.value!!) {
            val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, GPS_REQUEST_CODE)
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQUEST_CODE -> {
                mViewModel.locationOpened.value = BleUtils.isLocationEnabled(this)
            }
        }
    }

    fun openGPSSEtting() {
        val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, GPS_REQUEST_CODE)
    }

    override fun initViewModel(): WeightBondVM =
        ViewModelProvider(this).get(WeightBondVM::class.java)


    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightBondBinding {
        return ActivityWeightBondBinding.inflate(layoutInflater, parent, false)
    }
}