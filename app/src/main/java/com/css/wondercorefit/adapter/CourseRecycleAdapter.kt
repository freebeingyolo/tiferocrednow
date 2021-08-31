package com.css.wondercorefit.adapter

import com.css.ble.ui.view.BaseBindingAdapter
import com.css.service.data.CourseData
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ItemViewVideoBinding

class CourseRecycleAdapter(mData: List<CourseData>): BaseBindingAdapter<CourseData, ItemViewVideoBinding>(mData) {

    private var mItemClickListener: ((CourseData) -> Unit)? = null
    fun setOnItemClickListener(listener: ((CourseData) -> Unit)?) {
        mItemClickListener = listener
    }

    override fun getLayoutResId(viewType: Int): Int = R.layout.item_view_video

    override fun onBindItem(binding: ItemViewVideoBinding, item: CourseData, position: Int) {
        binding.courseData = item
        binding.clCourse.setOnClickListener {
            mItemClickListener?.invoke(item)
        }

        binding.executePendingBindings()
    }
}