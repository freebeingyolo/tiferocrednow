package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter

data class MainDeviceData(
    var deviceName: String,
    var connect: String = "未连接",
    var deviceImg: Int
) {
    companion object {
        @BindingAdapter("android:src")
        @JvmStatic
        fun setImageViewResource(imageView: AppCompatImageView, resource: Int) {
//            Glide.with(imageView.context).load(resource).into(imageView)
            imageView.setImageResource(resource);
        }
    }
}
