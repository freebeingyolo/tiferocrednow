package com.shopwonder.jingzaoyd.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonInformationViewModel : BaseViewModel() {
    val upPersonInfoData = MutableLiveData<String>()

    fun getPersonInfo() {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    UserRepository.queryPersonalInformation(
                        WonderCoreCache.getLoginInfo()?.userId.toString()
                    )
                }
            }, { _, d ->
                hideLoading()
                if (d != null && d.size > 0) {
                    WonderCoreCache.saveUserInfo(d[0])
                }
            }, { _, msg, _ ->
                hideLoading()
                showCenterToast(msg)
            }
        )
    }

    fun upDataPersonInfo(
        sex: String = "",
        age: String = "",
        height: String = "",
        goalBodyWeight: String = "",
        goalStepCount: String = ""
    ) {
        netLaunch(
            {
                showLoading()
                UserRepository.updatePersonalInformation(
                    WonderCoreCache.getLoginInfo()!!.userId.toString(), sex, age, height, goalBodyWeight, goalStepCount
                )
            }, { msg, _ ->
                upPersonInfoData.value = msg
                hideLoading()
            }, { _, msg, _ ->
                hideLoading()
                showCenterToast(msg)
            }
        )
    }
}