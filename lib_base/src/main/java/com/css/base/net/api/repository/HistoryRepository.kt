package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi

object HistoryRepository {

    private val api: WonderCoreApi.History by lazy {
        NetManager.create(WonderCoreApi.History::class.java)
    }

    //上传体重信息
    suspend fun uploadMeasureWeight(
        uid: Int,
        weight: Float,
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("userId", uid)
            .addParams("bodyWeight", weight.toString())
            .build()
        return api.appHistory(param)
    }


}