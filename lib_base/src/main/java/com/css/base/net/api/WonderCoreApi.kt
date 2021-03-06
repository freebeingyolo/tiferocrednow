
package com.css.base.net.api

import com.css.base.net.CommonResponse
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

        //京东登录
        @GET("auth/jdLogin")
        suspend fun jdLogin(@QueryMap map: Map<String, String>): CommonResponse<LoginUserData>

        //验证码绑定
        @GET("app/user/codeBind")
        suspend fun codeBind(@QueryMap map: Map<String, String>): CommonResponse<LoginUserData>

        //密码绑定
        @GET("app/user/pwdBind")
        suspend fun pwdBind(@QueryMap map: Map<String, String>): CommonResponse<LoginUserData>
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
        //查询反馈信息
        @GET("appFeedback/queryFeedback")
        suspend fun queryFeedbackHistory(): CommonResponse<ArrayList<FeedbackData>>
        //查询反馈信息详情
        @GET("appFeedback/queryFeedbackDetails")
        suspend fun queryFeedbackHistoryDetail(@QueryMap map: Map<String, Int>): CommonResponse<ArrayList<FeedbackData>>

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
        suspend fun queryBodyWeight(@Body requestBody: RequestBody): CommonResponse<List<HistoryWeight>>

        @POST("appHistory/queryInitialBodyWeight")
        suspend fun queryInitialBodyWeight(@Body requestBody: RequestBody): CommonResponse<List<HistoryWeight>>

    }

    interface Device {

        @GET("appDevice/queryBindDevice")
        suspend fun queryBindDevice(@QueryMap map: Map<String,String>): CommonResponse<List<DeviceData>>

        @GET("appDevice/queryDetails")
        suspend fun queryDeviceListDetails(@QueryMap map: Map<String,String>): CommonResponse<List<DeviceData>>

        @POST("appDevice/bind")
        suspend fun bindDevice(@Body requestBody: RequestBody): CommonResponse<DeviceData>

        @POST("appDevice/unbindDevice")
        suspend fun unbindDevice(@Body requestBody: RequestBody): CommonResponse<Any>

        //修改设备名字
        @POST("appDevice/updateDeviceName")
        suspend fun updateDeviceName(@Body requestBody: RequestBody): CommonResponse<Any>

        //上传单杠&健腹轮数据
        @POST("appHistory/addPushUps")
        suspend fun addPushUps(@Body requestBody: RequestBody): CommonResponse<PullUpData>

        //查询单杠&健腹轮数据
        @GET("appHistory/queryPushUps")
        suspend fun queryPushUps(@QueryMap map: Map<String,String>): CommonResponse<List<PullUpData>>
    }
    interface Course {
        //获取视频资源
        @POST("appVideo/queryVideo")
        suspend fun queryVideo(@Body requestBody: RequestBody): CommonResponse<List<CourseData>>
    }
}