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
import com.css.login.databinding.ActivityLoginBinding
import com.css.login.databinding.ActivityRegisterBinding
import com.css.login.model.LoginViewModel
import com.css.login.model.RegisterViewModel
import com.css.service.router.ARouterConst.PATH_APP_REGISTER

@Route(path = PATH_APP_REGISTER)
class RegisterActivity : BaseActivity<RegisterViewModel, ActivityRegisterBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mViewBinding.tvRegister.setOnClickListener(this)
    }

    override fun initViewModel(): RegisterViewModel =
        ViewModelProvider(this).get(RegisterViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        mViewModel.registerData.observe(this, Observer {
            showToast(it)
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.tvRegister -> {
                mViewModel.register("15959994075", "123456", "1234", "ruis")
            }
        }
    }
}