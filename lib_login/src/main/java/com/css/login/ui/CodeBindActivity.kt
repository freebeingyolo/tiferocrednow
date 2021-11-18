package com.css.login.ui

import android.content.Context
import android.content.Intent
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
import com.css.base.uibase.BaseActivity
import com.css.login.databinding.ActivityCodeBindBinding
import com.css.login.model.CodeBindViewModel
import com.css.service.data.LoginUserData
import com.css.service.router.ARouterConst.PATH_APP_CODE_BIND
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache

@Route(path = PATH_APP_CODE_BIND)
class CodeBindActivity : BaseActivity<CodeBindViewModel, ActivityCodeBindBinding>(),
    View.OnClickListener {
    var mInputIsOk1 = true
    var mInputIsOk2 = true
    var extra = ""

    companion object {
        fun starActivity(context: Context, extra: String) {
            val intent = Intent(context, CodeBindActivity::class.java)
            intent.putExtra("extra", extra)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        extra = intent.getStringExtra("extra")
        mViewBinding.tvBindBtn.setOnClickListener(this)
        mViewBinding.tvToPwdBind.setOnClickListener(this)
        mViewBinding.tvSendCode.setOnClickListener(this)
        checkEdittext()
    }

    override fun initViewModel(): CodeBindViewModel =
        ViewModelProvider(this).get(CodeBindViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        WonderCoreCache.getLiveData<LoginUserData>(CacheKey.LOGIN_DATA).observe(this) {
            ARouterUtil.openMainActivity()
            finish()
        }
        mViewModel.resetCodeData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = true
            mViewBinding.tvSendCode.text = it
        })
        mViewModel.timeDownData.observe(this, {
            mViewBinding.tvSendCode.isEnabled = false
            mViewBinding.tvSendCode.text = "${it}秒后可重发"
        })
        mViewModel.loginFailureData.observe(this, {
            showCenterToast(it)
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityCodeBindBinding = ActivityCodeBindBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.tvBindBtn -> {
                KeyboardUtils.hideSoftInput(this)
                if (NetworkUtils.isConnected()) {
                    if (mInputIsOk1 && mInputIsOk2) {
                        mViewModel.checkData(
                            mViewBinding.etTelephone.text.toString(),
                            mViewBinding.etSmsCode.text.toString(),
                            extra
                        )
                    } else {
                        showCenterToast("请输入正确内容")
                    }
                } else {
                    showNetworkErrorDialog()
                }

            }
            mViewBinding.tvToPwdBind -> {
                PwdBindActivity.starActivity(this, extra)
                finish()
            }
            mViewBinding.tvSendCode -> {
                if (NetworkUtils.isConnected()) {
                    mViewModel.sendCode(mViewBinding.etTelephone.text.toString())
                } else {
                    showNetworkErrorDialog()
                }
            }
        }
    }

    private fun checkEdittext() {
        mViewBinding.etTelephone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (mViewBinding.etTelephone.text.toString().length != 11 && mViewBinding.etTelephone.text.toString()
                        .isNotEmpty()
                ) {
                    mViewBinding.tvPhoneTip.visibility = View.VISIBLE
                    mInputIsOk1 = false
                } else if (mViewBinding.etTelephone.text.toString().isEmpty()) {
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
    }
}