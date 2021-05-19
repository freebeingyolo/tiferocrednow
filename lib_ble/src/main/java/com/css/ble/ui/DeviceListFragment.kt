package com.css.ble.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.databinding.FragmentDeviceListBinding
import com.css.ble.databinding.LayoutDeviceItemBinding
import com.css.ble.ui.fragment.WeightBondFragment
import com.css.ble.ui.fragment.WeightMeasureFragment
import com.css.ble.ui.view.SpaceItemDecoration
import com.css.ble.viewmodel.DeviceListVM.DeviceInfo
import com.css.ble.viewmodel.DeviceListVM
import com.css.service.data.BondDeviceData
import com.css.service.router.ARouterConst
import com.css.service.utils.WonderCoreCache
import kotlin.concurrent.thread

class DeviceListFragment : BaseFragment<DeviceListVM, FragmentDeviceListBinding>() {
    companion object {
        fun newInstance() = DeviceListFragment()
        private val TAG: String = "DeviceBondFragment"
    }

    lateinit var mAdapter: RecycleViewAdapter

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentDeviceListBinding {
        return FragmentDeviceListBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): DeviceListVM {
        return ViewModelProvider(requireActivity()).get(DeviceListVM::class.java)
    }

    override fun enabledVisibleToolBar(): Boolean = true
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("绑定设备")
        mViewBinding?.lv!!.apply {
            mAdapter = RecycleViewAdapter()
            mAdapter.itemClickListener = object : RecycleViewAdapter.onItemClickListener {
                override fun onItemClick(

                    holder: RecycleViewAdapter.MyViewHolder,
                    position: Int,
                    deviceInfo: DeviceInfo
                ) {
                    var d = WonderCoreCache.getData(
                        WonderCoreCache.BOND_WEIGHT_INFO,
                        BondDeviceData::class.java
                    )
                    if (d.mac.isNullOrEmpty()) {
                        //activity?.let { WeightBondActivity.starActivity(it) }
                        (requireActivity() as BleEntryActivity).changeFragment(WeightBondFragment::class.java)
                    } else {
                        requireActivity().finish()
                        startActivity(Intent(requireContext(), WeightMeasureActivity::class.java))
                    }
                }
            }
            addItemDecoration(SpaceItemDecoration(30))
            layoutManager = LinearLayoutManager(requireContext())
            //(layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.VERTICAL
            adapter = mAdapter
            //addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel._deviceInfos.observe(viewLifecycleOwner, {
            Log.d(TAG, "mViewModel._deviceInfos：$it")
            mAdapter.mList = it
            mAdapter.notifyDataSetChanged()
        })
    }

    override fun initData() {
        super.initData()
        thread(true) {
            var deviceInfos = mutableListOf<DeviceInfo>()
            deviceInfos.add(DeviceInfo(getString(R.string.device_weight), R.mipmap.icon_weight))
            deviceInfos.add(DeviceInfo(getString(R.string.device_wheel), R.mipmap.icon_abroller))
            mViewModel._deviceInfos.postValue(deviceInfos)
        }
    }

    class RecycleViewAdapter : RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder>() {
        var mList: List<DeviceInfo>? = null
        var itemClickListener: onItemClickListener? = null

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            var view = LayoutDeviceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            var binding: LayoutDeviceItemBinding = DataBindingUtil.getBinding(holder.itemView)!!
            mList?.let {
                binding.name.text = it[position].name
                binding.icon.setImageResource(it[position].icon)
                binding.container.setOnClickListener {
                    itemClickListener?.onItemClick(holder, position, mList!![position])
                }
            }
        }

        override fun getItemCount(): Int {
            return mList?.size ?: 0
        }

        interface onItemClickListener {
            fun onItemClick(holder: MyViewHolder, position: Int, deviceInfo: DeviceInfo)
        }
    }
}
