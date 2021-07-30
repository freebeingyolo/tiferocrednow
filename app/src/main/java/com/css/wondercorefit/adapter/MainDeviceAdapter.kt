package com.css.wondercorefit.adapter

import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.DeviceData
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ItemDeviceLayoutBinding

class MainDeviceAdapter(mData: List<DeviceData>) :
    BaseBindingAdapter<DeviceData, ItemDeviceLayoutBinding>(mData) {
    private var mItemClickListener: ((DeviceData) -> Unit)? = null

    fun setOnItemClickListener(listener: ((DeviceData) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_device_layout

    override fun onBindItem(binding: ItemDeviceLayoutBinding, item: DeviceData, position: Int) {
        binding.data = item
        binding.deviceWeight.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        binding.executePendingBindings()
    }
}