package com.css.base.uibase

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils
import com.css.service.BuildConfig

open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configArouter()
        initBlankj()
    }

    private fun initBlankj() {
        Utils.init(this)
    }

    private fun configArouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }
}