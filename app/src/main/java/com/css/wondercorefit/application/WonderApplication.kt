package com.css.wondercorefit.application

import androidx.multidex.MultiDex
import com.css.base.net.NetLongLogger
import com.css.base.net.NetManager
import com.css.base.net.URLConfig
import com.css.base.net.interceptor.HeaderInterceptor
import com.css.base.net.interceptor.LoginInterceptor
import com.css.base.uibase.BaseApplication
import com.css.wondercorefit.utils.SharedPreferencesUtils
import com.css.wondercorefit.utils.VideoCacheHelper
import com.tencent.bugly.Bugly
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.properties.Delegates

class WonderApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        SharedPreferencesUtils.init(this)
        VideoCacheHelper.init(this)
        configNet()
    }

    private fun configNet() {
        val httpLoggingInterceptor = HttpLoggingInterceptor(NetLongLogger())
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        NetManager.Builer(URLConfig.HOST_WEB!!, 15)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(HeaderInterceptor())
            .addInterceptor(LoginInterceptor())
            .build()
    }
}