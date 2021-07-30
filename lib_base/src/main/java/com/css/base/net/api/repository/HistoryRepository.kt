package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.HistoryWeight

object HistoryRepository {

    private val api: WonderCoreApi.History by lazy {
        NetManager.create(WonderCoreApi.History::class.java)
    }

    //上传体重信息
    suspend fun uploadMeasureWeight(
        uid: Int,
        weight: Float,
        adc:Int
    ): CommonResponse<Any> {
        val param = RequestBodyBuilder()
            .addParams("userId", uid)
            .addParams("bodyWeight", weight)
            .addParams("adc", adc)
            .build()
        return api.appHistory(param)
    }


    //查询最近一次体重
    suspend fun queryBodyWeight(uid: Int): CommonResponse<List<HistoryWeight>> {
        val param = RequestBodyBuilder()
            .addParams("userId", uid)
            .build()
        return api.queryBodyWeight(param)
    }

    //查询初始化体重
    suspend fun queryInitialBodyWeight(uid: Int): CommonResponse<List<HistoryWeight>> {
        val param = RequestBodyBuilder()
            .addParams("userId", uid)
            .build()
        return api.queryInitialBodyWeight(param)
    }

}