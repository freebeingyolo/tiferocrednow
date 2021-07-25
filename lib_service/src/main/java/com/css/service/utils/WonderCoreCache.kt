package com.css.service.utils

import android.os.Looper
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.SPUtils
import com.css.service.bus.LiveDataBus
import com.css.service.data.LoginUserData
import com.css.service.data.UserData
import com.google.gson.Gson

enum class CacheKey(val k: String) {
    USER_INFO("user_info"),
    BOND_WEIGHT_INFO("BOND_WEIGHT_INFO"), //体脂秤
    BOND_WHEEL_INFO("BOND_WHEEL_INFO"), //健腹轮
    FIRST_WEIGHT_INFO("FIRST_WEIGHT_INFO"),//第一次体脂秤数据
    LAST_WEIGHT_INFO("LAST_WEIGHT_INFO"),//上次体脂秤数据
    LOGIN_DATA("LOGIN_USER_DATA"),//上次体脂秤数据
    STEP_DATA("stepdata")
}

class WonderCoreCache { //一切围绕CacheKey

    companion object {

        val mGson = Gson()

        fun containsKey(k: CacheKey) = SPUtils.getInstance().contains(k.k)

        //apply：异步  commit:同步
        fun removeKey(k: CacheKey, isCommit: Boolean = true) {
            LiveDataBus.get().with2(k.k).value = null
            SPUtils.getInstance().remove(k.k, isCommit)
        }

        fun <T> saveData(k1: CacheKey, d: T) {
            val k = k1.k
            val json = mGson.toJson(d)
            SPUtils.getInstance().put(k, json)
            LiveDataBus.get().with<T>(k).value = d
        }

        fun <T> getData(k1: CacheKey, cls: Class<T>): T? {
            val k = k1.k
            val ret = LiveDataBus.get().with<T>(k).value
            if (ret != null) return ret
            val info = SPUtils.getInstance().getString(k)
            val t = if (info.isNullOrEmpty()) {
                null
            } else {
                mGson.fromJson(info, cls).also {
                    LiveDataBus.get().with<T>(k).value = it
                }
            }
            return t
        }

        fun <T> getLiveData(key: CacheKey): LiveData<T> {
            return LiveDataBus.get().with(key.k)
        }
        fun getLiveData2(key: CacheKey): LiveData<Any> {
            return LiveDataBus.get().with2(key.k)
        }

        /**User**/
        fun saveUserInfo(userData: UserData) {
            saveData(CacheKey.USER_INFO, userData)
        }

        fun getUserInfo(): UserData {
            var ret = getData(CacheKey.USER_INFO, UserData::class.java)
            return ret ?: UserData()
        }


        fun getLoginInfo(): LoginUserData? {
            return getData(CacheKey.LOGIN_DATA, LoginUserData::class.java)
        }

        fun saveLoginInfo(d: LoginUserData?) {
            saveData(CacheKey.LOGIN_DATA, d)
        }

        /**User**/

        /**Device**/


        /**Device**/
    }

}