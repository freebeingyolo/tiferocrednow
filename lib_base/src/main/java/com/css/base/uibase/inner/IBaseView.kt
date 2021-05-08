package com.css.base.uibase.inner

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.*
import com.css.base.view.ToolBarView


/**
 * 用户页面, 操作页面，对应Activity,fragment统一接口层
 * @author Ruis
 * @date 2021/5/6
 */
interface IBaseView : ICoreView {


    /**
     * 初始化UI
     *
     * @param rootView
     * @param savedInstanceState
     */
    fun initView( savedInstanceState: Bundle?)

    /**
     * 初始化数据，在InitView后调用
     */
    fun initData()

    fun initLazyData()

    /**
     * activity和fragment
     * 统一处理返回
     */
    fun onBackPressed()

}