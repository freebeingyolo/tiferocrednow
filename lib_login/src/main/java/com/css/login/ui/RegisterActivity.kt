package com.css.login.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.login.databinding.ActivityLoginBinding
import com.css.login.databinding.ActivityRegisterBinding
import com.css.login.model.LoginViewModel
import com.css.login.model.RegisterViewModel
import com.css.service.router.ARouterConst.PATH_APP_REGISTER
import com.css.service.router.ARouterUtil
import com.css.service.utils.SystemBarHelper

@Route(path = PATH_APP_REGISTER)
class RegisterActivity : BaseActivity<RegisterViewModel, ActivityRegisterBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        mViewBinding.tvRegisterBtn.setOnClickListener(this)
        mViewBinding.tvToLogin.setOnClickListener(this)
        mViewBinding.tvSendCode.setOnClickListener(this)
    }

    override fun initViewModel(): RegisterViewModel =
        ViewModelProvider(this).get(RegisterViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        mViewModel.registerData.observe(this, {
            showToast(it)
        })
        mViewModel.resetCodeData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = true
            mViewBinding.tvSendCode.text = it
        })
        mViewModel.timeDownData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = false
            mViewBinding.tvSendCode.text = "${it}秒后可重发"
        })
        mViewModel.registerData.observe(this, {
            finish()
            ARouterUtil.openLogin()
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.tvRegisterBtn -> {
                mViewModel.checkData(
                    mViewBinding.etTelephone.text.toString(),
                    mViewBinding.etPassword.text.toString(),
                    mViewBinding.etPasswordAgain.text.toString(),
                    mViewBinding.etSmsCode.text.toString(),
                    mViewBinding.etUsername.text.toString(),
                    mViewBinding.cbAgreement.isChecked
                )
            }
            mViewBinding.tvToLogin -> {
                ARouterUtil.openLogin()
                finish()
            }
            mViewBinding.tvSendCode -> {
                mViewModel.sendCode(mViewBinding.etTelephone.text.toString())
            }
        }
    }
}