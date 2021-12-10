package com.shopwonder.jingzaoyd.adapter

import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.MallData
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ItemProductLayoutBinding

class MallProductAdapter(mData: List<MallData>) :
    BaseBindingAdapter<MallData, ItemProductLayoutBinding>(mData) {
    private var mItemClickListener: ((MallData) -> Unit)? = null

    fun setOnItemClickListener(listener: ((MallData) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_product_layout

    override fun onBindItem(binding: ItemProductLayoutBinding, item: MallData, position: Int) {
        binding.data = item
        binding.clItem.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        binding.executePendingBindings()
    }
}