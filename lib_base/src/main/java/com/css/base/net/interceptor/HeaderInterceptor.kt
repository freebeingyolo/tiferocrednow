package com.css.base.net.interceptor

import okhttp3.Interceptor
import okhttp3.Response


class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request.newBuilder()
        newRequestBuilder.header("contentType", "application/json")
        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }

}