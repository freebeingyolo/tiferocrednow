package com.css.wondercorefit.viewmodel

import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.wondercorefit.R

object CourseViewModel : BaseViewModel() {
    const val url1 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-3bb45be8-cf20-43c4-9c68-56ebf826dfa3.mp4"
    const val url2 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-35f404be-2214-4886-b9df-bb8d71f672ac.mp4"
    const val url3 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-bbb28e5c-843f-4b66-855c-57b75dc249ea.mp4"// Rtmp resource
    const val url4 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-9893c0dc-3ab1-44c0-bfb3-c78d8a8f54a1.mp4"// Assets resource
    const val url5 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-6fd0b7b3-5857-4816-a7ee-950446075740.mp4"
    const val url6 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-b690ae6b-55ab-4cea-85dd-d0d4438fcffa.mp4"
    const val url7 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-f7d156bb-c583-4f38-9650-a003bb9965f9.mp4"
    const val url8 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-c2ba6c0a-7080-4f0e-9de2-1f787fcf694d.mp4"
    const val url9 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-4d13645a-5b1e-49c7-a599-ce9fb35dae20.mp4"
    const val url10 = "https://csqncdn.maxleap.cn/NWY2MmY1YzNhNTc5ZjEwMDAxZjIyMzA5/qn-973d2279-bc95-424f-a7b3-fc2fe393eb0a.mp4"
//    const val url11 = R.raw.raw// Raw resource

    const val str1 = "单杠训练"
    const val str2 = "弹力绳训练"
    const val str3 = "俯卧撑板训练"
    const val str4 = "杠铃组合训练"
    const val str5 = "健腹轮训练"
    const val str6 = "脚蹬拉力器训练"
    const val str7 = "拳击训练"
    const val str8 = "拳击沙包训练"
    const val str9 = "计数跳绳训练"
    const val str10 = "竞速跳绳训练"

    val urls = arrayListOf(url1, url2, url3, url4, url5, url6, url7, url8, url9, url10)
    val name = arrayListOf(str1, str2, str3, str4, str5, str6, str7, str8, str9, str10)
    val picture: IntArray = intArrayOf(R.mipmap.course_item1, R.mipmap.course_item2, R.mipmap.course_item3,
        R.mipmap.course_item4, R.mipmap.course_item5, R.mipmap.course_item6, R.mipmap.course_item7, R.mipmap.course_item8,
        R.mipmap.course_item9, R.mipmap.course_item10)
}