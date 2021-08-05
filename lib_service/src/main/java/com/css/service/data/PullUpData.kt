package com.css.service.data

import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class PullUpData(
    //消耗卡里路
    val burnCalories: Float,
    //设备类型：健腹轮、单杠、俯卧撑版
    val deviceType: String,
    //运动次数
    val exerciseNumber: Int,
    //运动消耗时间
    val exerciseTime: Int,
    val id: Int,
    val isDel: Any,
    //运动日期
    val todayDate: String,
    val userId: Int


)
//{
//    companion object {
//        @BindingAdapter("android:src")
//        @JvmStatic
//        fun setImageViewResource(imageView: AppCompatImageView, resource: String) {
//            Glide.with(imageView.context).load(resource).into(imageView)
//        }
//
//        fun formatDate(){
//
//        }
//    }
//}