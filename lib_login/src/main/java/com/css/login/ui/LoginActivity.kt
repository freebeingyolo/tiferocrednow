package com.css.login.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.dialog.CenterCommonAlertDialog
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.login.R
import com.css.login.databinding.ActivityLoginBinding
import com.css.login.model.LoginViewModel
import com.css.login.util.AuthHelper
import com.css.service.data.LoginUserData
import com.css.service.router.ARouterConst
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache
import com.jingdong.auth.login.JDAuthListener
import org.json.JSONObject

import razerdp.basepopup.BasePopupWindow

@Route(path = ARouterConst.PATH_APP_LOGIN)
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>(), View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        mViewBinding.tvRegister.setOnClickListener(this)
        mViewBinding.tvLogin.setOnClickListener(this)
        mViewBinding.forgetPassword.setOnClickListener(this)
        mViewBinding.ivJd.setOnClickListener(this)

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
                } else if (mViewBinding.etTelephone.text.toString().isEmpty()) {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                } else {
                    mViewBinding.tvPhoneTip.visibility = View.INVISIBLE
                }
            }

        })
    }

    override fun initViewModel(): LoginViewModel =
        ViewModelProvider(this).get(LoginViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater, parent, false)

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        WonderCoreCache.getLiveData<LoginUserData>(CacheKey.LOGIN_DATA).observe(this) {
            ARouterUtil.openMainActivity()
            finish()
        }

        mViewModel.loginFailureData.observe(this, {
            if (it?.contains("未注册") == true || it?.contains("登录失败") == true) {
                showToast(it)
                return@observe
            }
            CommonAlertDialog(baseContext).apply {
                type = CommonAlertDialog.DialogType.Center
                content = "账号或密码错误，请重新输入"
                leftBtnText = "忘记密码"
                rightBtnText = "重新输入"
                listener = object : DialogClickListener.DefaultLisener() {
                    override fun onRightBtnClick(view: View) {
                        super.onRightBtnClick(view)
                        mViewBinding.etTelephone.setText("")
                        mViewBinding.etPassword.setText("")
                    }

                    override fun onLeftBtnClick(view: View) {
                        super.onLeftBtnClick(view)
                        ARouterUtil.openForgetPassword()
                    }
                }
            }.show()
        })

        mViewModel.extraData.observe(this, {
            CodeBindActivity.starActivity(this, it)
            finish()
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            mViewBinding.tvRegister -> {
                ARouterUtil.openRegister()
            }
            mViewBinding.tvLogin -> {
                KeyboardUtils.hideSoftInput(this)
                if (NetworkUtils.isConnected()) {
                    mViewModel.checkPhoneAnddPassword(
                        mViewBinding.etTelephone.text.toString(),
                        mViewBinding.etPassword.text.toString()
                    )
                } else {
                    showNetworkErrorDialog()
                }

            }
            mViewBinding.forgetPassword -> {
                ARouterUtil.openForgetPassword()
                mViewBinding.etPassword.setText("")
            }
            mViewBinding.ivJd -> {
                if (NetworkUtils.isConnected()) {
                    AuthHelper.getAuthLogin(this).login(this, object : JDAuthListener {
                        override fun onSuccess(p0: JSONObject?) {
                            p0?.getString("accCode")?.let { mViewModel.jdLogin(it) }
                        }
                        override fun onError(p0: JSONObject?) {
                            showCenterToast(p0?.getString("errMsg"))
                        }
                    });
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
}