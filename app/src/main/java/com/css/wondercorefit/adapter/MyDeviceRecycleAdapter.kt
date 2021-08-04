package com.css.wondercorefit.adapter

import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.DeviceData
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ItemViewDeviceBinding

class MyDeviceRecycleAdapter(mData: List<DeviceData>): BaseBindingAdapter<DeviceData, ItemViewDeviceBinding>(mData) {
    private var mItemClickListener: ((DeviceData) -> Unit)? = null
    private var mDeleteDeviceClickListener: ((DeviceData) -> Unit)? = null
    private var mItemView: ItemViewDeviceBinding? = null
    private var itemPosition: Int = 0

    fun setOnItemClickListener(listener: ((DeviceData) -> Unit)?) {
        mItemClickListener = listener
    }

    fun setOnDeleteDeviceClickListener(listener: ((DeviceData) -> Unit)?) {
        mDeleteDeviceClickListener = listener
    }

    fun getDeviceInfo (): ItemViewDeviceBinding? {
        return mItemView
    }

    fun getPosition (): Int? {
        return itemPosition
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_view_device

    override fun onBindItem(binding: ItemViewDeviceBinding, item: DeviceData, position: Int) {
        binding.deviceData = item
        binding.myDeviceRecycle.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        binding.relDeviceDelete.setOnClickListener {
            mDeleteDeviceClickListener?.invoke(item)
        }
        mItemView = binding
        itemPosition = position
        binding.executePendingBindings()
    }
}