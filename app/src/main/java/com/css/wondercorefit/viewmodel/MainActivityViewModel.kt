package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.AppUtils
import com.css.base.net.HttpNetCode
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
                    showCenterToast(msg)
                }

            }, { code, msg, _ ->
                hideLoading()
                if (code != HttpNetCode.NET_CONNECT_ERROR) {
                    showCenterToast(msg)
                }
            }
        )
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
}