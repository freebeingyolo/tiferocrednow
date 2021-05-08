package com.css.wondercorefit.application

import com.css.base.uibase.BaseApplication
import com.css.wondercorefit.utils.VideoCacheHelper

class WonderApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        VideoCacheHelper.init(this)
    }
}