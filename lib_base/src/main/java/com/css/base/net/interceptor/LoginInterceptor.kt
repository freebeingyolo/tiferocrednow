package com.css.base.net.interceptor

import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.net.HttpNetCode
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import okhttp3.Interceptor
import okhttp3.Response

class LoginInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == HttpNetCode.LOGIN_EXPIRED) {
            ToastUtils.showShort("您还未登录或登录信息已过期，请登录")
            logout()
        }
        return response
    }

    private fun logout() {
        WonderCoreCache.removeKey(CacheKey.LOGIN_DATA)
        WonderCoreCache.removeKey(CacheKey.USER_INFO)
        ActivityUtils.finishAllActivities()
        ARouterUtil.openLogin()
    }
}