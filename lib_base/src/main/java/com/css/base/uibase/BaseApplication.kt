package com.css.base.uibase

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils
import com.css.service.BuildConfig

open class BaseApplication : Application() {
    companion object {
        private lateinit var instance: BaseApplication
        fun getContext(): Application = instance
    }
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