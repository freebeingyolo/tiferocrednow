package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import androidx.databinding.BindingAdapter

data class MallData(
    var id: Int,
    var isDel: String="",
    var mallLink: String="",
    var mallPreview: String="",
    var position: Int,
    var titleCopywriting: String="",
    var useScenes: String=""
){
    companion object {
        @BindingAdapter("android:src")
        @JvmStatic
        fun setImageViewResource(imageView: AppCompatImageView, resource: String) {
            Glide.with(imageView.context).load(resource).into(imageView)
        }
    }
}