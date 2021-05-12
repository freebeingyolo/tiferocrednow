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
import com.css.ble.databinding.FragmentWeightBoundBinding
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightBondVM


/**
 * @author yuedong
 * @date 2021-05-12
 */
class WeightBondFragment : BaseFragment<WeightBondVM, FragmentWeightBoundBinding>() {

    companion object {
        val TAG: String? = WeightBondFragment.javaClass.simpleName
        fun newInstance() = WeightBondFragment()
        const val GPS_REQUEST_CODE = 100;
    }

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightBoundBinding {

        return FragmentWeightBoundBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewBinding?.apply { tips.setOnClickListener { tipsClick(it) } }
        mViewModel.bleEnabled.observe(viewLifecycleOwner) {
            updateBleCondition()
        }
        mViewModel.locationAllowed.observe(viewLifecycleOwner) {
            updateBleCondition()
        }
        mViewModel.locationOpened.observe(viewLifecycleOwner) {
            updateBleCondition()
        }
        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled()
        mViewModel.locationOpened.value = BleUtils.isLocationEnabled(requireContext())
        mViewModel.locationAllowed.value = BleUtils.isLocationAllowed(requireContext())
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
        if (mViewModel.mBluetoothService != null && mViewModel.isBleEnvironmentOk) {
            Log.d(TAG, "mViewModel.bleService.isScanStatus:${mViewModel.mBluetoothService!!.isScanStatus()}")
            if (!mViewModel.mBluetoothService!!.isScanStatus()) {
                mViewModel.startScanBle(10);
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.bleEnabled.value = BluetoothAdapter.getDefaultAdapter().isEnabled()
        mViewModel.locationOpened.value = BleUtils.isLocationEnabled(requireContext())
        mViewModel.locationAllowed.value = BleUtils.isLocationAllowed(requireContext())
    }

    //点击检查蓝牙环境
    private fun tipsClick(view: View?) {
        if (!mViewModel.bleEnabled.value!!) {
            BluetoothAdapter.getDefaultAdapter().enable()
            return
        }
        if (!mViewModel.locationAllowed.value!!) {
            PermissionUtils.permission(PermissionConstants.LOCATION)
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
            val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, GPS_REQUEST_CODE)
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_REQUEST_CODE -> {
                mViewModel.locationAllowed.value = BleUtils.isLocationAllowed(requireContext())
            }
        }
    }

    fun openGPSSEtting() {
        val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, GPS_REQUEST_CODE)
    }
}