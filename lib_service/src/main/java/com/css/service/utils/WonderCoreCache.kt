package com.css.service.utils

import com.blankj.utilcode.util.SPUtils
import com.css.service.data.UserData
import com.google.gson.Gson

enum class CacheKey(val k: String) {
    USER_INFO("user_info"),
    BOND_WEIGHT_INFO("BOND_WEIGHT_INFO"), //体脂秤
    BOND_WHEEL_INFO("BOND_WHEEL_INFO"), //健腹轮
    FIRST_WEIGHT_INFO("FIRST_WEIGHT_INFO"),//第一次体脂秤数据
    LAST_WEIGHT_INFO("LAST_WEIGHT_INFO"),//上次体脂秤数据
    STEP_DATA("stepdata");
}

class WonderCoreCache {

    companion object {

        val mGson = Gson()

        fun saveUserInfo(userData: UserData) {
            val json = mGson.toJson(userData)
            SPUtils.getInstance().put(CacheKey.USER_INFO.k, json)
        }

        fun getUserInfo(): UserData {
            return if (!SPUtils.getInstance().getString(CacheKey.USER_INFO.k).isNullOrEmpty()) {
                mGson.fromJson(SPUtils.getInstance().getString(CacheKey.USER_INFO.k), UserData::class.java)
            } else {
                UserData()
            }
        }

        fun <T> saveData(k: String, d: T) {
            val json = mGson.toJson(d)
            SPUtils.getInstance().put(k, json)
        }

        fun <T> getData(k: String, cls: Class<T>): T {
            val info = SPUtils.getInstance().getString(k)
            val t = if (info.isNullOrEmpty()) {
                val t2 = cls.newInstance()
                t2
            } else {
                mGson.fromJson(info, cls)
            }
            return t
        }

        fun containsKey(k: String) = SPUtils.getInstance().contains(k)

        //apply：异步  commit:同步
        fun removeKey(k: String, isCommit: Boolean = true) = SPUtils.getInstance().remove(k, isCommit)

        //extension
        fun removeKey(k: CacheKey, isCommit: Boolean = true) = removeKey(k.k)
        fun <T> saveData(k: CacheKey, d: T) = saveData(k.k, d)
        fun <T> getData(k: CacheKey, cls: Class<T>) = getData(k.k, cls)
        fun containsKey(k: CacheKey) = containsKey(k.k)
    }

}