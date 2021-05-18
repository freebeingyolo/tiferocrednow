package com.css.wondercorefit.policy

import android.graphics.Canvas

/**
 * 圆角策略接口
 * @author chenPan
 *
 * @time Created by 2021/05/18
 *
 */
interface IRoundViewPolicy {
    fun beforeDispatchDraw(canvas: Canvas?)
    fun afterDispatchDraw(canvas: Canvas?)
    fun onLayout(left: Int, top: Int, right: Int, bottom: Int)
    fun setCornerRadius(cornerRadius: Float)
}