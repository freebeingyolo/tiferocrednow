package com.css.ble

import android.content.Context
import com.css.ble.bean.WeightDetailBean
import com.css.ble.databinding.ItemWeightDetailsBinding
import com.css.ble.ui.view.BaseBindingAdapter

class BodyDetailAdapter(private val mContext: Context, private val mData: List<WeightDetailBean>) :
    BaseBindingAdapter<WeightDetailBean, ItemWeightDetailsBinding>(mData) {
    override fun getLayoutResId(viewType: Int): Int = R.layout.item_weight_details

    override fun onBindItem(
        binding: ItemWeightDetailsBinding,
        item: WeightDetailBean,
        position: Int
    ) {
        binding.data = item
        binding.executePendingBindings()
    }

}