package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.css.res.R

data class CourseData(
    var id: Int,
    var isDel: String = "",
    var videoLink: String = "",
    var videoPreview: String = "",
    var deviceCategoryName: String = "",
    var materialName: String = "",
    var applicationScenes: String = ""
) {
    companion object {
        @BindingAdapter("android:src", "android:error")
        @JvmStatic
        fun setImageViewResource(imageView: AppCompatImageView, url: String, errorDrawable: Int) {
            Glide.with(imageView.context)
                .load(url)
                .error(errorDrawable)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(6)))
                .into(imageView)
        }
    }
}