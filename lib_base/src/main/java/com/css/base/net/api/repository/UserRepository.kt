package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi

object UserRepository {
    private val userApi: WonderCoreApi.User by lazy {
        NetManager.create(WonderCoreApi.User::class.java)
    }

    suspend fun login(phone: Int, password: Int): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("phone", phone)
            .addParams("password", password)
            .build()
        return userApi.login(param)
    }
}