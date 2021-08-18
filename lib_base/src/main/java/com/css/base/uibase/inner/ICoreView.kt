package com.css.base.uibase.inner

import androidx.annotation.StringRes

/**
 * ViewModel与View的契约接口
 * @author Ruis
 * @date 2020/1/18
 */
interface ICoreView {

    /**
     * 吐司
     */
    fun showToast(msg: String?, onDismiss: (() -> Unit)? = null)

    /**
     * 长吐司
     */
    fun showLongToast(msg: String?, onDismiss: (() -> Unit)? = null)

    /**
     * 吐司
     */
    fun showToast(@StringRes resId: Int,onDismiss: (() -> Unit)? = null)

    /**
     * 长吐司
     */
    fun showLongToast(@StringRes resId: Int, onDismiss: (() -> Unit)? = null)

    /**
     * 显示在屏幕中间的吐司
     */
    fun showCenterToast(msg: String?, onDismiss: (() -> Unit)? = null)

    /**
     * 显示在屏幕中间的长吐司
     */
    fun showCenterLongToast(msg: String?, onDismiss: (() -> Unit)? = null)

    /**
     * 显示在屏幕中间的吐司
     */
    fun showCenterToast(@StringRes resId: Int, onDismiss: (() -> Unit)? = null)

    /**
     * 显示在屏幕中间的长吐司
     */
    fun showCenterLongToast(@StringRes resId: Int, onDismiss: (() -> Unit)? = null)

    /**
     * 关闭Activity
     */
    fun finishAc()

    /**
     * 显示loading
     */
    fun showLoading()

    /**
     * 显示在屏幕中间的吐司
     */
    fun hideLoading()
}