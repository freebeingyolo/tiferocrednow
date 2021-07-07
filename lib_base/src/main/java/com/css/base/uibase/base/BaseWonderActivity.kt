package com.css.base.uibase.base

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.R
import com.css.base.dialog.LoadingDialog
import com.css.base.dialog.ToastDialog
import com.css.base.uibase.inner.IBaseView
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.utils.FragmentStarter
import com.css.base.utils.OSUtils
import com.css.base.view.ToolBarView

abstract class BaseWonderActivity<VM : BaseViewModel, VB : ViewBinding> : AppCompatActivity(),
    IBaseView, OnToolBarClickListener {
    protected val TAG = this.javaClass.simpleName

    lateinit var mViewModel: VM

    lateinit var mViewBinding: VB

    /**
     * activity is destroyed
     */
    private var isViewDestroy = false

    /**
     * 通用toolBar
     */
    private var mToolbarView: ToolBarView? = null

    /**
     * 顶部 bar，若使用通用toolbar则为ToolBarView。否则为自定义的top bar view
     */
    private var mTopBarView: View? = null

    /**
     * 通用的自定义toolbar，填充内容
     */
    protected var mChildContainerLayout: FrameLayout? = null

    /**
     * 默认显示，当不显示时，设置为false
     * 用户控件dialog的显示，注意，不能onResume判断，因为有些页面在初始化时，就要弹出dialog加载进度条。
     */
    private var isActivityVisible = true

    /**
     * 在使用自定义toolbar时候的根布局 =toolBarView+childView
     */
    private var mRootView: View? = null
     lateinit var mLoadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachViewModelAndLifecycle()
        initContentView()
        initImmersionBar()
        initCommonToolBar()
        initView(savedInstanceState)
        initUIChangeLiveDataCallBack()
        initData()
        postInitLazyData()

    }

    override fun enabledVisibleToolBar() = false

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
        mViewModel.showLoadingEvent.observe(this, Observer {
            showLoading()
        })
        mViewModel.hideLoadingEvent.observe(this, Observer {
            hideLoading()
        })
        registorUIChangeLiveDataCallBack()
    }

    open fun registorUIChangeLiveDataCallBack() {

    }

    override fun initData() {
    }

    override fun initLazyData() {
    }

    private fun postInitLazyData() {
        if (getRootView() != null) {
            val runnable = Runnable {
                if (!isFinishing && !isViewDestroyed()) {
                    initLazyData()
                }
            }
            getRootView().post(runnable)
        }
    }

    open fun isViewDestroyed(): Boolean {
        return isViewDestroy
    }

    private fun attachViewModelAndLifecycle() {
        mViewModel = initViewModel()
        lifecycle.addObserver(mViewModel)
    }

    abstract fun initViewModel(): VM

    abstract fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): VB


    private fun initContentView() {
        if (mRootView == null) {
            //根布局
            mRootView = LayoutInflater.from(this).inflate(R.layout.activity_base, null, false)
            //toolbar容器
            val toolbarVs = mRootView!!.findViewById<ViewStub>(R.id.vs_toolbar)
            //子布局容器
            if (enabledVisibleToolBar()) {
                val toolbarId =
                    if (isShowCustomToolbar()) getCustomToolBarLayoutResId() else getToolBarLayoutResId()
                //toolbar资源id
                toolbarVs.layoutResource = toolbarId
                //填充toolbar
                mTopBarView = toolbarVs.inflate()
            }
            mChildContainerLayout = mRootView!!.findViewById(R.id.fl_container)
            mChildContainerLayout!!.apply {
                //子布局
                mViewBinding = initViewBinding(layoutInflater, mChildContainerLayout)
                if (!contains(mViewBinding.root)) {
                    addView(mViewBinding.root)
                }
            }
        }
        super.setContentView(mRootView)
    }

    /**
     * 是过滤器显示通用toolBar
     * @return
     */
    private fun isShowCustomToolbar(): Boolean {
        return getCustomToolBarLayoutResId() != 0
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
        var enableBackPressed = true
        //说明，只有当通过pushFragmentToBackStack方式调用，才会回调processBackPressed方法。
        if (mCurrentFragment != null) {
            enableBackPressed = !mCurrentFragment!!.processBackPressed()
        }
        //log(" getBackStackEntryCount  " + supportFragmentManager.backStackEntryCount + "   " + enableBackPressed + ";mCurrentFragment:" + mCurrentFragment)

        if (!enableBackPressed) {
            return
        }
        val cnt = supportFragmentManager.backStackEntryCount
        if (cnt < 1 && isTaskRoot) {
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
        if (count < 1) {
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

    override fun showLoading() {
        mLoadingDialog = LoadingDialog(this)
        mLoadingDialog.showPopupWindow()
    }

    override fun hideLoading() {
        if (mLoadingDialog != null){
            mLoadingDialog.dismiss()
        }
    }

    override fun finishAc() {
        finish()
    }

    //------------------toolbar start setting-----------------------------------

    override fun hasCommonToolBar(): Boolean {
        return mToolbarView != null
    }

    /**
     * 初始化toolbar可重写覆盖自定的toolbar,base中实现的是通用的toolbar
     */
    private fun initCommonToolBar() { //toolbar
        mToolbarView = findViewById(R.id.toolBarView)
        if (!hasCommonToolBar()) {
            return
        }
        mToolbarView!!.setToolBarClickListener(this)
        //支持默认返回按钮和事件
        setToolBarViewVisible(enabledDefaultBack(), ToolBarView.ViewType.LEFT_IMAGE)
    }

    override fun getCommonToolBarView(): ToolBarView? {
        return mToolbarView
    }

    /**
     * 顶部toolbar，自定义view或通用toolbar
     *
     * @return
     */
    protected open fun getTopToolBar(): View? {
        return mTopBarView
    }

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                if (enabledDefaultBack()) {
                    onBackPressed()
                }
            }
        }
    }

    //------------------ toolbar end ------------


    //------------------------- 沉浸式 ImmersionBar start --------------------


    private fun initImmersionBar() {
        if (enabledVisibleToolBar()) {
            when (initCommonToolBarBg()) {
                ToolBarView.ToolBarBg.WHITE -> setWhiteFakeStatus(
                    R.id.ll_base_root,
                    enbaleFixImmersionAndEditBug()
                )
                ToolBarView.ToolBarBg.GRAY -> setGrayFakeStatus(
                    R.id.ll_base_root,
                    enbaleFixImmersionAndEditBug()
                )
            }
        } else {
            setTransparentStatus(R.id.ll_base_root, enbaleFixImmersionAndEditBug())
        }
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

    open fun setGrayFakeStatus(contentParentViewId: Int, enbaleFixImmersionAndEditBug: Boolean) {
        setFakeStatus(
            contentParentViewId,
            true,
            0,
            R.color.common_gray_color,
            enbaleFixImmersionAndEditBug
        )
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
        mRootView = null
    }

    override fun getRootView(): View {
        return mRootView!!
    }
    //--------------------- control fragment start --------------------------

    private var mCurrentFragment: BaseWonderFragment<*, *>? = null

    open fun getCurrentFragment(): BaseWonderFragment<*, *>? {
        return mCurrentFragment
    }

    /**
     * 添加到 stack中
     *
     * @param containerId
     * @param cls
     * @param data
     */
    open fun pushFragmentToBackStack(
        @IdRes containerId: Int,
        cls: Class<out BaseWonderFragment<*, *>>?,
        data: Any?
    ) {
        mCurrentFragment =
            FragmentStarter.pushFragmentToBackStack(this, containerId, cls, data, true)
        mCloseWarned = false

    }

    open fun pushFragmentToBackStack(cls: Class<out BaseWonderFragment<*, *>>?, data: Any?) {
        pushFragmentToBackStack(getFragmentContainerId(), cls, data)
    }

    open fun popTopFragment(data: Any?) {
        val fm = supportFragmentManager
        fm.popBackStackImmediate()
        if (tryToUpdateCurrentAfterPop() && mCurrentFragment != null) {
            mCurrentFragment!!.onBackWithData(data)
        }
    }

    private fun tryToUpdateCurrentAfterPop(): Boolean {
        val fm = supportFragmentManager
        val cnt = fm.backStackEntryCount
        if (cnt > 0) {
            val name = fm.getBackStackEntryAt(cnt - 1).name
            val fragment = fm.findFragmentByTag(name)
            if (fragment != null && fragment is BaseWonderFragment<*, *>) {
                mCurrentFragment = fragment
            }
            return true
        }
        return false
    }

    protected open fun getFragmentContainerId(): Int {
        return 0
    }
}

