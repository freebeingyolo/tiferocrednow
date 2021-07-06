package com.css.base.net

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody


/**
 * 创建：LJY
 * 时间: 2019/8/1
 */
class RequestBodyBuilder {
    private val paramsMap = hashMapOf<String, Any>()

    init {
//        paramsMap["system"] = URLConstant.SYSTEM
    }

    fun addParams(key: String, value: Any?): RequestBodyBuilder {
        if (value != null) {
            paramsMap[key] = value
        }
        return this
    }

    fun addParams(params: HashMap<String, Any>): RequestBodyBuilder {
        for (item in params) {
            paramsMap[item.key] = item.value
        }
        return this
    }


    fun build(): RequestBody {
        return Gson().toJson(paramsMap).toRequestBody("application/json; charset=utf-8".toMediaType())
    }

}