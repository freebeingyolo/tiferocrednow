package com.css.wondercorefit.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.uibase.BaseActivity
import com.css.service.router.ARouterUtil
import com.css.service.utils.WonderCoreCache
import com.css.wondercorefit.databinding.ActivitySplashBinding
import com.css.wondercorefit.viewmodel.SplashViewModel

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
        checkLocationPermission()
    }

    private fun checkStoragePermission() {
        val permission = PermissionConstants.STORAGE
        PermissionUtils.permission(permission)
            .rationale { _, shouldRequest ->
                shouldRequest.again(true)
            }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    checkLocationPermission()
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    checkLocationPermission()
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

    private fun checkLocationPermission() {
        val permission = PermissionConstants.LOCATION
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

    private fun start() {
        mViewModel.downTimeNormalTask(2)
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater, parent, false)
}