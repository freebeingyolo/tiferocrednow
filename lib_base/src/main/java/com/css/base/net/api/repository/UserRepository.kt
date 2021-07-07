package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.LoginUserData

object UserRepository {
    private val userApi: WonderCoreApi.User by lazy {
        NetManager.create(WonderCoreApi.User::class.java)
    }

    //    suspend fun login(phone: String, password: String): CommonResponse<LoginUserData> {
//        val param = RequestBodyBuilder()
//            .addParams("phone", phone)
//            .addParams("password", password)
//            .build()
//        return userApi.login(param)
//    }
    suspend fun loginGet(phone: String, password: String): CommonResponse<LoginUserData> {
        val map: MutableMap<String, String> = HashMap()
        map["phone"] = phone
        map["password"] = password
        return userApi.loginGet(map)
    }

    suspend fun register(
        phone: String,
        password: String,
        smsCode: String,
        userName: String
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("phone", phone)
            .addParams("password", password)
            .addParams("code", smsCode)
            .addParams("userName", userName)
            .build()
        return userApi.register(param)
    }

    suspend fun resetPassword(
        phone: String,
        password: String,
        smsCode: String
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("phone", phone)
            .addParams("password", password)
            .addParams("code", smsCode)
            .build()
        return userApi.resetPassword(param)
    }
}