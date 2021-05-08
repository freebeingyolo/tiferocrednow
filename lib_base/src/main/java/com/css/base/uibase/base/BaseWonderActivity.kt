package com.css.base.uibase.base

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.R
import com.css.base.uibase.inner.IBaseView
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.utils.OSUtils

abstract class BaseWonderActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity(),
    IBaseView{
    private val TAG = this.javaClass.simpleName

    lateinit var mViewModel: VM

    lateinit var mViewBinding: VB

    /**
     * activity is destroyed
     */
    private var isViewDestroy = false

    /**
     * 默认显示，当不显示时，设置为false
     * 用户控件dialog的显示，注意，不能onResume判断，因为有些页面在初始化时，就要弹出dialog加载进度条。
     */
    private var isActivityVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachViewModelAndLifecycle()
        initContentView()
        initView(savedInstanceState)
        initUIChangeLiveDataCallBack()
        initData()
    }

    /**
     * 注册ViewModel与View的契约UI回调事件
     */
    private fun initUIChangeLiveDataCallBack() {

        //Toast
        mViewModel.showToastStrEvent.observe(this, Observer { t ->
            showToast(t)
        })
        mViewModel.showLongToastStrEvent.observe(this, Observer { t ->
            showLongToast(t)
        })
        mViewModel.showToastResEvent.observe(this, Observer { t ->
            showToast(t)
        })
        mViewModel.showLongToastResEvent.observe(this, Observer { t ->
            showLongToast(t)
        })
        mViewModel.showCenterToastStrEvent.observe(this, Observer { t ->
            showCenterToast(t)
        })
        mViewModel.showCenterLongToastStrEvent.observe(this, Observer { t ->
            showCenterLongToast(t)
        })
        mViewModel.showCenterToastResEvent.observe(this, Observer { t ->
            showCenterToast(t)
        })
        mViewModel.showCenterLongToastResEvent.observe(this, Observer { t ->
            showCenterLongToast(t)
        })
        mViewModel.finishAcEvent.observe(this, Observer {
            finishAc()
        })
        registorUIChangeLiveDataCallBack()
    }

    open fun registorUIChangeLiveDataCallBack() {

    }

    override fun initData() {
    }

    override fun initLazyData() {
    }

    open fun isViewDestroyed(): Boolean {
        return isViewDestroy
    }

    private fun attachViewModelAndLifecycle() {
        mViewModel = initViewModel()
        lifecycle.addObserver(mViewModel)
    }

    abstract fun initViewModel(): VM

    abstract fun initViewBinding(): VB

    private fun initContentView() {
        mViewBinding = initViewBinding()
        super.setContentView(mViewBinding.root)
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    private var closeToast: Toast? = null

    protected open fun processBackPressed(): Boolean {
        return false
    }

    override fun onBackPressed() {
        if (processBackPressed()) {
            return
        }
        back()
    }

    private var mCloseWarned = false

    private fun back() {
        val cnt = supportFragmentManager.backStackEntryCount
        if (cnt <= 1 && isTaskRoot) {
            val closeWarningHint = "再按一次退出程序"
            if (!mCloseWarned && !TextUtils.isEmpty(closeWarningHint)) {
                closeToast =
                    Toast.makeText(applicationContext, closeWarningHint, Toast.LENGTH_SHORT)
                closeToast!!.show()
                mCloseWarned = true
                Handler().postDelayed({ mCloseWarned = false }, 1500)
            } else {
                if (closeToast != null) {
                    closeToast!!.cancel()
                }
                doReturnBack()
            }
        } else {
            mCloseWarned = false
            doReturnBack()
        }
    }

    private fun doReturnBack() {
        val count = supportFragmentManager.backStackEntryCount
        if (count <= 1) {
            finish()
        } else {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    protected open fun log(args: String?) {
        LogUtils.i(TAG, args)
    }

    override fun showToast(msg: String?) {
        msg?.let {
            ToastUtils.setGravity(-1, -1, -1)
            ToastUtils.showShort(it)
        }
    }

    override fun showLongToast(msg: String?) {
        msg?.let {
            ToastUtils.setGravity(-1, -1, -1)
            ToastUtils.showLong(it)
        }
    }

    override fun showToast(resId: Int) {
        ToastUtils.setGravity(-1, -1, -1)
        ToastUtils.showShort(resId)
    }

    override fun showLongToast(resId: Int) {
        ToastUtils.setGravity(-1, -1, -1)
        ToastUtils.showLong(resId)
    }

    override fun showCenterToast(msg: String?) {
        msg?.let {
            ToastUtils.setGravity(Gravity.CENTER, 0, 0)
            ToastUtils.showShort(msg)
        }
    }

    override fun showCenterLongToast(msg: String?) {
        msg?.let {
            ToastUtils.setGravity(Gravity.CENTER, 0, 0)
            ToastUtils.showLong(msg)
        }
    }

    override fun showCenterToast(resId: Int) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.showShort(resId)
    }

    override fun showCenterLongToast(resId: Int) {
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.showLong(resId)
    }

    override fun finishAc() {
        finish()
    }


    open fun setWhiteFakeStatus(contentParentViewId: Int, enbaleFixImmersionAndEditBug: Boolean) {
        setFakeStatus(
            contentParentViewId,
            true,
            0,
            R.color.common_white_color,
            enbaleFixImmersionAndEditBug
        )
        OSUtils.fixWhiteStatusbarBug(this)
    }

    open fun setTransparentStatus(contentParentViewId: Int, enbaleFixImmersionAndEditBug: Boolean) {
        val parentView = findViewById<View>(contentParentViewId)
        if (parentView != null) {
            BarUtils.setStatusBarColor(this, Color.argb(100, 0, 0, 0), false).background = null
            BarUtils.subtractMarginTopEqualStatusBarHeight(parentView)
            BarUtils.setStatusBarLightMode(this, true)
        }
        fixImmersionAndEditBug(enbaleFixImmersionAndEditBug)
    }

    private fun setFakeStatus(
        contentParentViewId: Int,
        isLightMode: Boolean,
        alpha: Int,
        statuBgResource: Int,
        enbaleFixImmersionAndEditBug: Boolean
    ) {
        val parentView = findViewById<View>(contentParentViewId)
        if (parentView != null) {
            BarUtils.setStatusBarColor(this, Color.argb(alpha, 0, 0, 0))
                .setBackgroundResource(statuBgResource)
            BarUtils.addMarginTopEqualStatusBarHeight(parentView)
            BarUtils.setStatusBarLightMode(this, isLightMode)
        }
        fixImmersionAndEditBug(enbaleFixImmersionAndEditBug)
    }

    private fun fixImmersionAndEditBug(enbaleFixImmersionAndEditBug: Boolean) {
        if (enbaleFixImmersionAndEditBug) {
            KeyboardUtils.fixAndroidBug5497(this) //解决沉浸式状态栏与edittext冲突问题
        }
    }

    override fun onResume() {
        super.onResume()
        this.isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        this.isActivityVisible = false
    }

    override fun onDestroy() {
        this.isViewDestroy = true
        this.isActivityVisible = false
        super.onDestroy()

    }

}

