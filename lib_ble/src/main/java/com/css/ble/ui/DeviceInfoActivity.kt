package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.databinding.ActivityDeviceInfoBinding

class DeviceInfoActivity : BaseActivity<DefaultViewModel, ActivityDeviceInfoBinding>(),
    OnToolBarClickListener, View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding.rlDeviceName.setOnClickListener(this)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityDeviceInfoBinding =
        ActivityDeviceInfoBinding.inflate(layoutInflater,parent,false)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rl_device_name -> {
                CommonAlertDialog(this).apply {
                    type = CommonAlertDialog.DialogType.Edit
                    title = "设备名称"
                    hint = "请输入设备名称"
                    leftBtnText = "取消"
                    rightBtnText = "确定"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onLeftBtnClick(view: View) {

                        }

                        override fun onRightEditBtnClick(view: View, content: String?) {
                            mViewBinding.tvDeviceName.text = content
                        }

                    }
                }.show()
            }
        }
    }

}