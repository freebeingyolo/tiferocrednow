package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter

data class DeviceData(
    val id: Int,
    val bluetoothAddress: String,
    val deviceCategory: String,
    val deviceName: String,
    val isBind: String,
    val isDel: String,
    val mcuVersion: Any,
    val moduleType: String,
    val moduleVersion: String,
    val productType: Any,
    val status: Any,
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