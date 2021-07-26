
package com.css.base.net.api

import com.css.base.net.CommonResponse
import com.css.service.data.DeviceData
import com.css.service.data.*
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
        @GET("app/user/code")
        suspend fun code(@QueryMap map: Map<String, String>): CommonResponse<String>

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

        //个人信息查询
        @GET("appSetUp/queryPersonalInformation")
        suspend fun queryPersonalInformation(@QueryMap map: Map<String, String>): CommonResponse<ArrayList<UserData>>

        //编辑个人信息
        @POST("appSetUp/updatePersonalInformation")
        suspend fun updatePersonalInformation(@Body requestBody: RequestBody): CommonResponse<Any>
    }

    interface Setting {
        //推送设置查询
        @POST("appSetUp/queryPushSet")
        suspend fun queryPushSet(@Body requestBody: RequestBody): CommonResponse<Any>

        //检查更新
        @GET("appSetUp/upgrade")
        suspend fun upGrade(@QueryMap map: Map<String, String>): CommonResponse<UpGradeData>

        //提交意见和反馈
        @POST(" appFeedback/addFeedback")
        suspend fun submit(@Body requestBody: RequestBody): CommonResponse<Any>
        //查询反馈记录

    }

    interface Mall {
        //查询商城数据
        @GET("appMall/queryMall")
        suspend fun queryMall(): CommonResponse<ArrayList<MallData>>
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

    interface Device {

        @GET("appDevice/queryBindDevice")
        suspend fun queryBindDevice(@QueryMap map: Map<String,String>): CommonResponse<List<DeviceData>>

        @POST("appDevice/bind")
        suspend fun bindDevice(@Body requestBody: RequestBody): CommonResponse<Any>

        @POST("appDevice/unbindDevice")
        suspend fun unbindDevice(@Body requestBody: RequestBody): CommonResponse<Any>

        //修改设备名字
        @POST("appDevice/updateDeviceName")
        suspend fun updateDeviceName(@Body requestBody: RequestBody): CommonResponse<Any>


    }
    interface Course {
        //获取视频资源
        @POST("appVideo/queryVideo")
        suspend fun queryVideo(@Body requestBody: RequestBody): CommonResponse<ArrayList<CourseDate>>
    }
}