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

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.resetCodeData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = true
            mViewBinding.tvSendCode.text = it
        })
        mViewModel.timeDownData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = false
            mViewBinding.tvSendCode.text = "${it}秒后可重发"
        })
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
                mViewModel.checkData(
                    mViewBinding.etPhone.text.toString(),
                    mViewBinding.etPassword.text.toString(),
                    mViewBinding.etPasswordAgain.text.toString(),
                    mViewBinding.etSmsCode.text.toString()
                )
            }
        }
    }
}