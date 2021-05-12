package com.css.wondercorefit.application

import com.css.base.uibase.BaseApplication
import com.css.wondercorefit.utils.VideoCacheHelper
import kotlin.properties.Delegates

class WonderApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        VideoCacheHelper.init(this)
        instance = this
    }

    companion object {
        var instance: WonderApplication by Delegates.notNull()
        fun instance() = instance
    }

}