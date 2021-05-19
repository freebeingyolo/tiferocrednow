package com.css.ble.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.ui.BleEntryActivity
import com.css.ble.ui.WeightBondActivity
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.utils.ImageUtils

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureFragment : BaseFragment<WeightMeasureVM, ActivityWeightMeasureBeginBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureBeginBinding {
        return ActivityWeightMeasureBeginBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        observerVM()
    }

    fun observerVM() {
        mViewModel.bleEnabled.observe(this) {
            updateBleCondition()
        }
        mViewModel.locationPermission.observe(this) {
            updateBleCondition()
        }
        mViewModel.locationOpened.observe(this) {
            updateBleCondition()
        }
        mViewModel.bleSvcLiveData.observe(this) {
            checkAndRequestBleEnv {
                startScan()
            }
        }
        mViewModel.state.observe(this) {
            when (it) {
                WeightMeasureVM.State.begin ->{

                }
                WeightMeasureVM.State.doing ->{

                }
                WeightMeasureVM.State.done ->{

                }
                WeightMeasureVM.State.timeout -> {

                }
            }
        }
    }

    private fun updateBleCondition() {
        when {
            !mViewModel.bleEnabled.value!! -> {
            }
            !mViewModel.locationPermission.value!! -> {
            }
            !mViewModel.locationOpened.value!! -> {
            }
            else -> {
            }
        }
        if (mViewModel.isBleEnvironmentOk) {
            mViewModel.startScanBle()
        } else {
            ToastUtils.showShort("已经停止绑定设备，请检查蓝牙环境")
            mViewModel.stopScanBle()
        }
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftText(getString(R.string.ble_weight_name))
        var view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> ActivityUtils.getActivityByContext(context).onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        var fragment = (requireActivity() as BleEntryActivity).changeFragment(DeviceInfoFragment::class.java)
                    }
                }
            }
        })
        mViewBinding?.tvToMeasure?.setOnClickListener {
            checkAndRequestBleEnv {
                startScan()
            }
        }
    }

    fun startScan() {
        mViewModel.startScanBle(10 * 1000)
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
}