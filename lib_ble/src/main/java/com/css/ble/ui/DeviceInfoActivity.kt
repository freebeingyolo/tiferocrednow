package com.css.ble.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.utils.StringUtils
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.FragmentDeviceInfoBinding
import com.css.pickerview.listener.OnDismissListener
import com.css.service.utils.WonderCoreCache
import razerdp.basepopup.BasePopupWindow

/**
 * @author yuedong
 * @date 2021-05-26
 */
class DeviceInfoActivity : BaseActivity<DefaultViewModel, FragmentDeviceInfoBinding>() {

    companion object {
        fun start(deviceKey: String) {
            var intent = Intent(
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

    override fun initViewModel(): DefaultViewModel {
        return ViewModelProvider(this).get(DefaultViewModel::class.java)
    }

    override fun enabledVisibleToolBar() = true
    private lateinit var data: BondDeviceData
    private lateinit var key: String


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        key = intent.getStringExtra("DeviceKey")!!
        data = WonderCoreCache.getData(key, BondDeviceData::class.java)
        mViewBinding?.apply {
            tvDeviceName.text = data.displayName
            tvMacAddress.text = data.mac
        }
        setToolBarLeftText(data.displayName)
        mViewBinding!!.rlDeleteDevice.setOnClickListener {
            WonderCoreCache.removeKey(key)
        }

        mViewBinding?.apply {
            tvDeviceName.text = data.displayName
            rlDeviceName.setOnClickListener {
                CommonAlertDialog(baseContext).apply {
                    type = CommonAlertDialog.DialogType.Edit
                    title = "设备名称"
                    content = data.displayName
                    hint = "请输入设备名称，不可包含符号"
                    leftBtnText = "取消"
                    rightBtnText = "确定"
                    listener = object : DialogClickListener.DefaultLisener() {

                        override fun onRightEditBtnClick(view: View, content: String?) {
                            val contentLength =
                                StringUtils.getCharacterNum(content) + StringUtils.getChineseNum(
                                    content!!
                                )
                            if (contentLength > 12 || StringUtils.getCheckSymbol(content)) {
                                showToast("设备名称12个字符以内、不可包含符号，无法保存设备名称")
                            } else {
                                val validContent = getInValidName(content)
                                tvDeviceName.text = validContent
                                data.alias = validContent
                                WonderCoreCache.saveData(key, data)
                                dialog?.dismiss()
                                setToolBarLeftText(data.displayName)
                            }
                        }
                    }
                }.show()
            }
            rlDeleteDevice.setOnClickListener {
                CommonAlertDialog(baseContext).apply {
                    type = CommonAlertDialog.DialogType.Confirm
                    title = "解除绑定"
                    content = "此操作会清除手机中有关该设备的所有数据。设备解绑后，若再次使用，需重新添加。"
                    leftBtnText = "取消"
                    rightBtnText = "确认解绑"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)
                            WonderCoreCache.removeKey(data.getCacheKey())
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

                        }
                    }
                }.show()
            }
        }

    }

    fun getInValidName(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return when (data.type) {
                R.mipmap.icon_weight -> ActivityUtils.getTopActivity()
                    .getString(R.string.device_weight)
                else -> ActivityUtils.getTopActivity().getString(R.string.device_wheel)
            }
        }
        //check str
        return str
    }
}