package com.css.base.uibase.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.*
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
import com.css.base.utils.UICoreConfig
import com.css.base.view.ToolBarView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

abstract class BaseWonderFragment<VM : BaseViewModel, VB : ViewBinding> : Fragment(), IBaseView{
    lateinit var viewModel: VM

     var mViewBinding: VB ?=null

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
        viewModel = initViewModel()
        lifecycle.addObserver(viewModel)
        isAttachViewModelOk = true
    }

    abstract fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): VB
    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = initViewBinding(inflater, viewGroup)
        return mViewBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isActivityDestroyed()) {
            log("onViewCreated fragment刚创建，Activity就destroy了！")
            return
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
        viewModel.showToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showToast(t)
        })
        viewModel.showLongToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showLongToast(t)
        })
        viewModel.showToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showToast(t)
        })
        viewModel.showLongToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showLongToast(t)
        })
        viewModel.showCenterToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterToast(t)
        })
        viewModel.showCenterLongToastStrEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterLongToast(t)
        })
        viewModel.showCenterToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterToast(t)
        })
        viewModel.showCenterLongToastResEvent.observe(viewLifecycleOwner, Observer { t ->
            showCenterLongToast(t)
        })
        viewModel.finishAcEvent.observe(viewLifecycleOwner, Observer {
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