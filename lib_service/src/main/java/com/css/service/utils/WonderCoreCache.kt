package com.css.service.utils

import com.blankj.utilcode.util.SPUtils
import com.css.service.data.UserData
import com.google.gson.Gson

class WonderCoreCache {
    companion object {
        val mGson = Gson()
        const val USER_INFO = "userinfo"

        fun saveUserInfo(userData: UserData) {
            var json = mGson.toJson(userData)
            SPUtils.getInstance().put(USER_INFO, json)
        }

        fun getUserInfo(): UserData {
            var userData: UserData = if (!SPUtils.getInstance().getString(USER_INFO).isNullOrEmpty()) {
                mGson.fromJson(
                    SPUtils.getInstance().getString(USER_INFO),
                    UserData::
                    class.java
                )
            } else {
                UserData()
            }
            return userData
        }
    }

}