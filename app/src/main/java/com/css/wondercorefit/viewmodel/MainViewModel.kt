package com.css.wondercorefit.viewmodel

import android.view.View
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.css.base.net.HttpNetCode
import com.css.base.net.api.repository.HistoryRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.WeightBondData
import com.css.ble.viewmodel.DeviceListVM
import com.css.service.data.UserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel : BaseViewModel() {
    //初始体重
    val initWeightTxt = Transformations.map(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.FIRST_WEIGHT_INFO)) {
        it?.weightKgFmt("%.1f") ?: "--"
    }.also {(it as MutableLiveData).value = "--" }
    val targetWeightTxt = Transformations.map(WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO)) {
        it?.goalBodyWeight ?: "--"
    }
    val lastWeightTxt = Transformations.map(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
        it?.weightKgFmt("%.1f") ?: "--"
    }.also {(it as MutableLiveData).value = "--" }
    val lostWeightPercent = MediatorLiveData<Int>().apply {
        addSource(WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO)) {
            value = getSubtractedWeightPer()
        }
        addSource(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
            value = getSubtractedWeightPer()
        }
    }
    val bmiTxt = Transformations.map(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
        it?.run { "BMI${bodyFatData.bmi}" } ?: "BMI --"
    }
    val bmiJudgeTxt = Transformations.map(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
        it?.run { bodyFatData.bmiJudge } ?: "--"
    }

    val bmiVisibility = Transformations.map(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
        it?.let { View.VISIBLE } ?: View.GONE
    }

    //已减去体重
    val lostWeightTxt = MediatorLiveData<String>().apply {
        addSource(WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO)) {
            value = getSubtractedWeight()
        }
        addSource(WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO)) {
            value = getSubtractedWeight()
        }
    }
    val recentDevice by lazy {
        WonderCoreCache.getLiveData<BondDeviceData>(CacheKey.RECENT_DEVICE).apply {
            val d = WonderCoreCache.getData(CacheKey.RECENT_DEVICE, BondDeviceData::class.java)
            (this as MutableLiveData).value = d
        }
    }

    //加载设备
    private fun loadDevice() {
        DeviceListVM().loadDeviceInfo(
            { msg, data ->
                //deviceData.value = data
            },
            { code, msg, d ->
                if (code != HttpNetCode.NET_CONNECT_ERROR) {
                    showCenterToast(msg)
                }
            })
    }

    //获取远端体重信息（第一次体重，上次体重）
    fun fetchRemoteWeight() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val uid = WonderCoreCache.getLoginInfo()!!.userId
                    val ret = HistoryRepository.queryInitialBodyWeight(uid)
                    if (ret.isSuccess && !ret.data.isNullOrEmpty()) {
                        val ret2 = HistoryRepository.queryBodyWeight(uid)
                        if (ret2.isSuccess && !ret2.data.isNullOrEmpty()) {
                            WonderCoreCache.saveData(CacheKey.FIRST_WEIGHT_INFO, WeightBondData(ret.data!![0]))
                            WonderCoreCache.saveData(CacheKey.LAST_WEIGHT_INFO, WeightBondData(ret2.data!![0]))
                        }
                    }
                    ret
                }
            }, { msg, data ->

            }, { code, msg, data ->
            }
        )
    }

    private fun getPersonInfo() {
        PersonInformationViewModel().getPersonInfo()
    }

    // 已减去体重
    private fun getSubtractedWeight(): String {
        val initialWeight = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.FIRST_WEIGHT_INFO).value?.weightKg
        val currentWeight = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO).value?.weightKg
        if (null != initialWeight && null != currentWeight) {
            return String.format("%.1f", initialWeight - currentWeight)
        }
        return "--"
    }

    // 已减去体重百分比
    private fun getSubtractedWeightPer(): Int {
        val initialWeight = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.FIRST_WEIGHT_INFO).value?.weightKg
        val currentWeight = WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO).value?.weightKg
        val goalWeight = WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO).value?.targetWeightFloat
        if (null != initialWeight && null != currentWeight && null != goalWeight) {
            val sub = initialWeight - currentWeight
            val goal = initialWeight - goalWeight
            if (0f >= sub || 0f >= goal) {
                return 0
            } else if (sub >= goal) {
                return 100
            }
            return (sub / goal * 100).toInt()
        }
        return 0
    }

    val isLoadedData get() = initWeightTxt.value != null
    fun loadData(force: Boolean = true) {
        loadDevice()
        fetchRemoteWeight()
        getPersonInfo()
    }

    val userData = WonderCoreCache.getLiveData<UserData>(CacheKey.USER_INFO)
}