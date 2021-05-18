package com.css.ble

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.css.ble.bean.WeightDetailsBean
import com.css.ble.databinding.ItemWeightDetailsBinding
import com.css.ble.ui.view.BaseBindingAdapter

class BodyDetailAdapter(private val mContext: Context, private val mData: List<WeightDetailsBean>) :
    BaseBindingAdapter<WeightDetailsBean, ItemWeightDetailsBinding>(mData) {
    override fun getLayoutResId(viewType: Int): Int = R.layout.item_weight_details

    override fun onBindItem(
        binding: ItemWeightDetailsBinding,
        item: WeightDetailsBean,
        position: Int
    ) {
        binding.data = item
        binding.executePendingBindings();
    }

}