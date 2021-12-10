package com.shopwonder.jingzaoyd.bean

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


data class ProductBean(
    var productImg: Int,
    var productName: String,
) {
    companion object {
        @BindingAdapter("android:src")
        @JvmStatic
        fun setImageViewResource(imageView: AppCompatImageView, resource: Int) {
            Glide.with(imageView.context).load(resource).into(imageView)
        }
    }

}
