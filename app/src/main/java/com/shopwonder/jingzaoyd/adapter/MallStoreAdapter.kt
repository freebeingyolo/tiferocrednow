package com.shopwonder.jingzaoyd.adapter

import android.view.View
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.MallData
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ItemProductLayoutBinding
import com.shopwonder.jingzaoyd.databinding.ItemStoreLayoutBinding

class MallStoreAdapter(mData: List<MallData>) :
    BaseBindingAdapter<MallData, ItemStoreLayoutBinding>(mData) {
    private var mItemClickListener: ((MallData) -> Unit)? = null

    fun setOnItemClickListener(listener: ((MallData) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_store_layout

    override fun onBindItem(binding: ItemStoreLayoutBinding, item: MallData, position: Int) {
        binding.data = item
        binding.tvStoreDetails.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        if (position==1){
            binding.line.visibility = View.GONE
        }else{
            binding.line.visibility = View.VISIBLE
        }
        binding.executePendingBindings()
    }
}