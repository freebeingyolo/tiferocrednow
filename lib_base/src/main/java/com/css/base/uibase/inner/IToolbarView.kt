package com.css.base.uibase.inner

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.*
import com.css.base.R
import com.css.base.view.ToolBarView

/**
 * 标题栏接口
 * @author yuedong
 * @date 2021-05-17
 */
interface IToolbarView {
    /**
     * 就否支持默认有返回按键的 toolBar
     *
     * @return
     */
    fun enabledDefaultBack(): Boolean = true

    /**
     * 当前页面Fragment支持沉浸式初始化。默认返回false，可设置支持沉浸式初始化
     * Immersion bar enabled boolean.
     *
     * @return the boolean
     */
    fun enabledImmersion(): Boolean = true

    /**
     * 是否开启解决沉浸式状态栏与edittext冲突问题(常见场景：聊天页属于沉浸式并且布局底部有edittext，如果没开启的话，底部的edittext弹出软键盘后没法跟着一起顶上去)
     */
    fun enbaleFixImmersionAndEditBug(): Boolean = false


    /**
     * 获取自定义toolBarView 资源id 默认为-1，showToolBar()方法必须返回true才有效
     *
     * @return
     */
    @LayoutRes
    fun getToolBarLayoutResId(): Int {
        when (initCommonToolBarBg()) {
            ToolBarView.ToolBarBg.WHITE -> return R.layout.include_common_white_toolbar
            else -> return R.layout.include_common_white_toolbar
        }
    }

    @LayoutRes
    fun getCustomToolBarLayoutResId(): Int = 0

    fun enabledVisibleToolBar(): Boolean = true

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置中心title
     *
     * @param title
     */
    fun setToolBarTitle(title: String): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterText(title)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置中心title
     *
     * @param resId
     */
    fun setToolBarTitle(@StringRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterText(resId)
        } else {
            return null
        }
    }

    fun hasCommonToolBar(): Boolean

    fun getCommonToolBarView(): ToolBarView?

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置中心title颜色
     *
     * @param resId
     */
    fun setToolBarTitleColor(@ColorRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterTextColor(resId)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置中心title颜色
     *
     * @param resId
     */
    fun setToolBarTitleColorInt(@ColorInt resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterTextColorInt(resId)
        } else {
            return null
        }
    }

    fun initCommonToolBarBg(): ToolBarView.ToolBarBg = ToolBarView.ToolBarBg.WHITE

    fun setRightImageScaleType(scaleType: ImageView.ScaleType): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImageScaleType(scaleType)
        } else {
            return null
        }
    }

    fun setRightImage(bm: Bitmap): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImage(bm)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置右侧 图标
     *
     * @param drawable
     */
    fun setToolBarRightImage(@DrawableRes drawable: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImage(drawable)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置右侧 文字
     *
     * @param text
     */
    fun setToolBarRightText(text: String): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightText(text)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置右侧 文字颜色
     *
     * @param text
     */
    fun setToolBarRightTextColor(@ColorRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightTextColor(resId)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置右侧 文字颜色
     *
     * @param text
     */
    fun setToolBarRightTextColorInt(@ColorInt resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightTextColorInt(resId)
        } else {
            return null
        }
    }

    /**
     * enabledCommonToolBar 为true时有效。
     * 设置右侧 文字
     *
     * @param resId
     */
    fun setToolBarRightText(@StringRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightText(resId)
        } else {
            return null
        }
    }


    fun setToolBarViewVisible(isVisible: Boolean, vararg events: ToolBarView.ViewType): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setToolBarViewVisible(isVisible, *events)
        } else {
            return null
        }
    }

    fun setToolBarBottomLineVisible(isVisible: Boolean): ToolBarView? {
        return if (hasCommonToolBar()) {
            getCommonToolBarView()?.showBottomLine(isVisible)
        } else null
    }

}