package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.CleanUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentSettingBinding
import com.css.wondercorefit.ui.activity.setting.AboutUsActivity
import com.css.wondercorefit.ui.activity.setting.FeedbackActivity
import com.css.wondercorefit.ui.activity.setting.PersonInformationActivity


class SettingFragment : BaseFragment<DefaultViewModel, FragmentSettingBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mViewBinding?.rlPersonInfo?.setOnClickListener(this)
        mViewBinding?.rlAboutUs?.setOnClickListener(this)
        mViewBinding?.exitLogin?.setOnClickListener(this)
        mViewBinding?.rlFeedback?.setOnClickListener(this)
        mViewBinding?.rlCleanCache?.setOnClickListener(this)
        mViewBinding?.tvCache?.text = FileUtils.getSize(Utils.getApp().cacheDir)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rl_person_info -> {
                activity?.let { PersonInformationActivity.starActivity(it) }
            }
            R.id.rl_about_us -> {
                activity?.let { AboutUsActivity.starActivity(it) }
            }
            R.id.rl_feedback -> {
                activity?.let { FeedbackActivity.starActivity(it) }
            }
            R.id.exit_login -> {
                activity?.let {
                    CommonAlertDialog(it).apply {
                        gravity = Gravity.BOTTOM
                        type = CommonAlertDialog.DialogType.Confirm
                        title = "确认要退出登录吗?"
                        content = "退出登录后将清除您在本地缓存的全部数据，以及未上传至云端存储的设备相关数据等。"
                        leftBtnText = "取消"
                        rightBtnText = "仍要退出"
                        listener = object : DialogClickListener.DefaultLisener() {
                            override fun onRightBtnClick(view: View) {
                                super.onRightBtnClick(view)
                                WonderCoreCache.removeKey(CacheKey.LOGIN_DATA)
                                WonderCoreCache.removeKey(CacheKey.USER_INFO)
                                ARouterUtil.openLogin()
                                finishAc()
                            }
                        }
                    }.show()
                }
            }
            R.id.rl_clean_cache -> {
                CleanUtils.cleanInternalCache()
                mViewBinding?.tvCache?.text = FileUtils.getSize(Utils.getApp().cacheDir)
            }
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentSettingBinding = FragmentSettingBinding.inflate(inflater, viewGroup, false)
}