package com.css.login.model

import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel

class RegisterViewModel : BaseViewModel() {
    val registerData = MutableLiveData<String>()
    fun register(
        phone: String,
        password: String,
        smsCode: String,
        userName: String
    ) {
        netLaunch(
            {
                UserRepository.register(phone, password, smsCode, userName)
            }, { msg, d ->
                registerData.value = msg
            }, { _, msg, _ ->
                showToast(msg)
            }
        )
    }
}