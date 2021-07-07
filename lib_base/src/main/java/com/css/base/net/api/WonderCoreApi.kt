package com.css.base.net.api

import com.css.base.net.CommonResponse
import com.css.service.data.LoginUserData
import okhttp3.RequestBody
import retrofit2.http.*

internal interface WonderCoreApi {
    interface User {
        //登录
        @POST("app/user/login")
        suspend fun login(@Body requestBody: RequestBody): CommonResponse<LoginUserData>
        //登录
        @GET("app/user/login")
        suspend fun loginGet(@QueryMap map: Map<String, String>): CommonResponse<LoginUserData>
        //注册
        @POST("app/user/register")
        suspend fun register(@Body requestBody: RequestBody): CommonResponse<Any>

        //发送验证码
        @POST("app/user/code")
        suspend fun code(@Body requestBody: RequestBody): CommonResponse<Any>

        //发送验证码
        @POST("app/user/bind/{userId}")
        suspend fun bind(
            @Body requestBody: RequestBody,
            @Path("userId") userId: String
        ): CommonResponse<Any>

        //密码重置
        @POST("app/user/reset")
        suspend fun resetPassword(@Body requestBody: RequestBody): CommonResponse<Any>

        //版本更新
        @POST("app/user/upgrade")
        suspend fun upgrade(@Body requestBody: RequestBody): CommonResponse<Any>
    }

    interface Setting {
        //个人信息查询
        @POST("appSetUp/queryPersonalInformation")
        suspend fun queryPersonalInformation(@Body requestBody: RequestBody): CommonResponse<Any>

        //推送设置查询
        @POST("appSetUp/queryPushSet")
        suspend fun queryPushSet(@Body requestBody: RequestBody): CommonResponse<Any>

        //编辑个人信息
        @POST("appSetUp/updatePersonalInformation")
        suspend fun updatePersonalInformation(@Body requestBody: RequestBody): CommonResponse<Any>
    }

    interface Mall {
        //查询商城数据
        @POST("appMall/queryMall")
        suspend fun queryMall(@Body requestBody: RequestBody): CommonResponse<Any>
    }

    interface History {
        //写入体重数据
        @POST("appHistory/addBodyWeight")
        suspend fun appHistory(@Body requestBody: RequestBody): CommonResponse<Any>

        //单杠/俯卧撑板新增
        @POST("appHistory/addPushUps")
        suspend fun addPushUps(@Body requestBody: RequestBody): CommonResponse<Any>

        //查询体重数据
        @POST("appHistory/queryBodyWeight")
        suspend fun queryBodyWeight(@Body requestBody: RequestBody): CommonResponse<Any>

        //单杠/俯卧撑板查询
        @POST("appHistory/queryPushUps")
        suspend fun queryPushUps(@Body requestBody: RequestBody): CommonResponse<Any>
    }
}