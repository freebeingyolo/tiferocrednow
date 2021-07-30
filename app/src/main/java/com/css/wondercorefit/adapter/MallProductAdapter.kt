package com.css.wondercorefit.adapter

import android.content.Context
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.MallData
import com.css.wondercorefit.R
import com.css.wondercorefit.bean.ProductBean
import com.css.wondercorefit.databinding.ItemProductLayoutBinding

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