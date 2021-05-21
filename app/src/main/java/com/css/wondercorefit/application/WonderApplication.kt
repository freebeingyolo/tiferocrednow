package com.css.wondercorefit.application

import androidx.multidex.MultiDex
import com.css.base.uibase.BaseApplication
import com.css.wondercorefit.utils.VideoCacheHelper
import com.tencent.bugly.Bugly
import kotlin.properties.Delegates

class WonderApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        VideoCacheHelper.init(this)
    }
}