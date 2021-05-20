package com.css.wondercorefit.adapter

import android.content.Context
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.wondercorefit.R
import com.css.wondercorefit.bean.ProductBean
import com.css.wondercorefit.databinding.ItemProductLayoutBinding

class MallProductAdapter(private val mContext: Context, private val mData: List<ProductBean>) :
    BaseBindingAdapter<ProductBean, ItemProductLayoutBinding>(mData) {
    private var mItemClickListener: ((ProductBean) -> Unit)? = null

    fun setOnItemClickListener(listener: ((ProductBean) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_product_layout

    override fun onBindItem(binding: ItemProductLayoutBinding, item: ProductBean, position: Int) {
        binding.data = item
        binding.clItem.setOnClickListener {
            mItemClickListener?.invoke(item)
        }
        binding.executePendingBindings();
    }
}