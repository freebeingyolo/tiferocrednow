package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.CourseDate

object CourseRepository {

    private val otherApi: WonderCoreApi.Course by lazy {
        NetManager.create(WonderCoreApi.Course::class.java)
    }

    suspend fun queryVideo(): CommonResponse<ArrayList<CourseDate>> {
//        val map: MutableMap<String, Any> = HashMap()
//        map["applicationScenes"] = "教程"
//        map["deviceCategoryName"] = "单杠"
//        map["id"] = 0
        val param = RequestBodyBuilder()
                .addParams("applicationScenes", "教程")
                .addParams("deviceCategoryName", "单杠")
                .addParams("id", 0)
                .build()
        return otherApi.queryVideo(param)
    }


    suspend fun queryVideo(scene:String,deviceCategoryName:String): CommonResponse<ArrayList<CourseDate>> {
        val param = RequestBodyBuilder()
            .addParams("applicationScenes", scene)
            .addParams("deviceCategoryName", deviceCategoryName)
            .build()
        return otherApi.queryVideo(param)
    }

}