package com.css.wondercorefit.viewmodel

import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.wondercorefit.R

object CourseViewModel : BaseViewModel() {
    const val url1 = "http://vfx.mtime.cn/Video/2020/08/24/mp4/200824095210276119.mp4"
    const val url2 = "http://vfx.mtime.cn/Video/2021/05/05/mp4/210505224544809192.mp4"
    const val url3 = "http://vfx.mtime.cn/Video/2019/03/14/mp4/190314224955123234.mp4"// Rtmp resource
    const val url4 = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4"// Assets resource
    const val url5 = "https://media.w3.org/2010/05/sintel/trailer.mp4"
    const val url6 = "http://vfx.mtime.cn/Video/2021/05/06/mp4/210506133534996171.mp4"
    const val url7 = "http://vfx.mtime.cn/Video/2021/05/06/mp4/210506144345992109.mp4"
    const val url8 = "http://vfx.mtime.cn/Video/2021/01/07/mp4/210107172407759182.mp4"
    const val url9 = "http://vfx.mtime.cn/Video/2021/05/06/mp4/210506141757460192.mp4"
    const val url10 = "http://vfx.mtime.cn/Video/2021/04/14/mp4/210414080153287160.mp4"
//    const val url11 = R.raw.raw// Raw resource

    const val str1 = "热身运动"
    const val str2 = "腹部卷曲运动"
    const val str3 = "拉伸运动"
    const val str4 = "背部拉伸运动"
    const val str5 = "腿部放松运动"
    const val str6 = "腰部运动"
    const val str7 = "颈部放松运动"
    const val str8 = "关节舒缓运动"
    const val str9 = "全身运动"
    const val str10 = "胸部拓展运动"

    val urls = arrayListOf(url1, url2, url3, url4, url5, url6, url7, url8, url9, url10)
    val name = arrayListOf(str1, str2, str3, str4, str5, str6, str7, str8, str9, str10)
    val picture: IntArray = intArrayOf(R.mipmap.item1, R.mipmap.item2, R.mipmap.item3,
        R.mipmap.item4, R.mipmap.item5, R.mipmap.item6, R.mipmap.item7, R.mipmap.item8,
        R.mipmap.item9, R.mipmap.item10)
}