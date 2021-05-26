package com.css.base.uibase

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.css.base.ActivityHolder
import com.css.base.uibase.base.BaseWonderActivity
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseActivity<VM : BaseViewModel, VB : ViewBinding> : BaseWonderActivity<VM, VB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ActivityHolder.addActivity(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; // 禁用横屏
    }

    override fun onStop() {
        super.onStop()
//        if (this.isFinishing) ActivityHolder.removeActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
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
            var res = base?.resources
            var config = res?.configuration
            config?.fontScale = 1f
            var newContext = config?.let { base?.createConfigurationContext(it) }
            super.attachBaseContext(newContext)
        } else {
            super.attachBaseContext(base)
        }
    }
}