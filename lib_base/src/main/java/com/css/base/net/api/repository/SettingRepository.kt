package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.FeedbackData
import com.css.service.data.UpGradeData

/**
 * Created by YH
 * Describe 设置页面请求
 * on 2021/7/13.
 */
object SettingRepository {
    private val settingApi: WonderCoreApi.Setting by lazy {
        NetManager.create(WonderCoreApi.Setting::class.java)
    }

    suspend fun submit(
        userId: Int?,
        id: Int,
        phone: String,
        content: String
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("feedbackUserId", userId)
            .addParams("feedbackId", id)
            .addParams("phone", phone)
            .addParams("feedbackContent", content)
            .build()
        return settingApi.submit(param)
    }

    suspend fun queryFeedBackHistory(
    ): CommonResponse<ArrayList<FeedbackData>> {
        return settingApi.queryFeedbackHistory();
    }

    suspend fun queryFeedBackHistoryDetail(
        id: Int): CommonResponse<ArrayList<FeedbackData>> {
        val map: MutableMap<String, Int> = HashMap()
        map["id"] = id
        return settingApi.queryFeedbackHistoryDetail(map);
    }

    suspend fun upGrade(
        version: String
    ): CommonResponse<UpGradeData> {
        val map: MutableMap<String, String> = HashMap()
        map["version"] = version
        return settingApi.upGrade(map)
    }
}