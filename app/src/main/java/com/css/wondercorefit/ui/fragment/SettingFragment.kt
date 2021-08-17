package com.css.wondercorefit.ui.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.CleanUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.ui.DataStatisticsActivity
import com.css.service.data.UserData
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.SystemBarHelper
import com.css.service.utils.WonderCoreCache
import com.css.step.service.TodayStepService
import com.css.step.utils.BootstrapService
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentSettingBinding
import com.css.wondercorefit.ui.activity.setting.AboutUsActivity
import com.css.wondercorefit.ui.activity.setting.FeedbackActivity
import com.css.wondercorefit.ui.activity.setting.MyDeviceActivity
import com.css.wondercorefit.ui.activity.setting.PersonInformationActivity
import razerdp.basepopup.BasePopupWindow


class SettingFragment : BaseFragment<DefaultViewModel, FragmentSettingBinding>(),
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    lateinit var userInfo: UserData
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        userInfo = WonderCoreCache.getUserInfo()
        mViewBinding?.rlNotificationSet?.isChecked = "开" == userInfo.notification
        mViewBinding?.rlPersonInfo?.setOnClickListener(this)
        mViewBinding?.rlMyDevice?.setOnClickListener(this)
        mViewBinding?.rlAboutUs?.setOnClickListener(this)
        mViewBinding?.rlNotificationSet?.setOnCheckedChangeListener(this)
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
                if (NetworkUtils.isConnected()) {
                    activity?.let { PersonInformationActivity.starActivity(it) }
                }else{
                    showNetworkErrorDialog()
                }
            }
            R.id.rl_my_device -> {
                if (NetworkUtils.isConnected()) {
                    activity?.let { MyDeviceActivity.starActivity(it) }
                }else{
                    showNetworkErrorDialog()
                }
            }
            R.id.rl_about_us -> {
                activity?.let { AboutUsActivity.starActivity(it) }
            }
            R.id.rl_feedback -> {
                activity?.let {
                    if (NetworkUtils.isConnected()) {
                        FeedbackActivity.starActivity(it)
                    } else {
                        showNetworkErrorDialog()
                    }
                }
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
                CommonAlertDialog(requireContext()).apply {
                    type = CommonAlertDialog.DialogType.Image
                    imageResources = com.css.ble.R.mipmap.icon_tick
                    content = "缓存已清除"
                    onDismissListener = object : BasePopupWindow.OnDismissListener() {
                        override fun onDismiss() {

                        }
                    }
                }.show()
            }
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentSettingBinding = FragmentSettingBinding.inflate(inflater, viewGroup, false)

    override fun onCheckedChanged(compoundButton: CompoundButton?, boolean: Boolean) {
        when (compoundButton?.id) {
            R.id.rl_notification_set -> {
                val intentBootstrap = Intent(activity, BootstrapService::class.java)
                val intentsteps = Intent(activity, TodayStepService::class.java)
                if (mViewBinding?.rlNotificationSet?.isChecked == true) {
                    userInfo.notification = "开"
                    WonderCoreCache.saveData(CacheKey.USER_INFO, userInfo)
                    val intent = Intent()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = "android.intent.action.OPEN_NOTIFICATION"
                    activity?.sendBroadcast(intent)
                    if (Build.VERSION.SDK_INT >= 26) {
                        activity?.startForegroundService(intentsteps)
                    } else {
                        activity?.startService(intentsteps)
                    }
                } else {
                    userInfo.notification = "关"
                    WonderCoreCache.saveData(CacheKey.USER_INFO, userInfo)
                    val intent = Intent()
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = "android.intent.action.CLOSE_NOTIFICATION"
                    activity?.sendBroadcast(intent)
                    if (Build.VERSION.SDK_INT >= 26) {
                        activity?.startForegroundService(intentBootstrap)
                    } else {
                        activity?.startService(intentBootstrap)
                    }
                }

            }
        }
    }
}