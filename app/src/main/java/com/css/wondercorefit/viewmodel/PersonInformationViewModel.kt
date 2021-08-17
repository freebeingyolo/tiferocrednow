package com.css.wondercorefit.viewmodel

import androidx.lifecycle.MutableLiveData
import com.css.base.dialog.CommonAlertDialog
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.R
import com.css.service.data.UserData
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import razerdp.basepopup.BasePopupWindow

class PersonInformationViewModel : BaseViewModel() {
    val personInfoData = MutableLiveData<ArrayList<UserData>>()
    val nonePersonInfoData = MutableLiveData<String>()
    val upPersonInfoData = MutableLiveData<String>()

    fun getPersonInfo() {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    UserRepository.queryPersonalInformation(
                        WonderCoreCache.getLoginInfo()?.userInfo?.userId.toString()
                    )
                }
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
                    WonderCoreCache.getLoginInfo()!!.userInfo.userId.toString(), sex, age, height, goalBodyWeight, goalStepCount
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