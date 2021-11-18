package com.css.service.router

import com.alibaba.android.arouter.launcher.ARouter

object ARouterUtil {
        fun openMainActivity() {
            ARouter.getInstance().build(ARouterConst.PATH_APP_MAIN)
                .navigation()
        }

    fun openRegister(){
        ARouter.getInstance().build(ARouterConst.PATH_APP_REGISTER)
            .navigation()
    }

    fun openLogin(){
        ARouter.getInstance().build(ARouterConst.PATH_APP_LOGIN)
            .navigation()
    }
    fun openForgetPassword(){
        ARouter.getInstance().build(ARouterConst.PATH_APP_RESET_PWD)
            .navigation()
    }
    fun openCodeBind(){
        ARouter.getInstance().build(ARouterConst.PATH_APP_CODE_BIND)
            .navigation()
    }
    fun openPwdBind(){
        ARouter.getInstance().build(ARouterConst.PATH_APP_PWD_BIND)
            .navigation()
    }
}