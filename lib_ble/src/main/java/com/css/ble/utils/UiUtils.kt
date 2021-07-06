package com.css.ble.utils

import android.app.Application
import android.graphics.Rect
import android.os.Looper
import android.view.TouchDelegate
import android.view.View
import cn.wandersnail.commons.util.UiUtils
import com.blankj.utilcode.util.ActivityUtils

/**
 * @author yuedong
 * @date 2021-06-07
 */
object UiUtils {
    /**
     * 用于扩大View的点击范围
     *
     * @param delegateView 需要扩大点击范围的View,必须保证delegateView的parent足够的大，足以放下扩展的mTouchDelegate区域
     * @param l          left
     * @param t          top
     * @param r          right
     * @param b          bottom
     */
    fun largerViewBounds(delegateView: View, l: Int, t: Int, r: Int, b: Int) {
        val parent: View = delegateView.parent as View
        val action = fun() {
            val mRect = Rect()
            delegateView.getHitRect(mRect)
            mRect.left += l
            mRect.top += t
            mRect.right += r
            mRect.bottom += b
            val mTouchDelegate = TouchDelegate(mRect, delegateView)
            parent.touchDelegate = mTouchDelegate
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            action()
        } else {
            parent.post(action)
        }
    }

    fun getApplication(): Application {
        val clazz = Class.forName("android.app.ActivityThread")
        val acThreadMethod = clazz.getMethod("currentActivityThread")
        acThreadMethod.isAccessible = true
        val acThread = acThreadMethod.invoke(null)
        val appMethod = acThread.javaClass.getMethod("getApplication")
        val app = appMethod.invoke(acThread) as Application
        return app
    }


}