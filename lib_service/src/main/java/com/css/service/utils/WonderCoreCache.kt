package com.css.service.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.blankj.utilcode.util.SPUtils
import com.css.service.bus.LiveDataBus
import com.css.service.bus.LiveDataBus.BusMediatorLiveData
import com.css.service.data.GlobalData
import com.css.service.data.LoginUserData
import com.css.service.data.UserData
import com.google.gson.Gson
import com.tencent.bugly.proguard.r

enum class CacheKey(val k: String) {
    USER_INFO("user_info"),
    GLOBAL_DATA("GLOBAL_DATA"),
    BOND_WEIGHT_INFO("BOND_WEIGHT_INFO"), //体脂秤
    BOND_WHEEL_INFO("BOND_WHEEL_INFO"), //健腹轮
    BOND_HORIZONTALBAR_INFO("BOND_HORIZONTALBAR_INFO"), //单杠
    BOND_PUSHUP_INFO("BOND_PUSHUP_INFO"), //俯卧撑板
    BOND_COUNTER_INFO("BOND_COUNTER_INFO"), //计数器
    BOND_ROPE_INFO("BOND_ROPESKIPPER_INFO"), //跳绳器
    FIRST_WEIGHT_INFO("FIRST_WEIGHT_INFO"),//第一次体脂秤数据
    LAST_WEIGHT_INFO("LAST_WEIGHT_INFO"),//上次体脂秤数据
    LOGIN_DATA("LOGIN_USER_DATA"),//上次体脂秤数据
    STEP_DATA("stepdata")
}

class WonderCoreCache { //一切围绕CacheKey
    companion object {
        val deviceCacheKeys = CacheKey.values().filter { it.k.startsWith("BOND") }.toTypedArray()

        val mGson = Gson()

        fun containsKey(k: CacheKey) = SPUtils.getInstance().contains(k.k)

        //apply：异步  commit:同步
        fun removeKey(k: CacheKey, isCommit: Boolean = true) {
            LiveDataBus.get().with2(k.k).value = null
            SPUtils.getInstance().remove(k.k, isCommit)
        }

        //如果d为null，将会移除这个key
        fun <T> saveData(k1: CacheKey, d: T?, serialized: Boolean = true) {
            if (d == null) {
                removeKey(k1, true)
            } else {
                val k = k1.k
                val json = mGson.toJson(d)
                LiveDataBus.get().with<T>(k).value = d
                if (serialized) SPUtils.getInstance().put(k, json)
            }
        }

        //获取同类型的CacheKey
        fun <T> getDatas(cls: Class<T>, vararg k1: CacheKey): List<T> {
            val ret = mutableListOf<T>()
            for (k in k1) {
                getData(k, cls)?.let { ret.add(it) }
            }
            return ret
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

        fun <From, To> getLiveData(transformer: (k: CacheKey, t: From?) -> To?, key: CacheKey): LiveData<To> {
            val fromLv = LiveDataBus.get().with<From>(key.k)
            return Transformations.map(fromLv) { transformer(key, it) }
        }

        //多个LiveData的合并,适用于监听多个LiveData
        fun <From, To> getLiveDataMerge(transformer: (k: CacheKey, t: From?) -> To?, vararg keys: CacheKey): LiveData<To> {
            /*
            val kk = StringBuilder().run {
                keys.forEach { this.append(it.k).append("|") }
                toString()
            }
            val contains = LiveDataBus.get().contains(kk)
            val liveDataMerge = LiveDataBus.get().withMediaLiveData<To>(kk)
            */
            val contains = false
            val liveDataMerge = BusMediatorLiveData<To>()
            if (!contains) {
                for (k in keys) {
                    liveDataMerge.addSource(getLiveData<From>(k)) { v ->
                        liveDataMerge.value = transformer(k, v)
                    }
                }
            }
            return liveDataMerge
        }

        fun <T> getLiveDataMerge(vararg keys: CacheKey): LiveData<T> {
            return getLiveDataMerge<T, T>({ _, t -> t }, *keys)
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
        /**Global**/

        /**User**/
        fun saveGlobalData(globalData: GlobalData) {
            saveData(CacheKey.GLOBAL_DATA, globalData)
        }

        fun getGlobalData(): GlobalData {
            var ret = getData(CacheKey.GLOBAL_DATA, GlobalData::class.java)
            return ret ?: GlobalData()
        }

        /**Global**/
        /**Device**/


        /**Device**/
    }

}