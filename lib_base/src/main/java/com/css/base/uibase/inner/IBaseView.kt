package com.css.base.uibase.inner

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.*
import com.css.base.R
import com.css.base.view.ToolBarView


/**
 * 用户页面, 操作页面，对应Activity,fragment统一接口层
 * @author Ruis
 * @date 2021/5/6
 */
interface IBaseView : ICoreView, IToolbarView {

    /* 初始化UI
    *
    * @param rootView
    * @param savedInstanceState
    */
    fun initView(savedInstanceState: Bundle?)

    /**
     * 初始化数据，在InitView后调用
     */
    fun initData()

    fun initLazyData()

    /**
     * 根视图View
     *
     * @return
     */
    fun getRootView(): View

    /**
     * activity和fragment
     * 统一处理返回
     */
    fun onBackPressed()

    /**
     * 若用户使用自定义toolbar，而且还想使用statusLayout，则需要重写此方法，返回statusLayout覆盖的内容区域resId
     *
     * @return
     */
    @IdRes
    fun getCoverStatusLayoutResId(): Int = 0

}