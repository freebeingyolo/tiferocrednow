package com.css.login.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.login.databinding.ActivityResetPasswordBinding
import com.css.login.model.ResetPasswordViewModel
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper

@Route(path = ARouterConst.PATH_APP_RESET_PWD)
class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel, ActivityResetPasswordBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)

        mViewBinding.tvSubmit.setOnClickListener(this)
    }

    override fun initViewModel(): ResetPasswordViewModel =
        ViewModelProvider(this).get(ResetPasswordViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityResetPasswordBinding =
        ActivityResetPasswordBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.tvSubmit -> {
                if (mViewBinding.etPassword.text.toString() != mViewBinding.etPasswordAgain.text.toString()) {
                    showToast("两次密码输入不一致，请重新输入")
                    mViewBinding.etPassword.setText("")
                    mViewBinding.etPasswordAgain.setText("")
                } else {
                    mViewModel.checkPhoneAnddPassword(
                        mViewBinding.etPhone.text.toString(),
                        mViewBinding.etPassword.text.toString(),
                        mViewBinding.etSmsCode.text.toString()
                    )
                }
            }
        }
    }
}