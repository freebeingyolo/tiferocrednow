package com.css.login.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.login.databinding.ActivityLoginBinding
import com.css.login.model.LoginViewModel
import com.css.service.router.ARouterConst
import com.css.service.router.ARouterUtil
import com.css.service.utils.SystemBarHelper

@Route(path = ARouterConst.PATH_APP_LOGIN)
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>(), View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        mViewBinding.tvRegister.setOnClickListener(this)
        mViewBinding.tvLogin.setOnClickListener(this)
        mViewBinding.forgetPassword.setOnClickListener(this)
    }

    override fun initViewModel(): LoginViewModel =
        ViewModelProvider(this).get(LoginViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater, parent, false)

    override fun registorUIChangeLiveDataCallBack() {
        mViewModel.loginData.observe(this, {
            ARouterUtil.openMainActivity()
            finish()
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            mViewBinding.tvRegister -> {
                ARouterUtil.openRegister()
            }
            mViewBinding.tvLogin -> {
                mViewModel.checkPhoneAnddPassword(mViewBinding.etTelephone.text.toString(), mViewBinding.etPassword.text.toString())
            }
            mViewBinding.forgetPassword -> {
                ARouterUtil.openForgetPassword()
            }
        }
    }
}