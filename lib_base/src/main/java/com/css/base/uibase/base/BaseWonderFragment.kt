package com.css.base.uibase.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.R
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.IBaseView
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.base.utils.FragmentStarter
import com.css.base.view.ToolBarView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

abstract class BaseWonderFragment<VM : BaseViewModel, VB : ViewBinding> : Fragment(), IBaseView, OnToolBarClickListener {
    lateinit var mViewModel: VM

    var mViewBinding: VB? = null

    private var mViewRef: Reference<Activity>? = null

    /**
     * 在使用自定义toolbar时候的根布局 =toolBarView+childView
     */
    private var mRootView: View? = null

    /**
     * 顶部 bar，若使用通用toolbar则为ToolBarView。否则为自定义的top bar view
     */
    private var mTopBarView: View? = null


    /**
     * 通用的自定义toolbar，填充内容
     */
    protected var mChildContainerLayout: FrameLayout? = null

    /**
     * 通用的自定义toolbar
     */
    private var mCommonToolbarView: ToolBarView? = null

    private var isViewDestroy = false

    /**
     * user visible
     */
    private var isFVisible = false

    /**
     * initLazyData调用方法
     */
    private var isInitLazyData = false

    private var isAttachViewModelOk = false

    /**
     * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
     * Use onAttachToContext instead
     */
    @TargetApi(23)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onAttachToContext(context)
        }
    }

    /**
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity)
        }
    }

    private fun onAttachToContext(context: Context?) {

        mViewRef = WeakReference(context as Activity)
        attachViewModelAndLifecycle()

    }

    private fun attachViewModelAndLifecycle() {
        mViewModel = initViewModel()
        lifecycle.addObserver(mViewModel)
        isAttachViewModelOk = true
    }

    abstract fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRootView == null) {
            //为空时初始化。
            //根布局
            mRootView = inflater.inflate(R.layout.activity_base, viewGroup, false)
            //toolbar容器
            val toolbarVs = mRootView!!.findViewById<ViewStub>(R.id.vs_toolbar)
            if (enabledVisibleToolBar()) {
                val toolbarLayoutId =
                    if (isShowCustomToolbar()) {
                        getCustomToolBarLayoutResId()
                    } else {
                        getToolBarLayoutResId()
                    }
                //toolbar资源id
                toolbarVs.layoutResource = toolbarLayoutId
                //填充toolbar
                mTopBarView = toolbarVs.inflate()
            }
            //子布局容器
            mChildContainerLayout = mRootView!!.findViewById(R.id.fl_container)
            mChildContainerLayout!!.apply {
                //子布局
                mViewBinding = initViewBinding(inflater, mChildContainerLayout)
                if (!contains(mViewBinding!!.root)) {
                    addView(mViewBinding!!.root)
                }
            }
        }
        return mRootView
    }

    //--------------------toolbar start setting -----------------------------
    /**
     * 初始化toolbar可重写覆盖自定的toolbar,base中实现的是通用的toolbar
     */
    open fun initCommonToolBar() {
        mCommonToolbarView = mRootView!!.findViewById(R.id.toolBarView) as ToolBarView?
        if (!hasCommonToolBar()) {
            return
        }
        mCommonToolbarView!!.setToolBarClickListener(this)
        //支持默认返回按钮和事件
        setToolBarViewVisible(enabledDefaultBack(), ToolBarView.ViewType.LEFT_IMAGE)
    }

    protected open fun hasCommonToolBar(): Boolean {
        return mCommonToolbarView != null
    }

    /**
     * 是过滤器显示通用toolBar
     *
     * @return
     */
    private fun isShowCustomToolbar(): Boolean {
        return getCustomToolBarLayoutResId() != 0
    }

    override fun setToolBarTitle(title: String): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterText(title)
        } else {
            return null
        }
    }

    override fun setToolBarTitle(@StringRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterText(resId)
        } else {
            return null
        }
    }

    override fun setToolBarTitleColor(@ColorRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterTextColor(resId)
        } else {
            return null
        }
    }

    override fun setToolBarTitleColorInt(@ColorInt resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setCenterTextColorInt(resId)
        } else {
            return null
        }
    }

    override fun setRightImageScaleType(scaleType: ImageView.ScaleType): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImageScaleType(scaleType)
        } else {
            return null
        }
    }

    override fun setRightImage(bm: Bitmap): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImage(bm)
        } else {
            return null
        }
    }

    override fun setToolBarRightImage(@DrawableRes drawable: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightImage(drawable)
        } else {
            return null
        }
    }

    override fun setToolBarRightText(@StringRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightText(resId)
        } else {
            return null
        }
    }

    override fun setToolBarRightText(text: String): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightText(text)
        } else {
            return null
        }
    }

    override fun setToolBarRightTextColor(@ColorRes resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightTextColor(resId)
        } else {
            return null
        }
    }

    override fun setToolBarRightTextColorInt(@ColorInt resId: Int): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setRightTextColorInt(resId)
        } else {
            return null
        }
    }

    override fun setToolBarBottomLineVisible(isVisible: Boolean): ToolBarView? {
        return if (hasCommonToolBar()) {
            getCommonToolBarView()?.showBottomLine(isVisible)
        } else null
    }

    override fun setToolBarViewVisible(
        isVisible: Boolean,
        vararg events: ToolBarView.ViewType
    ): ToolBarView? {
        if (hasCommonToolBar()) {
            return getCommonToolBarView()?.setToolBarViewVisible(isVisible, *events)
        } else {
            return null
        }
    }

    /**
     * 顶部toolbar，自定义view或通用toolbar
     *
     * @return
     */
    protected open fun getTopToolBar(): View? {
        return mTopBarView
    }

    protected open fun getCommonToolBarView(): ToolBarView? {
        return mCommonToolbarView
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

    //--------------------toolbar end setting -----------------------------
    //----------------------- 沉浸式 ImmersionBar start ------------------------
    private fun canUsedImmersionBar(): Boolean {
        return if (getBaseActivity() != null && getBaseActivity() is BaseActivity<*, *>) {
            (getBaseActivity() as BaseActivity<*, *>).enabledImmersion() && enabledImmersion()
        } else false
    }

    open fun initImmersionBar() {
        if (getBaseActivity() == null) {
            return
        }
        if (!canUsedImmersionBar()) {
            return
        }
        val baseActivity = getBaseActivity() as BaseWonderActivity<*, *>
        if (enabledVisibleToolBar()) {
            when (initCommonToolBarBg()) {
                ToolBarView.ToolBarBg.WHITE -> baseActivity.setWhiteFakeStatus(
                    R.id.ll_base_root,
                    enbaleFixImmersionAndEditBug()
                )
            }
        } else {
            baseActivity.setTransparentStatus(R.id.ll_base_root, enbaleFixImmersionAndEditBug())
        }
    }

    //----------------------- 沉浸式 ImmersionBar end ------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isActivityDestroyed()) {
            log("onViewCreated fragment刚创建，Activity就destroy了！")
            return
        }
        initImmersionBar()
        if (enabledVisibleToolBar()) {
            initCommonToolBar()
        }
        initView(savedInstanceState)
        initUIChangeLiveDataCallBack()
        initData()

    }

    abstract fun initViewModel(): VM

    /**
     * 运行在initView之后
     * 此时已经setContentView
     * 可以做一些初始化操作
     */
    override fun initView(savedInstanceState: Bundle?) {
    }

    /**
     * 注册ViewModel与View的契约UI回调事件
     */
    private fun initUIChangeLiveDataCallBack() {

        //Toast
        mViewModel.showToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showToast(t)
        })
        mViewModel.showLongToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showLongToast(t)
        })
        mViewModel.showToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showToast(t)
        })
        mViewModel.showLongToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showLongToast(t)
        })
        mViewModel.showCenterToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterToast(t)
        })
        mViewModel.showCenterLongToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterLongToast(t)
        })
        mViewModel.showCenterToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterToast(t)
        })
        mViewModel.showCenterLongToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterLongToast(t)
        })
        mViewModel.finishAcEvent.observe(viewLifecycleOwner, Observer {
            finishAc()
        })
        registorUIChangeLiveDataCallBack()
    }

    open fun registorUIChangeLiveDataCallBack() {

    }

    override fun initData() {
    }

    override fun finishAc() {
        activity?.finish()
    }

    /**
     * 判断Activity是否被销毁
     *
     * @return
     */
    private fun isActivityDestroyed(): Boolean {
        val activity = activity
        return if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activity.isDestroyed
            } else {
                activity.isFinishing
            }
        } else false
    }

    override fun onResume() {
        super.onResume()
        try {
            log("fragment onResume:" + super.isResumed() + ";isVisible:" + super.isVisible() + ";isHidden:" + super.isHidden() + ";getUserVisibleHint:" + super.getUserVisibleHint() + ";isAdded:" + super.isAdded())
            /**
             * 当调用onResume时，可能会出现对用户不可以。使用isVisible失效
             */
            setUserVisible(userVisibleHint)
        } catch (e: Throwable) {

        }
    }

    /**
     * fragment第一次初始化时，在onAttach前调用，而此时presenter未初始化。
     * 所以，需要在onResume方法时，调用setUserVisible(true);
     *
     * @param isVisibleToUser
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        try {
            log("fragment visible setUserVisibleHint:" + super.isResumed() + ";" + super.isVisible() + ";" + super.isHidden() + ";" + super.getUserVisibleHint() + ";" + super.isAdded())
            setUserVisible(isVisibleToUser)
        } catch (e: Throwable) {

        }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        try {
            log("fragment visible onHiddenChanged:" + super.isResumed() + ";" + super.isVisible() + ";" + super.isHidden() + ";" + super.getUserVisibleHint() + ";" + super.isAdded())
            setUserVisible(!hidden)
        } catch (e: Throwable) {

        }
    }

    override fun onPause() {
        super.onPause()
        try {
            log("fragment visible onPause:" + super.isResumed() + ";" + super.isVisible() + ";" + super.isHidden() + ";" + super.getUserVisibleHint() + ";" + super.isAdded())
            setUserVisible(false)
        } catch (e: Throwable) {

        }
    }

    @Synchronized
    private fun setUserVisible(isUserVisible: Boolean) {
        //第一次启动fragment，viewModel为空.需要能过onResume来实现visible调用
        if (!isAttachViewModelOk) {
            return
        }
        log("[setUserVisible]isUserVisible:[$isUserVisible]")
        if (isUserVisible) {
            if (!isFVisible) {
                isFVisible = true
                onUserVisible()
            }
        } else {
            if (isFVisible) {
                isFVisible = false
                onUserVisibleHint()
            }
        }
    }

    override fun onBackPressed() {
        try {
            if (getBaseActivity() != null) {
                if (getBaseActivity() is BaseWonderActivity<*, *>) {
                    getBaseActivity()!!.onBackPressed()
                }
            }
        } catch (e: Throwable) {

        }
    }

    /**
     * 相当于Fragment的onResume
     * notice:页面显示时调用
     */
    private fun onUserVisible() {
        log("[onUserVisible]")
        onVisible()
    }

    /**
     * 相当于Fragment的onPause
     */
    private fun onUserVisibleHint() {
        log("[onUserVisibleHint]")
        onInVisible()
    }

    /**
     * 页面隐藏时调用。与onVisible相反
     */
    protected open fun onInVisible() {}

    /**
     * 页面显示时调用。注意和InitView区分.可做页面显示时更新UI,或加载数据
     */
    protected open fun onVisible() {}

    override fun initLazyData() {
    }


    open fun isViewDestroyed(): Boolean {
        return isViewDestroy
    }


    override fun onDestroyView() {
        super.onDestroyView()
        isFVisible = false
        mViewBinding = null
    }

    override fun getRootView(): View {
        return mRootView!!
    }

    override fun onDestroy() {
        this.isViewDestroy = true
        super.onDestroy()
    }

    private fun log(args: String) {
        LogUtils.i("BaseFragment", this.javaClass.simpleName + " " + args)
    }

    open fun processBackPressed(): Boolean {
        return false
    }

    open fun getBaseActivity(): Activity? {
        if (mViewRef != null) {
            return mViewRef!!.get()
        } else {
            return null
        }
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

    /**
     * 创建fragment时传入的数据对象
     */
    private var mDataIn: Any? = null

    open fun getDataIn(): Any? {
        return mDataIn
    }

    fun onEnterWithData(data: Any?) {
        mDataIn = data
    }

    open fun onBackWithData(data: Any?) {

    }

    /**
     * 跳转fragment
     *
     * @param toFragment
     */
    open fun startFragment(toFragment: Fragment?) {
        startFragment(toFragment, null)
    }

    /**
     * @param toFragment 跳转的fragment
     * @param tag        fragment的标签
     */
    open fun startFragment(toFragment: Fragment?, tag: String?) {
        FragmentStarter.startFragment(this, toFragment, tag)
    }
    //--------------------- control fragment end --------------------------
}