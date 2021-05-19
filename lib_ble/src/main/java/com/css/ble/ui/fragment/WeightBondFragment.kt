package com.css.ble.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.databinding.FragmentWeightBondBinding
import com.css.ble.ui.WeightBondActivity
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.data.BondDeviceData
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightBondFragment : BaseFragment<WeightBondVM, FragmentWeightBondBinding>() {
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    companion object {
        fun newInstance() = WeightBondFragment()
        private val TAG: String = "WeightBond#WeightBondFragment"
    }

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): FragmentWeightBondBinding {
        return FragmentWeightBondBinding.inflate(inflater, parent, false)
    }


    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        observerVM()
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("蓝牙体脂秤")
        mViewBinding?.apply {
            tips.setOnClickListener { tipsClick(it) }
            var clickLis: (v: View) -> Unit = {
                mViewModel.state.value = WeightBondVM.State.bondbegin
            }
            research.setOnClickListener(clickLis)
            research2.setOnClickListener(clickLis)
            bond.setOnClickListener {
                var d = BondDeviceData(
                    mViewModel.bondDevice.value!!.mac,
                    mViewModel.bondDevice.value!!.manifactureHex,
                    BondDeviceData.TYPE_WEIGHT
                )
                WonderCoreCache.saveData(WonderCoreCache.BOND_WEIGHT_INFO, d)
                mViewModel.state.value = WeightBondVM.State.bonded
            }
            //返回主页
            back.setOnClickListener {
                onBackPressed()
                //requireActivity().finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mViewModel.state.value = WeightBondVM.State.bondbegin
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
        }

        mViewModel.bondData.observe(this) {
            Log.d(TAG, "得到设备数据-->" + it.toString())
            ToastUtils.showShort("得到设备数据：${it.weight} ${it.adc}")
            //发现绑定设备，停止搜索,并且得到体重
            mViewModel.state.value = WeightBondVM.State.found
            mViewModel.stopScanBle()
        }

        mViewModel.state.observe(this) {
            //清除3s返回页面任务
            mHandler.removeCallbacksAndMessages(null)
            when (it) {
                WeightBondVM.State.bondbegin -> {
                    showFragment(mViewBinding!!.bondbeginRoot)
                    checkAndRequestBleEnv {
                        startScan()
                    }
                }
                WeightBondVM.State.bondingTimeOut -> {
                    showFragment(mViewBinding!!.timeoutRoot)
                }
                WeightBondVM.State.found -> {
                    showFragment(mViewBinding!!.foundRoot)
                    var it = mViewModel.bondDevice.value!!
                    mViewBinding!!.fondDevice.text = it.mac + "||" + it.manifactureHex
                }

                WeightBondVM.State.bonded -> {
                    showFragment(mViewBinding!!.bondedRoot)
                    startCountDonwBack()
                }
            }
        }
    }

    //开启3s返回页面任务
    private fun startCountDonwBack() {
        var seconds = 3
        mViewBinding!!.countdownTv.text = Html.fromHtml(getString(R.string.count_down_tips, seconds))
        var r = object : Runnable {
            override fun run() {
                seconds--
                Log.d(TAG, "seconds:$seconds")
                mViewBinding!!.countdownTv.text = Html.fromHtml(getString(R.string.count_down_tips, seconds))
                if (seconds > 0) {
                    mHandler.postDelayed(this, 1000)
                } else {
                    mViewBinding!!.back.performClick()
                }
            }
        }
        mHandler.postDelayed(r, 1000)
    }

    private fun showFragment(view: View) {
        for (i in 0..mViewBinding!!.root.childCount) {
            var child = mViewBinding!!.root.getChildAt(i)
            if (child is ViewGroup) {
                child.visibility = if (child == view) View.VISIBLE else View.GONE
            }
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
            startScan()
        } else {
            ToastUtils.showShort("已经停止绑定设备，请检查蓝牙环境")
            mViewModel.stopScanBle()
        }
    }

    fun startScan() {
        if (!mViewModel.mBluetoothService!!.isScanStatus) {
            mViewModel.startScanBle(3 * 1000);
            mViewBinding!!.content.text = "开始搜索"
        }
    }


    //点击检查蓝牙环境
    private fun tipsClick(view: View?) {
        checkAndRequestBleEnv {
            startScan()
        }
    }

    private fun checkAndRequestBleEnv(onBleEnvOk: () -> Unit) {
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
            startActivityForResult(intent, WeightBondActivity.GPS_REQUEST_CODE)
            return
        }
        //环境OK
        onBleEnvOk()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            WeightBondActivity.GPS_REQUEST_CODE -> {
                mViewModel.locationOpened.value = BleUtils.isLocationEnabled(requireContext())
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mHandler.removeCallbacksAndMessages(null)
    }

}