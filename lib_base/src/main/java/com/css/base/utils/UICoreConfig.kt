package com.css.base.utils

import com.css.base.BuildConfig

/**
 * @author Ruis
 * @date 2020/6/2
 */
object UICoreConfig {

    /**
     * 异常监听
     */
    var mode = BuildConfig.DEBUG//环境
    var defaultThemeColor = 0//主题色
    var defaultEmptyIcon = 0//空布局图标
    var loadErrorIcon = 0//错误布局图标
    var netDisconnectIcon = 0//网络断开图标
    var loadingLottie = ""//加载中布局动图
    var progressLottie = ""//加载中弹窗动图
}