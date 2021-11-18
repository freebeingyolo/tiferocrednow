package com.css.login.model

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.RegexUtils
import com.css.base.net.HttpNetCode
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.service.utils.WonderCoreCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CodeBindViewModel : BaseViewModel() {

    val loginFailureData = MutableLiveData<String>()
    val timeDownData = MutableLiveData<Long>()
    val resetCodeData = MutableLiveData<String>()
    private var mTimer: CountDownTimer? = null

    fun codeBind(
        phone: String,
        smsCode: String,
        extra: String
    ) {
        netLaunch(
            {
                showLoading()
                withContext(Dispatchers.IO) {
                    UserRepository.codeBind(phone, smsCode, extra)
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

    fun sendCode(
        phone: String
    ) {
        if (phone.isNotEmpty()) {

            netLaunch(
                {
                    mTimer = object : CountDownTimer(60 * 1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            var seconds = millisUntilFinished / 1000 + 1
                            timeDownData.value = seconds
                        }

                        override fun onFinish() {
                            resetCodeData.value = "重发验证码"
                        }
                    }
                    showLoading()
                    withContext(Dispatchers.IO) {
                        UserRepository.sendCode(phone)
                    }
                }, { msg, _ ->
                    hideLoading()
                    mTimer!!.start()
                    showCenterToast(msg)
                }, { _, msg, _ ->
                    hideLoading()
                    showCenterToast(msg)
                }
            )
        } else {
            showCenterToast("请输入手机号")
        }
    }

    fun checkData( phone: String, smsCode: String, extra: String
    ) {
        if (phone.isEmpty()) {
            showCenterToast("请输入手机号码")
        } else if (phone.length != 11) {
            showCenterToast("请输入正确的手机号码")
        } else if (!RegexUtils.isMobileExact(phone)) {
            showCenterToast("请输入正确的手机号码")
        } else if (smsCode.isEmpty()) {
            showCenterToast("请输入验证码")
        } else {
            codeBind(phone, smsCode, extra)
        }
    }
}