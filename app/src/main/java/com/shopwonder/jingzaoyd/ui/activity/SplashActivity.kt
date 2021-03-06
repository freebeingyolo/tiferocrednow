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
            val builder = SpannableStringBuilder("????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????\r\n??????????????????????????????????????????????????????????????????????????????????????????")
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    TermsActivity.starActivity(applicationContext, TermsActivity.TERMS_SERVICE)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(R.color.color_e1251b)
                    ds.isUnderlineText = false
                }
            }, 11, 17, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    TermsActivity.starActivity(applicationContext, TermsActivity.TERMS_PRIVACY)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(R.color.color_e1251b)
                    ds.isUnderlineText = false
                }
            }, 18, 24, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            CommonAlertDialog(this).apply {
                type = CommonAlertDialog.DialogType.Scroll
                title = "????????????APP???????????????????????????"
                content = builder
                leftBtnText = "??????"
                rightBtnText = "?????????"
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