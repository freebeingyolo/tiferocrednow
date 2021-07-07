package com.css.login.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.login.databinding.ActivityResetPasswordBinding
import com.css.login.model.ResetPasswordViewModel
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper

@Route(path = ARouterConst.PATH_APP_RESET_PWD)
class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel, ActivityResetPasswordBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
    }

    override fun initViewModel(): ResetPasswordViewModel =
        ViewModelProvider(this).get(ResetPasswordViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityResetPasswordBinding =
        ActivityResetPasswordBinding.inflate(layoutInflater, parent, false)
}