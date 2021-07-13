package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.LoginUserData
import com.css.service.data.MallData

object MallRepository {
    private val otherApi: WonderCoreApi.Mall by lazy {
        NetManager.create(WonderCoreApi.Mall::class.java)
    }

    suspend fun queryMall(): CommonResponse<List<MallData>> {
        return otherApi.queryMall()
    }
}