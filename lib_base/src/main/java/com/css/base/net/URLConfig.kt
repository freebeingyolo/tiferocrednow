package com.css.base.net

import com.css.service.BuildConfig

object URLConfig {
    private const val HOST_DEBUG = "http://192.168.65.156:8081/wondercore/" //测试环境
    private const val HOST_RELEASE = "http://192.168.65.156:8081/wondercore/" //测试环境
    //private const val HOST_DEBUG = "http://192.168.65.52:8081/wondercore/" //测试环境
    private var REQUEST_URL_HOST = arrayOfNulls<String>(2)

    init {
        REQUEST_URL_HOST[0] = HOST_DEBUG
        REQUEST_URL_HOST[1] = HOST_RELEASE
    }

    val HOST_WEB = REQUEST_URL_HOST[BuildConfig.REQUEST_URL_TYPE]
}