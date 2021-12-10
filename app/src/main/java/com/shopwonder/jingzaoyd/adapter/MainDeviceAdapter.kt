package com.shopwonder.jingzaoyd.adapter

import com.css.ble.bean.BondDeviceData
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.DeviceData
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ItemDeviceLayoutBinding

class MainDeviceAdapter(mData: List<BondDeviceData>) :
    BaseBindingAdapter<BondDeviceData, ItemDeviceLayoutBinding>(mData) {
    private var mItemClickListener: ((BondDeviceData) -> Unit)? = null

    fun setOnItemClickListener(listener: ((BondDeviceData) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_device_layout

    override fun onBindItem(binding: ItemDeviceLayoutBinding, item: BondDeviceData, position: Int) {
        binding.data = item
        binding.deviceWeight.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        binding.executePendingBindings()
    }
}