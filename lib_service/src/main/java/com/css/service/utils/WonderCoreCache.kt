package com.css.service.utils

import androidx.annotation.StringDef
import com.blankj.utilcode.util.SPUtils
import com.css.service.data.BaseData
import com.css.service.data.UserData
import com.google.gson.Gson

class WonderCoreCache(Bond_INFO: String) {
    @StringDef(
        USER_INFO,
        BOND_WHEEL_INFO,
        BOND_WHEEL_INFO
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class CacheKey


    companion object {
        const val BOND_WEIGHT_INFO = "BOND_WEIGHT_INFO"
        const val BOND_WHEEL_INFO = "BOND_WHEEL_INFO"
        const val USER_INFO = "userinfo"


        val mGson = Gson()

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

        fun <T> saveData(@CacheKey k: String, d: T) {
            var json = mGson.toJson(d)
            SPUtils.getInstance().put(k, json)
        }

        fun <T> getData(@CacheKey k: String, cls: Class<T>): T {
            var info = SPUtils.getInstance().getString(k)
            var t = if (info.isNullOrEmpty()) {
                var t2 = cls.newInstance()
                saveData(k, t2)
                t2
            } else {
                mGson.fromJson(info, cls)
            }
            return t
        }
    }



}