package com.css.login.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.uibase.BaseActivity
import com.css.login.databinding.ActivityPwdBindBinding
import com.css.login.model.PwdBindViewModel
import com.css.service.data.LoginUserData
import com.css.service.router.ARouterConst.PATH_APP_CODE_BIND
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache

@Route(path = PATH_APP_CODE_BIND)
class PwdBindActivity : BaseActivity<PwdBindViewModel, ActivityPwdBindBinding>(),
    View.OnClickListener {
    var extra = ""

    companion object {
        fun starActivity(context: Context, extra: String) {
            val intent = Intent(context, PwdBindActivity::class.java)
            intent.putExtra("extra", extra)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(this, 0f)
        SystemBarHelper.setHeightAndPadding(this, mViewBinding.topView)
        extra = intent.getStringExtra("extra")!!
        mViewBinding.tvBindBtn.setOnClickListener(this)
        mViewBinding.tvToCodeBind.setOnClickListener(this)
    }

    override fun initViewModel(): PwdBindViewModel =
        ViewModelProvider(this).get(PwdBindViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        WonderCoreCache.getLiveData<LoginUserData>(CacheKey.LOGIN_DATA).observe(this) {
            ARouterUtil.openMainActivity()
            finish()
        }
        mViewModel.loginFailureData.observe(this, {
            showCenterToast(it)
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityPwdBindBinding = ActivityPwdBindBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.tvBindBtn -> {
                KeyboardUtils.hideSoftInput(this)
                if (NetworkUtils.isConnected()) {
                    mViewModel.checkData(
                        mViewBinding.etTelephone.text.toString(),
                        mViewBinding.etPassword.text.toString(),
                        extra
                    )
                } else {
                    showNetworkErrorDialog()
                }

            }
            mViewBinding.tvToCodeBind -> {
                CodeBindActivity.starActivity(this, extra)
                finish()
            }
        }
    }
}