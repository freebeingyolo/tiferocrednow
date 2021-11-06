package com.css.base.uibase

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.viewbinding.ViewBinding
import com.css.base.uibase.base.BaseWonderActivity
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : BaseWonderActivity<VM, VB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; // 禁用横屏
    }

    //重写字体缩放比例 api<25
    override fun getResources(): Resources {
        var resources = super.getResources()
        val newConfig: Configuration = resources!!.configuration
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        if (newConfig.fontScale != 1f) {
            newConfig.fontScale = 1f
            val configurationContext = createConfigurationContext(newConfig)
            resources = configurationContext.resources
            displayMetrics.scaledDensity = displayMetrics.density * newConfig.fontScale
        }
        return resources!!
    }

    //重写字体缩放比例  api>25
    override fun attachBaseContext(base: Context?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            val res = base?.resources
            val config = res?.configuration
            config?.fontScale = 1f
            val newContext = config?.let { base?.createConfigurationContext(it) }
            super.attachBaseContext(newContext)
        } else {
            super.attachBaseContext(base)
        }
    }
}