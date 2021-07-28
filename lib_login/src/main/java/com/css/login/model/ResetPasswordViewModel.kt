package com.css.login.model

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.css.base.net.api.repository.UserRepository
import com.css.base.uibase.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetPasswordViewModel : BaseViewModel() {
    val resetPwdData = MutableLiveData<String>()
    val timeDownData = MutableLiveData<Long>()
    val resetCodeData = MutableLiveData<String>()
    private var mTimer: CountDownTimer? = null
    private fun resetPassword(
        phone: String,
        password: String,
        smsCode: String
    ) {
        netLaunch(
            {
                withContext(Dispatchers.IO) {
                    UserRepository.resetPassword(phone, password, smsCode)
                }
            }, { msg, _ ->
                resetPwdData.value = msg
            }, { _, msg, _ ->
                showToast(msg)
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
                    mTimer!!.start()
                    showLoading()
                    withContext(Dispatchers.IO) {
                        UserRepository.sendCode(phone)
                    }
                }, { msg, _ ->
                    hideLoading()
                    showToast(msg)
                }, { _, msg, _ ->
                    hideLoading()
                    showToast(msg)
                }
            )
        } else {
            showToast("请输入手机号")
        }
    }
    fun checkData(
        phone: String, password: String, passwordAgain: String,
        smsCode: String
    ) {
        if (phone.isEmpty()) {
            showCenterToast("请输入手机号码")
        } else if (phone.length != 11) {
            showCenterToast("请输入正确的手机号码")
        } else if (password.isEmpty()) {
            showCenterToast("请输入密码")
        } else if (password.length < 6 || password.length > 16) {
            showCenterToast("密码格式错误")
        } else if (password != passwordAgain) {
            showCenterToast("两次密码输入不一致，请重新输入")
        }else if (smsCode.isEmpty()) {
            showCenterToast("请输入验证码")
        }else {
            resetPassword(phone, password,smsCode)
        }
    }
}