package com.css.login.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.css.base.uibase.BaseActivity
import com.css.base.utils.StringUtils
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
        checkEdittext()
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

    private fun checkEdittext() {
        mViewBinding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etUsername.text.toString().length > 10) {
                    mViewBinding.tvUsernameTip.visibility = View.VISIBLE
                } else if (mViewBinding.etUsername.text.toString().isEmpty()) {
                    mViewBinding.tvUsernameTip.visibility = View.INVISIBLE
                } else if (StringUtils.getCheckSymbol(mViewBinding.etUsername.text.toString())) {
                    mViewBinding.tvUsernameTip.visibility = View.VISIBLE
                } else {
                    mViewBinding.tvUsernameTip.visibility = View.INVISIBLE
                }
            }
        })
        mViewBinding.etTelephone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etTelephone.text.toString().length != 11&&mViewBinding.etTelephone.text.toString().isNotEmpty()) {
                    mViewBinding.tvPhoneTip.visibility = View.VISIBLE
                } else if (mViewBinding.etTelephone.text.toString().isEmpty()) {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                } else {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                }
            }
        })

        mViewBinding.etSmsCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etSmsCode.text.toString().length != 6&&mViewBinding.etSmsCode.text.toString().isNotEmpty()) {
                    mViewBinding.tvCodeTip.visibility = View.VISIBLE
                } else if (mViewBinding.etSmsCode.text.toString().isEmpty()) {
                    mViewBinding.tvCodeTip.visibility = View.INVISIBLE
                } else {
                    mViewBinding.tvCodeTip.visibility = View.INVISIBLE
                }
            }
        })
        mViewBinding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etPassword.text.toString().length < 6 || mViewBinding.etPassword.text.toString().length > 16) {
                    mViewBinding.tvPasswordTip.visibility = View.VISIBLE
                }else {
                    mViewBinding.tvPasswordTip.visibility = View.INVISIBLE
                }
                if (mViewBinding.etPassword.text.toString().isEmpty()) {
                    mViewBinding.tvPasswordTip.visibility = View.INVISIBLE
                }
            }
        })
        mViewBinding.etPasswordAgain.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etPasswordAgain.text.toString() != mViewBinding.etPassword.text.toString()&&mViewBinding.etPasswordAgain.text.toString().isNotEmpty()) {
                    mViewBinding.tvPasswordAgainTip.visibility = View.VISIBLE
                } else if (mViewBinding.etPasswordAgain.text.toString().isEmpty()) {
                    mViewBinding.tvPasswordAgainTip.visibility = View.INVISIBLE
                } else {
                    mViewBinding.tvPasswordAgainTip.visibility = View.INVISIBLE
                }
            }
        })
    }
}