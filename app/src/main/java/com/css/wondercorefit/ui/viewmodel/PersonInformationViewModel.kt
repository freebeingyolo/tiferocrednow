package com.css.wondercorefit.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.LoginUserData
import com.css.service.data.UserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

class PersonInformationViewModel : BaseViewModel() {
    val personInfoData = MutableLiveData<ArrayList<UserData>>()
    val nonePersonInfoData = MutableLiveData<String>()
    val upPersonInfoData = MutableLiveData<String>()

    fun getPersonInfo() {
        netLaunch(
            {
                showLoading()
                UserRepository.queryPersonalInformation(
                    WonderCoreCache.getData(
                        CacheKey.LOGIN_DATA,
                        LoginUserData::class.java
                    ).userId.toString()
                )
            }, { _, d ->
                hideLoading()
                personInfoData.value = d
            }, { _, msg, _ ->
                hideLoading()
                nonePersonInfoData.value = ""
                showToast(msg)
            }
        )
    }

    fun upDataPersonInfo(
        sex: String,
        age: String,
        height: String,
        goalBodyWeight: String,
        goalStepCount: String
    ) {
        netLaunch(
            {
                showLoading()
                UserRepository.updatePersonalInformation(
                    WonderCoreCache.getData(
                        CacheKey.LOGIN_DATA,
                        LoginUserData::class.java
                    ).userId.toString(), sex, age, height, goalBodyWeight, goalStepCount
                )
            }, { msg, _ ->
                upPersonInfoData.value = msg
                hideLoading()
            }, { _, msg, _ ->
                hideLoading()
                showToast(msg)
            }
        )
    }
}