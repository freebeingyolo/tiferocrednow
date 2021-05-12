package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWeightBeginBinding
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightEntryViewModel

class WeightBeginFragment : BaseFragment<WeightEntryViewModel, FragmentWeightBeginBinding>() {

    companion object {
        val TAG: String? = WeightBeginFragment.javaClass.simpleName
        fun newInstance() = WeightBeginFragment()
        const val REQUEST_LOCATION = 100;
    }

    override fun initViewModel(): WeightEntryViewModel {
        return ViewModelProvider(requireActivity()).get(WeightEntryViewModel::class.java)
    }

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightBeginBinding {
        return FragmentWeightBeginBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding!!.tips.setOnClickListener { tipsClick(it) }
        mViewBinding!!.back.setOnClickListener { onBackPressed() }

        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled
        mViewModel.locationOpened.value = BleUtils.isLocationEnabled(requireContext())
        mViewModel.locationAllowed.value = PermissionUtils.isGranted(PermissionConstants.LOCATION)

        mViewModel.bleEnabled.observe(viewLifecycleOwner, {
            updateBleCondition()
        })
        mViewModel.locationAllowed.observe(viewLifecycleOwner, {
            updateBleCondition()
        })
        mViewModel.locationOpened.observe(viewLifecycleOwner, {
            updateBleCondition()
        })
    }


    private fun updateBleCondition() {
        when {
            !mViewModel.bleEnabled.value!! -> {
                mViewBinding!!.tips.text = "蓝牙未打开"
                mViewBinding!!.tips.setTextColor(Color.RED)
            }
            !mViewModel.locationAllowed.value!! -> {
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
        if (mViewModel.mBluetoothService != null) {
            Log.d(TAG, "mViewModel.bleService.isScanStatus:${mViewModel.mBluetoothService!!.isScanStatus()}")
            if (!mViewModel.mBluetoothService!!.isScanStatus()) {
                mViewModel.startScanBle(10);
            }
        }
    }

    //点击检查蓝牙环境
    private fun tipsClick(view: View?) {
        startFragment(DeviceBondFragment.newInstance())

        if (!mViewModel.bleEnabled.value!!) {
            BluetoothAdapter.getDefaultAdapter().enable()
            return
        }
        if (!mViewModel.locationAllowed.value!!) {
            Log.d(TAG, "PermissionUtils.isGranted：" + PermissionUtils.isGranted(PermissionConstants.LOCATION))
            PermissionUtils.permission(PermissionConstants.LOCATION) //动态申请定位权限
                .rationale { _, shouldRequest ->
                    shouldRequest.again(true)
                }
                .callback(object : PermissionUtils.FullCallback {
                    override fun onGranted(granted: MutableList<String>) {
                        mViewModel.locationAllowed.value = true
                    }

                    override fun onDenied(deniedForever: MutableList<String>, denied: MutableList<String>) {
                        mViewModel.locationAllowed.value = false
                    }
                })
                .request()
            return
        }
        if (!mViewModel.locationOpened.value!!) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, REQUEST_LOCATION)
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOCATION) {
            mViewModel.locationOpened.value = BleUtils.isLocationEnabled(requireContext())
        }
    }
}