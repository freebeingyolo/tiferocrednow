package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class CourseDate(
        var id: Int,
        var isDel: String="",
        var videoLink: String="",
        var videoPreview: String="",
        var deviceCategoryName: String="",
        var materialName: String="",
        var applicationScenes: String=""
) {
    companion object {
        @BindingAdapter("android:src")
        @JvmStatic
        fun setImageViewResource(imageView: AppCompatImageView, resource: String) {
            Glide.with(imageView.context).load(resource).into(imageView)
        }
    }
}