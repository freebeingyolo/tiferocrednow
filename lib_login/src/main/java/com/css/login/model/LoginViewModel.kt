package com.css.login.model

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.RegexUtils
import com.css.base.net.HttpNetCode
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.data.LoginUserData
import com.css.service.utils.WonderCoreCache
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginViewModel : BaseViewModel() {
    //    val loginData = MutableLiveData<LoginUserData>()
    val loginFailureData = MutableLiveData<String>()
    val extraData = MutableLiveData<String>()

    fun login(
        phone: String,
        password: String
    ) {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    UserRepository.loginGet(phone, password)
                }
            }, { msg, d ->
                hideLoading()
                WonderCoreCache.saveLoginInfo(d)
            }, { code, msg, _ ->
                hideLoading()
                when (code) {
                    HttpNetCode.NET_TIMEOUT -> showCenterToast("网络请求超时")
                    HttpNetCode.NET_CONNECT_ERROR -> showCenterToast("网络连接错误")
                    else -> loginFailureData.value = msg
                }
            }
        )
    }

    fun checkPhoneAnddPassword(phone: String, password: String) {
        if (phone.isEmpty()) {
            showCenterToast("请输入手机号码")
        } else if (phone.length != 11) {
            showCenterToast("请输入正确的手机号码")
        } else if (!RegexUtils.isMobileExact(phone)) {
            showCenterToast("请输入正确的手机号码")
        } else if (password.isEmpty()) {
            showCenterToast("请输入密码")
        } else if (password.length < 6 || password.length > 16) {
            showCenterToast("密码格式错误")
        } else {
            login(phone, password)
        }
    }

    fun jdLogin(
        code: String
    ) {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    UserRepository.jdLogin(code)
                }
            }, { msg, d ->
                hideLoading()
                WonderCoreCache.saveLoginInfo(Gson().fromJson(d, LoginUserData::class.java));
            }, { code, msg, d ->
                hideLoading()
                when (code) {
                    HttpNetCode.NET_TIMEOUT -> showCenterToast("网络请求超时")
                    HttpNetCode.NET_CONNECT_ERROR -> showCenterToast("网络连接错误")
                    else -> {
                        if ("请绑定手机号".equals(msg)) {
                            extraData.value = d?.toString()
                        } else {
                            loginFailureData.value = msg
                        }
                    }
                }
            }
        )
    }
}