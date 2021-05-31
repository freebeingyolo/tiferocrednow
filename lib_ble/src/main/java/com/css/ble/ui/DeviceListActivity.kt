package com.css.ble.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.databinding.FragmentDeviceListBinding
import com.css.ble.databinding.LayoutDeviceItemBinding
import com.css.ble.ui.view.SpaceItemDecoration
import com.css.ble.viewmodel.DeviceListVM
import com.css.service.router.ARouterConst
import com.css.service.utils.WonderCoreCache
import kotlin.concurrent.thread

/**
 * @author yuedong
 * @date 2021-05-26
 */
@Route(path = ARouterConst.PATH_APP_BLE_DEVICELIST)
class DeviceListActivity : BaseActivity<DeviceListVM, FragmentDeviceListBinding>() {
    lateinit var mAdapter: RecycleViewAdapter

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentDeviceListBinding {
        return FragmentDeviceListBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): DeviceListVM {
        return ViewModelProvider(this).get(DeviceListVM::class.java)
    }

    override fun enabledVisibleToolBar(): Boolean = true
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(R.string.bond_device)
        mViewBinding.lv!!.apply {
            mAdapter = RecycleViewAdapter()
            mAdapter.itemClickListener = object : RecycleViewAdapter.onItemClickListener {
                override fun onItemClick(
                    holder: RecycleViewAdapter.MyViewHolder,
                    position: Int,
                    deviceInfo: DeviceListVM.DeviceInfo
                ) {
                    if (deviceInfo.icon == R.mipmap.icon_abroller) {
                        ToastUtils.showShort(context.getString(R.string.developping))
                        return
                    }
                    var d = deviceInfo.getBondDeviceData()
                    if (d == null) {
                        ARouter.getInstance().build(ARouterConst.PATH_APP_BLE_WEIGHTBOND)
                            .navigation()
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
                                    WonderCoreCache.removeKey(d.getCacheKey())
                                    CommonAlertDialog(context).apply {
                                        type = CommonAlertDialog.DialogType.Image
                                        imageResources = R.mipmap.icon_tick
                                        content = "解绑成功"
                                    }.show()
                                    mViewModel._deviceInfos.value = mViewModel._deviceInfos.value
                                }
                            }
                        }.show()
                    }
                }
            }
            addItemDecoration(SpaceItemDecoration(30))
            layoutManager = LinearLayoutManager(context)
            //(layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.VERTICAL
            adapter = mAdapter
            //addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel._deviceInfos.observe(this, {
            Log.d(TAG, "mViewModel._deviceInfos：$it")
            mAdapter.mList = it
            mAdapter.notifyDataSetChanged()
        })
    }

    override fun initData() {
        super.initData()
        thread(true) {
            var deviceInfos = mutableListOf<DeviceListVM.DeviceInfo>()
            deviceInfos.add(
                DeviceListVM.DeviceInfo(
                    getString(R.string.device_weight),
                    R.mipmap.icon_weight
                )
            )
            deviceInfos.add(
                DeviceListVM.DeviceInfo(
                    getString(R.string.device_wheel),
                    R.mipmap.icon_abroller
                )
            )
            mViewModel._deviceInfos.postValue(deviceInfos)
        }
    }

    class RecycleViewAdapter : RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>() {
        var mList: List<DeviceListVM.DeviceInfo>? = null
        var itemClickListener: onItemClickListener? = null

        class MyViewHolder(itemView: View, val binding: LayoutDeviceItemBinding) :
            RecyclerView.ViewHolder(itemView) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var binding = LayoutDeviceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MyViewHolder(binding.root, binding)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            mList?.let {
                var binding = holder.binding;
                binding.name.text = it[position].name
                binding.icon.setImageResource(it[position].icon)
                binding.container.setOnClickListener {
                    itemClickListener?.onItemClick(holder, position, mList!![position])
                }
                binding.masked.visibility =
                    if (it[position].getBondDeviceData() == null) View.VISIBLE else View.GONE
                binding.masked2.visibility =
                    if (it[position].getBondDeviceData() != null) View.VISIBLE else View.GONE
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