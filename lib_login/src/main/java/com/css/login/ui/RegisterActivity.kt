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

@Route(path = PATH_APP_REGISTER)
class RegisterActivity : BaseActivity<RegisterViewModel, ActivityRegisterBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("注册")
        mViewBinding.tvRegister.setOnClickListener(this)
        mViewBinding.tvToLogin.setOnClickListener(this)
    }

    override fun initViewModel(): RegisterViewModel =
        ViewModelProvider(this).get(RegisterViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        mViewModel.registerData.observe(this, Observer {
            showToast(it)
        })
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.WHITE
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
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
            mViewBinding.tvToLogin -> {
                ARouterUtil.openLogin()
                finish()
            }
        }
    }
}