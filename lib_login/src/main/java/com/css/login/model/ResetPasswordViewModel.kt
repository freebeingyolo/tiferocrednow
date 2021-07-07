package com.css.login.model

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel

class ResetPasswordViewModel : BaseViewModel() {
    val resetPwdData = MutableLiveData<String>()

    fun register(
        phone: String,
        password: String,
        smsCode: String
    ) {
        netLaunch(
            {
                UserRepository.resetPassword(phone, password, smsCode)
            }, { msg, _ ->
                resetPwdData.value = msg
            }, { _, msg, _ ->
                showToast(msg)
            }
        )
    }

    fun checkPhoneAnddPassword(phone: String, password: String, smsCode: String) {
        if (phone.isEmpty()) {
            showCenterToast("请输入手机号码")
        } else if (phone.length != 11) {
            showCenterToast("请输入正确的手机号码")
        } else if (password.isEmpty()) {
            showCenterToast("请输入密码")
        } else if (password.length < 6 || password.length > 16) {
            showCenterToast("密码格式错误")
        } else {
            register(phone, password,smsCode)
        }
    }
}