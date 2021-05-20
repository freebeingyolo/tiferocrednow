package com.css.service.utils

import android.util.Log
import androidx.annotation.StringDef
import com.blankj.utilcode.util.SPUtils
import com.css.service.data.UserData
import com.google.gson.Gson

class WonderCoreCache {
    @StringDef(
        USER_INFO,
        BOND_WEIGHT_INFO,
        BOND_WHEEL_INFO,
        FIRST_WEIGHT_INFO,
        LAST_WEIGHT_INFO,
        STEP_DATA
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
    annotation class CacheKey


    companion object {
        const val BOND_WEIGHT_INFO = "BOND_WEIGHT_INFO" //体脂秤
        const val BOND_WHEEL_INFO = "BOND_WHEEL_INFO" //健腹轮
        const val FIRST_WEIGHT_INFO = "FIRST_WEIGHT_INFO" //
        const val LAST_WEIGHT_INFO = "LAST_WEIGHT_INFO"
        const val USER_INFO = "user_info"
        const val STEP_DATA = "stepdata"

        val mGson = Gson()

        fun saveUserInfo(userData: UserData) {
            var json = mGson.toJson(userData)
            SPUtils.getInstance().put(USER_INFO, json)
        }

        fun getUserInfo(): UserData {
            return if (!SPUtils.getInstance().getString(USER_INFO).isNullOrEmpty()) {
                mGson.fromJson(SPUtils.getInstance().getString(USER_INFO), UserData::class.java)
            } else {
                UserData()
            }
        }

        fun <T> saveData(@CacheKey k: String, d: T) {
            var json = mGson.toJson(d)
            SPUtils.getInstance().put(k, json)
        }

        fun <T> getData(@CacheKey k: String, cls: Class<T>): T {
            var info = SPUtils.getInstance().getString(k)
            var t = if (info.isNullOrEmpty()) {
                var t2 = cls.newInstance()
                t2
            } else {
                mGson.fromJson(info, cls)
            }
            return t
        }

        fun containsKey(@CacheKey k: String) = SPUtils.getInstance().contains(k)
        fun removeKey(@CacheKey k: String) = SPUtils.getInstance().remove(k)
    }


}