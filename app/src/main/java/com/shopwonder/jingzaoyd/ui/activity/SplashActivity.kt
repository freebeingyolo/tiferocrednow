package com.shopwonder.jingzaoyd.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.service.router.ARouterUtil
import com.css.service.utils.WonderCoreCache
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ActivitySplashBinding
import com.shopwonder.jingzaoyd.ui.activity.setting.TermsActivity
import com.shopwonder.jingzaoyd.utils.SharedPreferencesUtils
import com.shopwonder.jingzaoyd.viewmodel.SplashViewModel

class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    override fun initViewModel(): SplashViewModel =
        ViewModelProvider(this).get(SplashViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.mDownSecondNormalEvent.observe(this, Observer {
            val loginUserData = WonderCoreCache.getLoginInfo()
            if (loginUserData != null) {
                gotoActMain()
            } else {
                if (WonderCoreCache.getGlobalData().isFirst) {
                    ARouterUtil.openRegister()
                } else {
                    ARouterUtil.openLogin()
                }
            }
            finish()
        })
    }

    private fun gotoActMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        checkPrivacy()
    }

    private fun checkStoragePermission() {
        val permission = PermissionConstants.STORAGE
        PermissionUtils.permission(permission)
            .rationale { _, shouldRequest ->
                shouldRequest.again(true)
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    checkBodySensorPermission()
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    checkBodySensorPermission()
                }
            })
            .request()
    }

    @SuppressLint("WrongConstant")
    private fun checkBodySensorPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            PermissionUtils.permission(permission)
                .rationale { _, shouldRequest ->
                    shouldRequest.again(true)
                }
                .callback(object : PermissionUtils.FullCallback {
                    override fun onGranted(permissionsGranted: List<String>) {
                        start()
                    }

                    override fun onDenied(
                        permissionsDeniedForever: List<String>,
                        permissionsDenied: List<String>
                    ) {
                        start()
                    }
                })
                .request()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            start()
        }
    }

    private fun checkPrivacy() {
        if (!SharedPreferencesUtils.getInstance().getBoolean("isFirstUse", false)) {
            val builder = SpannableStringBuilder("请你务必阅读、充分理解《服务协议》和《隐私政策》各条款，包括但不限于：为了向你提供运动教程、内容分享等服务，我们需要收集你的设备信息、操作日志等个人信息。你可以在隐私设置中查看、变更、删除你的个人信息并管理你的授权。\r\n未经你的授权同意，我们不会主动向任何第三方共享你的个人信息。")
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    TermsActivity.starActivity(applicationContext, TermsActivity.TERMS_SERVICE)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(R.color.color_0066ff)
                    ds.isUnderlineText = false
                }
            }, 11, 17, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    TermsActivity.starActivity(applicationContext, TermsActivity.TERMS_PRIVACY)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(R.color.color_0066ff)
                    ds.isUnderlineText = false
                }
            }, 18, 24, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            CommonAlertDialog(this).apply {
                type = CommonAlertDialog.DialogType.Scroll
                title = "京造运动APP隐私与许可服务协议"
                content = builder
                leftBtnText = "同意"
                rightBtnText = "不同意"
                gravity = Gravity.CENTER
                outSideDismiss = false
                backPressEnable = false
                listener = object: DialogClickListener.DefaultLisener() {
                    override fun onLeftBtnClick(view: View) {
                        super.onLeftBtnClick(view)
                        SharedPreferencesUtils.getInstance().saveData("isFirstUse", true)
                        checkLocationPermission()
                    }
                    override fun onRightBtnClick(view: View) {
                        super.onRightBtnClick(view)
                        finish()
                    }
                }
            }.show()
        } else {
            checkLocationPermission();
        }
    }

    private fun checkLocationPermission() {
        val permission = PermissionConstants.LOCATION
        PermissionUtils.permission(permission)
            .rationale { _, shouldRequest ->
                shouldRequest.again(true)
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    checkStoragePermission()
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    checkStoragePermission()
                }
            })
            .request()
    }

    private fun start() {
        mViewModel.downTimeNormalTask(2)
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater, parent, false)
}