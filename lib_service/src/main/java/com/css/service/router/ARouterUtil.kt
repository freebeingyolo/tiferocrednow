package com.css.service.router

import com.alibaba.android.arouter.launcher.ARouter

object ARouterUtil {
        fun openMainActivity() {
            ARouter.getInstance().build(ARouterConst.PATH_APP_MAIN)
                .navigation()
        }
}