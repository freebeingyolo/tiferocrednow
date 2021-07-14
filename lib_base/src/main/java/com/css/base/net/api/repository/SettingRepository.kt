package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.FeedbackData

/**
 * Created by YH
 * Describe 设置页面请求
 * on 2021/7/13.
 */
object SettingRepository {
    private val settingApi: WonderCoreApi.Setting by lazy {
        NetManager.create(WonderCoreApi.Setting::class.java)
    }

    //    "feedbackContent": "string",
//    "feedbackDate": "string",
//    "feedbackTime": "string",
//    "id": 0,
//    "isDel": "string",
//    "replyAccountId": 0,
//    "userId": 0
    suspend fun submit(
        Content: String,
        Date: String,
        Time: String,
        phone: String,
        userId: String
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("feedbackContent", Content)
            .addParams("feedbackDate", Date)
            .addParams("feedbackTime", Time)
            .addParams("phone", phone)
            .addParams("userId", userId)
            .build()
        return settingApi.submit(param)
    }

    suspend fun queryFeedBackHistory(
        userId: String
    ): CommonResponse<ArrayList<FeedbackData>> {
        val map: MutableMap<String, String> = HashMap()
        map["userId"] = userId
        return settingApi.queryFeedbackHistory(map);
    }
}