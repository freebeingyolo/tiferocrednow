package com.css.ble.ui.view

import android.annotation.SuppressLint
import com.css.base.utils.DateTimeHelper
import com.css.ble.R
import com.css.ble.databinding.ItemStatisticsBinding
import com.css.service.data.PullUpData

/**
 * Created by YH
 * Describe 数据统计-历史数据
 * on 2021/8/3.
 */
class StatisticsAdapter(mData: List<PullUpData>) :
    BaseBindingAdapter<PullUpData, ItemStatisticsBinding>(mData) {
//    private var mItemClickListener: ((MallData) -> Unit)? = null

//    fun setOnItemClickListener(listener: ((MallData) -> Unit)?) {
//        mItemClickListener = listener
//    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_statistics

    override fun onBindItem(binding: ItemStatisticsBinding, item: PullUpData, position: Int) {
        binding.tvDate.text = DateTimeHelper.formatToString(item.todayDate,"MM月dd日")
        binding.tvNumber.text = item.exerciseNumber.toString() + "次"
//        val cal = item.burnCalories *1000
        binding.tvCalories.text = item.burnCalories.toString() + "kcal"
//        binding.data = item
//        binding.clItem.setOnClickListener {
//            mItemClickListener?.invoke(item)
//        }
        binding.executePendingBindings()
    }
}