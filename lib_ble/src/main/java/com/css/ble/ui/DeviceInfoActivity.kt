package com.css.ble.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.utils.StringUtils
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.FragmentDeviceInfoBinding
import com.css.ble.viewmodel.DeviceInfoVM
import razerdp.basepopup.BasePopupWindow

/**
 * @author yuedong
 * @date 2021-05-26
 */
class DeviceInfoActivity : BaseActivity<DeviceInfoVM, FragmentDeviceInfoBinding>() {

    companion object {
        fun start(deviceKey: String) {
            val intent = Intent(
                ActivityUtils.getTopActivity(),
                DeviceInfoActivity::class.java
            ).apply { putExtra("DeviceKey", deviceKey) }
            ActivityUtils.startActivity(intent)
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentDeviceInfoBinding {
        return FragmentDeviceInfoBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): DeviceInfoVM {
        return ViewModelProvider(this).get(DeviceInfoVM::class.java)
    }

    override fun enabledVisibleToolBar() = true
    private lateinit var data: BondDeviceData
    private lateinit var key: String


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        key = intent.getStringExtra("DeviceKey")!!
        data = BondDeviceData.getDevice(DeviceType.valueOf(key))!!
        mViewBinding.apply {
            tvDeviceName.text = data.displayName
            tvMacAddress.text = data.mac
        }
        setToolBarLeftText(data.displayName)
        mViewBinding.rlDeleteDevice.setOnClickListener {
            BondDeviceData.setDevice(data.deviceType, null)
        }

        mViewBinding.apply {
            tvDeviceName.text = data.displayName
            rlDeviceName.setOnClickListener {
                CommonAlertDialog(baseContext).apply {
                    type = CommonAlertDialog.DialogType.Edit
                    title = "设备名称"
                    content = data.displayName
                    hint = "请输入设备名称，只可为汉字或英文字母"
                    leftBtnText = "取消"
                    rightBtnText = "确定"
                    listener = object : DialogClickListener.DefaultLisener() {

                        override fun onRightEditBtnClick(view: View, content: String?) {
    //                            val contentLength =
    //                                StringUtils.getCharacterNum(content) + StringUtils.getChineseNum(
    //                                    content!!
    //                                )
                            if (StringUtils.getCheckSymbol(content.toString())) {
                                showToast("设备名称只可为10个汉字或英文字母，包含其他字符将无法保存")
                            } else {
                                val validContent = if (content.isNullOrEmpty()) {
                                    data.displayName
                                } else {
                                    content
                                }
                                data.alias = validContent
                                mViewModel.updateDeviceName(data,
                                    { _, _ ->
                                        dialog?.dismiss()
                                        tvDeviceName.text = validContent
                                        setToolBarLeftText(data.displayName)
                                    },
                                    { _, msg, _ ->
                                        showToast(msg)
                                    })

                            }
                        }
                    }
                }.show()
            }
            rlDeleteDevice.setOnClickListener {
                if (BondDeviceData.getDevice(data.deviceType) == null) {
                    ToastUtils.showLong("设备已经解绑")
                    return@setOnClickListener
                }
                CommonAlertDialog(baseContext).apply {
                    type = CommonAlertDialog.DialogType.Confirm
                    title = "解除绑定"
                    content = "此操作会清除手机中有关该设备的所有数据。设备解绑后，若再次使用，需重新添加。"
                    leftBtnText = "取消"
                    rightBtnText = "确认解绑"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)
                            mViewModel.unBindDevice(BondDeviceData.getDevice(data.deviceType)!!,
                                { _, _ ->
                                    data.alias = null
                                    CommonAlertDialog(context).apply {
                                        type = CommonAlertDialog.DialogType.Image
                                        imageResources = R.mipmap.icon_tick
                                        content = context.getString(R.string.unbond_ok)
                                        onDismissListener = object : BasePopupWindow.OnDismissListener() {
                                            override fun onDismiss() {
                                                finish()
                                            }
                                        }
                                    }.show()
                                }, { _, _, _ ->

                                })
                        }
                    }
                }.show()
            }
        }

    }
}