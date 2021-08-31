package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.CourseData

object CourseRepository {

    private val otherApi: WonderCoreApi.Course by lazy {
        NetManager.create(WonderCoreApi.Course::class.java)
    }

    suspend fun queryVideo(): CommonResponse<List<CourseData>> {
        val param = RequestBodyBuilder()
            .addParams("applicationScenes", "教程")
            .build()
        return otherApi.queryVideo(param)
    }

    //健腹轮中的玩法推荐：scene:玩法推荐,deviceCategoryName:健腹轮
    suspend fun queryVideo(scene: String, deviceCategoryName: String): CommonResponse<List<CourseData>> {
        val param = RequestBodyBuilder()
            .addParams("applicationScenes", scene)
            .addParams("deviceCategoryName", deviceCategoryName)
            .build()
        return otherApi.queryVideo(param)
    }

}