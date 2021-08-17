package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.AppUtils
import com.css.base.net.api.repository.HistoryRepository
import com.css.base.net.api.repository.SettingRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.WeightBondData
import com.css.service.data.UpGradeData

import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivityViewModel : BaseViewModel() {
    val upGradeData = MutableLiveData<UpGradeData>()

    fun getUpGrade() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    SettingRepository.upGrade(AppUtils.getAppVersionCode().toString())
                }
            }, { msg, data ->
                hideLoading()
                if (data != null) {
                    upGradeData.value = data
                } else {
                    showToast(msg)
                }

            }, { _, msg, _ ->
                hideLoading()
                showToast(msg)
            }
        )
    }
    //获取远端体重信息（第一次体重，上次体重）
    fun fetchRemoteWeight() {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    val uid = WonderCoreCache.getLoginInfo()!!.userInfo.userId
                    var ret = HistoryRepository.queryInitialBodyWeight(uid)
                    if (ret.isSuccess && !ret.data.isNullOrEmpty()) {
                        WonderCoreCache.saveData(CacheKey.FIRST_WEIGHT_INFO, WeightBondData(ret.data!![0]))
                    }
                    ret = HistoryRepository.queryBodyWeight(uid)
                    if (ret.isSuccess && !ret.data.isNullOrEmpty()) {
                        WonderCoreCache.saveData(CacheKey.LAST_WEIGHT_INFO, WeightBondData(ret.data!![0]))
                    }
                    ret
                }
            }, { msg, data ->

            }, { code, msg, data ->
            }
        )
    }
}