package com.css.login.util;

import android.content.Context;

import com.jingdong.auth.login.AuthLogin;

/**
 * @Auther: bieli
 * @datetime: 2019/10/25
 * @desc:
 */
public class AuthHelper {
    static AuthLogin authLogin;

    public static AuthLogin getAuthLogin(Context context) {
        if (authLogin == null) {
            authLogin = AuthLogin.createInstance(context, "jd0d5b0328628f5ba2", "3c2c5c4f8dd0331890aca57bff3842e7");
//            AuthLogin.setDebug(true);//内部开发联调用的，切换开发环境和下上环境
//            AuthLogin.openLog(true); //打开SDKLog
        }
        return authLogin;
    }
}
