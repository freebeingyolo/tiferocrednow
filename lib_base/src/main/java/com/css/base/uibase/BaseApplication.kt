package com.css.base.uibase

import android.app.Application
import com.blankj.utilcode.util.Utils

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initBlankj()

    }

    private fun initBlankj() {
        Utils.init(this)
    }
}