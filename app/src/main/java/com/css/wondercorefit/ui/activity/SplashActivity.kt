package com.css.wondercorefit.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.uibase.BaseActivity
import com.css.wondercorefit.databinding.ActivitySplashBinding
import com.css.wondercorefit.viewmodel.SplashViewModel

class SplashActivity : BaseActivity<SplashViewModel, ActivitySplashBinding>() {

    override fun initViewModel(): SplashViewModel =
        ViewModelProvider(this).get(SplashViewModel::class.java)

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.mDownSecondNormalEvent.observe(this, Observer {
            gotoActMain()
        })
    }


    private fun gotoActMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        checkStoragePermission()
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

    private fun checkBodySensorPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1
                )
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )
                ) {
                    //此处需要弹窗通知用户去设置权限
                    Toast.makeText(this, "请允许获取健身运动信息，不然无法为你计步哦~", Toast.LENGTH_SHORT).show()
                }
            }
        }
        start()
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

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater,parent,false)
}