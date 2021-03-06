package com.shopwonder.jingzaoyd.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.service.data.DeviceData
import com.shopwonder.jingzaoyd.adapter.MyDeviceRecycleAdapter
import com.shopwonder.jingzaoyd.databinding.ActivityMyDeviceBinding
import com.shopwonder.jingzaoyd.viewmodel.MyDeviceViewModel
import razerdp.basepopup.BasePopupWindow

class MyDeviceActivity : BaseActivity<MyDeviceViewModel, ActivityMyDeviceBinding>() {

    var mData = ArrayList<DeviceData>()
    lateinit var mAdapter: MyDeviceRecycleAdapter

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, MyDeviceActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.WHITE
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("我的设备")
        mAdapter = MyDeviceRecycleAdapter(mData)
        mViewBinding?.deviceRecycle?.layoutManager = LinearLayoutManager(this)
        mViewBinding?.deviceRecycle?.adapter = mAdapter
        mAdapter.setOnDeleteDeviceClickListener {
            deleteDevice(it)
        }
        initRecycle()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.deviceInfo.observe(this, Observer {
            mData.addAll(it)
            mAdapter.setItems(mData)
        })
        mViewModel.unBindDevice.observe(this, Observer {
            mData.remove(it)
            mAdapter.setItems(mData)
            CommonAlertDialog(baseContext).apply {
                type = CommonAlertDialog.DialogType.Image
                imageResources = com.css.ble.R.mipmap.icon_tick
                content = context.getString(com.css.ble.R.string.unbond_ok)
                onDismissListener = object : BasePopupWindow.OnDismissListener() {
                    override fun onDismiss() {
                        finish()
                    }
                }
            }.show()
        })
    }

    private fun initRecycle() {
        mViewModel.loadDevice()
    }

    private fun deleteDevice(it: DeviceData) {
        CommonAlertDialog(baseContext).apply {
            type = CommonAlertDialog.DialogType.Confirm
            gravity = Gravity.BOTTOM
            title = "解除绑定"
            content = "此操作会清除手机中有关该设备的所有数据。设备解绑后，若再次使用，需重新添加。"
            leftBtnText = "取消"
            rightBtnText = "确认解绑"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    mViewModel.unBindDevice(it.id, it.deviceCategory,it)
                }
            }
        }.show()
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initViewModel(): MyDeviceViewModel =
        ViewModelProvider(this).get(MyDeviceViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityMyDeviceBinding =
        ActivityMyDeviceBinding.inflate(inflater, parent, false)
}