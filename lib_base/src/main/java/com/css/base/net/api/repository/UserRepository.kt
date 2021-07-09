package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.LoginUserData
import com.css.service.data.UserData

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

    suspend fun queryPersonalInformation(
        userId: String
    ): CommonResponse<List<UserData>> {
        val map: MutableMap<String, String> = HashMap()
        map["userId"] = userId
        return userApi.queryPersonalInformation(map)
    }

    suspend fun updatePersonalInformation(
        userId: String,
        sex: String,
        age: String,
        height: String,
        goalBodyWeight: String,
        goalStepCount: String
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("userId", userId)
        if (sex.isNotEmpty()) {
            param.addParams("sex", sex)
        }
        if (age.isNotEmpty()) {
            param.addParams("age", age)
        }
        if (height.isNotEmpty()) {
            param.addParams("height", height)
        }
        if (goalBodyWeight.isNotEmpty()) {
            param.addParams("goalBodyWeight", goalBodyWeight)
        }
        if (goalStepCount.isNotEmpty()) {
            param.addParams("goalStepCount", goalStepCount)
        }
        return userApi.updatePersonalInformation(param.build())
    }
}