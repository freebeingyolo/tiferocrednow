package com.css.base.uibase

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.Utils
import com.css.service.BuildConfig

open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        configArouter()
        initBlankj()
    }

    private fun initBlankj() {
        Utils.init(this)
    }

    //重写字体缩放比例 api<25
    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
        createConfigurationContext(config)
        return res
    }

    //重写字体缩放比例  api>25
    override fun attachBaseContext(base: Context?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            var res = base?.getResources();
            var config = res?.getConfiguration();
            config?.fontScale = 1f
            var newContext = config?.let { base?.createConfigurationContext(it) };
            super.attachBaseContext(newContext);
        } else {
            super.attachBaseContext(base);
        }

    }

    private fun configArouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }
}