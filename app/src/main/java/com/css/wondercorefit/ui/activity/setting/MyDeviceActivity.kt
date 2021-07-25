package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.ble.bean.BondDeviceData
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityMyDeviceBinding
import com.css.wondercorefit.ui.viewmodel.DeviceInfoViewModel
import com.css.wondercorefit.viewmodel.MyDeviceViewModel
import razerdp.basepopup.BasePopupWindow

class MyDeviceActivity : BaseActivity<MyDeviceViewModel, ActivityMyDeviceBinding>() {
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
        initRecycle()
    }

    private fun initRecycle() {
        mViewBinding?.deviceRecycle?.layoutManager = LinearLayoutManager(this)
        val recyclerAdapter = RecycleAdapter()
        mViewBinding?.deviceRecycle?.adapter = recyclerAdapter
    }

    inner class RecycleAdapter : RecyclerView.Adapter<RecycleAdapter.DeviceHolder>() {
        private var opened = -1
        inner class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            val deviceTitle: TextView = itemView.findViewById(R.id.my_device_name)
            private val deviceImage: ImageView = itemView.findViewById(R.id.device_image)
            private val deviceLinearLayout :LinearLayout = itemView.findViewById(R.id.ln_device_info)
            private val deviceRecyclerView: RelativeLayout = itemView.findViewById(R.id.my_device_recycle)
            private val deleteDevice: RelativeLayout = itemView.findViewById(R.id.rel_device_delete)
            init {
                deviceRecyclerView.setOnClickListener(this)
                deleteDevice.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                when (v?.id) {
                    R.id.my_device_recycle -> {
                        if (opened == bindingAdapterPosition) {
                            opened = -1
                            deviceImage.setImageResource(R.mipmap.icon_next)
                            deviceLinearLayout.visibility = View.GONE
                        } else {
                            var oldOpened = opened
                            opened = bindingAdapterPosition
                            deviceImage.setImageResource(R.mipmap.icon_more)
                            deviceLinearLayout.visibility = View.VISIBLE
                        }
                    }
                    R.id.rel_device_delete -> {
                        deleteDevice(bindingAdapterPosition)
                    }
                }

            }
        }

        private fun deleteDevice(bindingAdapterPosition: Int) {
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
                        CommonAlertDialog(context).apply {
                            type = CommonAlertDialog.DialogType.Image
                            imageResources = com.css.ble.R.mipmap.icon_tick
                            content = context.getString(com.css.ble.R.string.unbond_ok)
                            onDismissListener = object : BasePopupWindow.OnDismissListener() {
                                override fun onDismiss() {
                                }
                            }
                        }.show()
                        DeviceInfoViewModel.name.remove(DeviceInfoViewModel.name[bindingAdapterPosition])
                        notifyItemRemoved(bindingAdapterPosition)

                    }
                }
            }.show()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
            return DeviceHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_view_device,
                    parent,
                    false
                )
            )
        }
        // item 数量暂时写 3
        override fun getItemCount() = DeviceInfoViewModel.name.size

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.run {
                deviceTitle.text = DeviceInfoViewModel.name[position]
            }

        }
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initViewModel(): MyDeviceViewModel = ViewModelProvider(this).get(MyDeviceViewModel::class.java)

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityMyDeviceBinding =
            ActivityMyDeviceBinding.inflate(inflater, parent, false)
}