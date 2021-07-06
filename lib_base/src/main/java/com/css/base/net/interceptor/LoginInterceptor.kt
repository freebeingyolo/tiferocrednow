package com.css.base.net.interceptor

import com.blankj.utilcode.util.ToastUtils
import com.css.base.net.HttpNetCode
import okhttp3.Interceptor
import okhttp3.Response

class LoginInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == HttpNetCode.LOGIN_EXPIRED) {
            ToastUtils.showShort("您还未登录或登录信息已过期，请登录")
//            logout()
        }
        return response
    }
}