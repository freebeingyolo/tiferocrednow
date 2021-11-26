package com.css.login.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.uibase.BaseActivity
import com.css.base.utils.StringUtils
import com.css.login.R
import com.css.login.databinding.ActivityResetPasswordBinding
import com.css.login.model.ResetPasswordViewModel
import com.css.service.router.ARouterConst
import com.css.service.utils.SystemBarHelper
import razerdp.basepopup.BasePopupWindow

@Route(path = ARouterConst.PATH_APP_RESET_PWD)
class ResetPasswordActivity : BaseActivity<ResetPasswordViewModel, ActivityResetPasswordBinding>(),
    View.OnClickListener {
    var mInputIsOk1 = true
    var mInputIsOk2 = true
    var mInputIsOk3 = true
    var mInputIsOk4 = true
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        mViewBinding.tvSubmit.setOnClickListener(this)
        mViewBinding.tvSendCode.setOnClickListener(this)
        checkEdittext()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.timeDownData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = !it.matches(Regex(".*\\d+.*")) //不是读秒才enable
            mViewBinding.tvSendCode.text = it
        })
        mViewModel.resetPwdData.observe(this, {
            showCenterToast("重置成功")
            finish()
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
                KeyboardUtils.hideSoftInput(this)
                if (NetworkUtils.isConnected()) {
                    if (mInputIsOk1 && mInputIsOk2 && mInputIsOk3 && mInputIsOk4) {
                        mViewModel.checkData(
                            mViewBinding.etPhone.text.toString(),
                            mViewBinding.etPassword.text.toString(),
                            mViewBinding.etPasswordAgain.text.toString(),
                            mViewBinding.etSmsCode.text.toString()
                        )
                    } else {
                        showCenterToast("请输入正确内容")
                    }
                } else {
                    showNetworkErrorDialog()
                }
            }
            mViewBinding.tvSendCode -> {
                if (NetworkUtils.isConnected()) {
                    mViewModel.sendCode(mViewBinding.etPhone.text.toString())
                } else {
                    showNetworkErrorDialog()
                }

            }
        }
    }

    private fun showNetworkErrorDialog() {
        CommonAlertDialog(this).apply {
            type = CommonAlertDialog.DialogType.Image
            imageResources = R.mipmap.icon_error
            content = getString(R.string.network_error)
            onDismissListener = object : BasePopupWindow.OnDismissListener() {
                override fun onDismiss() {
                }
            }
        }.show()
    }

    private fun checkEdittext() {

        mViewBinding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etPhone.text.toString().length != 11 && mViewBinding.etPhone.text.toString()
                        .isNotEmpty()
                ) {
                    mViewBinding.tvPhoneTip.visibility = View.VISIBLE
                    mInputIsOk1 = false
                } else if (mViewBinding.etPhone.text.toString().isEmpty()) {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                    mInputIsOk1 = true
                } else {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                    mInputIsOk1 = true
                }
            }
        })

        mViewBinding.etSmsCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etSmsCode.text.toString().length != 6 && mViewBinding.etSmsCode.text.toString()
                        .isNotEmpty()
                ) {
                    mViewBinding.tvCodeTip.visibility = View.VISIBLE
                    mInputIsOk2 = false
                } else if (mViewBinding.etSmsCode.text.toString().isEmpty()) {
                    mViewBinding.tvCodeTip.visibility = View.INVISIBLE
                    mInputIsOk2 = true
                } else {
                    mViewBinding.tvCodeTip.visibility = View.INVISIBLE
                    mInputIsOk2 = true
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
                } else if (StringUtils.getCheckPwdSymbol(mViewBinding.etPassword.text.toString())) {
                    mViewBinding.tvPasswordTip.visibility = View.VISIBLE
                    mInputIsOk3 = false
                } else {
                    mViewBinding.tvPasswordTip.visibility = View.INVISIBLE
                    mInputIsOk3 = true
                }
                if (mViewBinding.etPassword.text.toString().isEmpty()) {
                    mViewBinding.tvPasswordTip.visibility = View.INVISIBLE
                    mInputIsOk3 = true
                }
            }
        })
        mViewBinding.etPasswordAgain.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etPasswordAgain.text.toString() != mViewBinding.etPassword.text.toString() && mViewBinding.etPasswordAgain.text.toString()
                        .isNotEmpty()
                ) {
                    mViewBinding.tvPasswordAgainTip.visibility = View.VISIBLE
                    mInputIsOk4 = false
                } else if (mViewBinding.etPasswordAgain.text.toString().isEmpty()) {
                    mViewBinding.tvPasswordAgainTip.visibility = View.INVISIBLE
                    mInputIsOk4 = true
                } else {
                    mViewBinding.tvPasswordAgainTip.visibility = View.INVISIBLE
                    mInputIsOk4 = true
                }
            }
        })
    }
}