package com.css.login.model

import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.utils.LiveDataBus
import com.css.service.data.LoginUserData
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache

class LoginViewModel : BaseViewModel() {
    //val loginData = MutableLiveData<LoginUserData>()

    fun login(
        phone: String,
        password: String
    ) {
        netLaunch(
            {
                showLoading()
                UserRepository.loginGet(phone, password)
            }, { msg, d ->
                hideLoading()
                //loginData.value = d
                LiveDataBus.get().with<LoginUserData>("LoginUserData").value  = d;
                WonderCoreCache.saveData(CacheKey.LOGIN_DATA, d)
            }, { _, msg, _ ->
                hideLoading()
                showToast(msg)
            }
        )
    }

    fun checkPhoneAnddPassword(phone: String, password: String) {
        if (phone.isEmpty()) {
            showCenterToast("请输入手机号码")
        } else if (phone.length != 11) {
            showCenterToast("请输入正确的手机号码")
        } else if (password.isEmpty()) {
            showCenterToast("请输入密码")
        } else if (password.length < 6 || password.length > 16) {
            showCenterToast("密码格式错误")
        } else {
            login(phone, password)
        }
    }
}