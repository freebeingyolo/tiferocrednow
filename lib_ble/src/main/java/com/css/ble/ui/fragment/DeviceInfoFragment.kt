package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.FragmentDeviceInfoBinding
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-17
 */
class DeviceInfoFragment : BaseFragment<DefaultViewModel, FragmentDeviceInfoBinding>() {

    fun setArguments(deviceKey: String) {
        val args = Bundle()
        args.putString("DeviceKey", deviceKey)
        this.arguments = args
    }

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): FragmentDeviceInfoBinding {

        return FragmentDeviceInfoBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): DefaultViewModel {
        return ViewModelProvider(requireActivity()).get(DefaultViewModel::class.java)
    }

    override fun enabledVisibleToolBar() = true
    private lateinit var data: BondDeviceData
    private lateinit var key: String

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        key = arguments?.getString("DeviceKey")!!
        data = WonderCoreCache.getData(key,BondDeviceData::class.java)
        mViewBinding?.apply {
            tvDeviceName.text = data.displayName
            tvMacAddress.text = data.mac
        }
        var displayName = when (key) {
            WonderCoreCache.BOND_WEIGHT_INFO -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
            else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
        }
        setToolBarLeftText(displayName)
        mViewBinding!!.rlDeleteDevice.setOnClickListener {
            WonderCoreCache.removeKey(key)
        }

        mViewBinding?.apply {
            tvDeviceName.text = data.displayName
            rlDeviceName.setOnClickListener {
                CommonAlertDialog(requireContext()).apply {
                    type = CommonAlertDialog.DialogType.Edit
                    title = "设备名称"
                    content = data.displayName
                    hint = "请输入设备名称"
                    leftBtnText = "取消"
                    rightBtnText = "确定"
                    listener = object : DialogClickListener.DefaultLisener() {

                        override fun onRightEditBtnClick(view: View, content: String?) {
                            var validContent = getInValidName(content)
                            tvDeviceName.text = validContent
                            data.alias == validContent
                            WonderCoreCache.saveData(key, data)
                        }
                    }
                }.show()
            }
            rlDeleteDevice.setOnClickListener {
                CommonAlertDialog(requireContext()).apply {
                    type = CommonAlertDialog.DialogType.Confirm
                    content = "确定要解绑吗"
                    leftBtnText = "取消"
                    rightBtnText = "解绑"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onLeftBtnClick(view: View) {
                        }

                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)

                            WonderCoreCache.removeKey(data.getCacheKey())
                            ToastUtils.showShort("解锁成功")
                        }
                    }
                }.show()
            }
        }

    }

    fun getInValidName(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return when (data.type) {
                R.mipmap.icon_weight -> ActivityUtils.getTopActivity().getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        }
        //check str
        return str
    }
}