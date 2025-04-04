package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.FragmentDeviceListBinding
import com.css.ble.databinding.LayoutDeviceItemBinding
import com.css.ble.viewmodel.DeviceListVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.service.router.ARouterConst
import razerdp.basepopup.BasePopupWindow

/**
 * @author yuedong
 * @date 2021-05-26
 */
@Route(path = ARouterConst.PATH_APP_BLE_DEVICELIST)
class DeviceListActivity : BaseActivity<DeviceListVM, FragmentDeviceListBinding>() {
    lateinit var mAdapter: RecycleViewAdapter

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentDeviceListBinding {
        return FragmentDeviceListBinding.inflate(layoutInflater, parent, false)
    }

    override fun initViewModel(): DeviceListVM {
        return ViewModelProvider(this).get(DeviceListVM::class.java)
    }

    override fun enabledVisibleToolBar(): Boolean = true
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(R.string.bond_device)
        mViewBinding.lv.apply {
            mAdapter = RecycleViewAdapter()
            mAdapter.itemClickListener = object : RecycleViewAdapter.onItemClickListener {
                override fun onItemClick(
                    holder: RecycleViewAdapter.MyViewHolder,
                    position: Int,
                    deviceInfo: DeviceListVM.DeviceInfo
                ) {
                    val d = deviceInfo.getBondDeviceData()
                    if (d == null) {
                        when (deviceInfo.deviceType) {
                            DeviceType.WEIGHT -> ARouter.getInstance()
                                .build(ARouterConst.PATH_APP_BLE_WEIGHTBOND).navigation()
                            DeviceType.WHEEL -> ARouter.getInstance()
                                .build(ARouterConst.PATH_APP_BLE_WHEELBOND).navigation()
                            else -> {
                                ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_COMMON)
                                    .withInt("mode", BaseDeviceScan2ConnVM.WorkMode.BOND.ordinal)
                                    .withInt("deviceType", deviceInfo.deviceType.ordinal)
                                    .navigation()
                            }
                        }
                    } else {
                        CommonAlertDialog(context).apply {
                            type = CommonAlertDialog.DialogType.Confirm
                            title = "解除绑定"
                            content = "此操作会清除手机中有关该设备的所有数据。设备解绑后，若再次使用，需重新添加。"
                            leftBtnText = "取消"
                            rightBtnText = "确认解绑"
                            listener = object : DialogClickListener.DefaultLisener() {
                                override fun onRightBtnClick(view: View) {
                                    super.onRightBtnClick(view)
                                    mViewModel.unBindDevice(d,
                                        { _, _ ->
                                            CommonAlertDialog(context).apply {
                                                type = CommonAlertDialog.DialogType.Image
                                                imageResources = R.mipmap.icon_tick
                                                content = context.getString(R.string.unbond_ok)
                                                onDismissListener =
                                                    object : BasePopupWindow.OnDismissListener() {
                                                        override fun onDismiss() {
                                                            ARouter.getInstance()
                                                                .build(ARouterConst.PATH_APP_MAIN)
                                                                .navigation()
                                                        }
                                                    }
                                            }.show()
                                        }, { _, msg, _ ->
                                            showCenterToast(msg)
                                        })

                                }
                            }
                        }.show()
                    }
                }
            }
            layoutManager = GridLayoutManager(context, 2)
            adapter = mAdapter
            //addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.deviceInfos.observe(this, {
            //Log.d(TAG, "mViewModel._deviceInfos：$it")
            mViewBinding.tip.text = "已经为您选择" + it.size + "款运动设备。请打开设备进行连接。"
            mAdapter.mList = it
            mAdapter.notifyDataSetChanged()
        })
        BondDeviceData.getDeviceLiveDataMerge().observe(this) { pair ->
            if (mViewBinding.lv.isComputingLayout) return@observe
            mAdapter.mList?.let { list ->
                list.indexOfFirst { it.deviceType.cacheKey == pair.first }
                    .takeIf { it != -1 }
                    ?.let {
                        mAdapter.notifyItemChanged(it)
                    }
            }
        }
    }

    override fun initData() {
        super.initData()
        mViewModel.loadDeviceInfo(null, { _, msg, _ ->
            run {
                showCenterToast(msg) { finish() }
            }
        })
    }

    class RecycleViewAdapter : RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>() {
        var mList: List<DeviceListVM.DeviceInfo>? = null
        var itemClickListener: onItemClickListener? = null

        class MyViewHolder(itemView: View, val binding: LayoutDeviceItemBinding) :
            RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val binding = LayoutDeviceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MyViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            mList?.let {
                val binding = holder.binding
                binding.name.text = it[position].name
                binding.state.text = it[position].getBondDeviceData()?.deviceConnect ?: "点击绑定"
                binding.icon.setImageResource(it[position].icon)
                binding.container.setOnClickListener {
                    itemClickListener?.onItemClick(holder, position, mList!![position])
                }
                binding.masked.visibility = if (it[position].getBondDeviceData() == null) View.VISIBLE else View.GONE
                binding.masked2.visibility = if (it[position].getBondDeviceData() != null) View.VISIBLE else View.GONE
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

        interface onItemClickListener {
            fun onItemClick(
                holder: MyViewHolder,
                position: Int,
                deviceInfo: DeviceListVM.DeviceInfo
            )
        }
    }
}