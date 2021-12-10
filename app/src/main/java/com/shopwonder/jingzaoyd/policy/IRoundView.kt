package com.shopwonder.jingzaoyd.policy

/**
 * 通用圆角布局抽象接口
 * @author chenPan
 *
 * @time Created by 2021/05/18
 *
 */
interface IRoundView {
    fun setCornerRadius(cornerRadius: Float)
    fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int)
}