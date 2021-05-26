package com.css.wondercorefit.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.LogUtils
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.service.BuildConfig
import com.css.service.bus.EventMessage
import com.css.service.data.StepData
import com.css.service.inner.BaseInner
import com.css.service.router.ARouterConst
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityMainBinding
import com.css.wondercorefit.ui.fragment.CourseFragment
import com.css.wondercorefit.ui.fragment.MainFragment
import com.css.wondercorefit.ui.fragment.MallFragment
import com.css.wondercorefit.ui.fragment.SettingFragment
import com.tencent.bugly.Bugly
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = ARouterConst.PATH_APP_MAIN)
class MainActivity : BaseActivity<DefaultViewModel, ActivityMainBinding>() {
    private var mCurFragment: Fragment? = null
    private lateinit var mTabMainFragment: MainFragment
    lateinit var mTabCourseFragment: CourseFragment
    private lateinit var mTabMallFragment: MallFragment
    private lateinit var mTabSettingFragment: SettingFragment

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)



    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        EventBus.getDefault().register(this)
        if (BuildConfig.DEBUG) {
            Bugly.init(applicationContext, "718b817297", true)
        } else {
            Bugly.init(applicationContext, "718b817297", false)
        }
        initTablayout()
    }
    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage<*>) {
        when (event.message) {
            EventMessage.Code.MAIN_INDEX_BACK -> {
                LogUtils.vTag("suisui", "2222"+(event.t as StepData).defaultSteps)
            }
        }
//        when (event.type) {
//            MessageType.ShowLog -> {
//                Log.e(TAG, "onMessageEvent: " + event.getString())
//            }
//            MessageType.ShowToast -> {
//                Toast.makeText(activity, "onMessageEvent: " + event.getString(), Toast.LENGTH_SHORT).show()
//            }
//        }
    }
    private fun initTablayout() {
        mTabMainFragment = MainFragment()
        mTabCourseFragment = CourseFragment()
        mTabMallFragment = MallFragment()
        mTabSettingFragment = SettingFragment()
        mViewBinding.tablayout.initTab(callback = {
            mViewBinding.tablayout.tag = it
//            val fragment = getFragment(it)
//            when (it) {
//                BaseInner.TabIndex.HOME -> {
//                    if (mCurFragment == fragment) {
//                        mTabMainFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.MALL -> {
//                    if (curFragment == fragment) {
//                        tabMallFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.COURSE -> {
//                    if (curFragment == fragment) {
//                        tabCartFragment.scrollTopRefresh()
//                    }
//
//                }
//                BaseInner.TabIndex.SETTING -> {
//                    if (curFragment == fragment) {
//                        tabMyFragment.scrollTopRefresh()
//                    }
//
//                }
//            }
            changeFragment(it)
        })

    }

    private fun changeFragment(tabIndex: Int) {
        val fragment = getFragment(tabIndex) ?: return
        if (mCurFragment == fragment) {
            return
        }
        val newFragmentTag = fragment::class.java.simpleName
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        if (mCurFragment != null && !mCurFragment!!.isHidden) {
            ft.hide(mCurFragment!!)
        }
        val fragmentByTag = fragmentManager.findFragmentByTag(newFragmentTag)
        if (fragmentByTag == null) {
            if (!fragment.isAdded) {
                ft.add(R.id.container_main, fragment, newFragmentTag)
            }
        } else {
            ft.show(fragmentByTag)
        }
        ft.commitAllowingStateLoss()
        mCurFragment = fragment
    }

    private fun getFragment(@BaseInner.TabIndex tabIndex: Int): Fragment? {
        when (tabIndex) {
            BaseInner.TabIndex.HOME -> return mTabMainFragment
            BaseInner.TabIndex.COURSE -> return mTabCourseFragment
            BaseInner.TabIndex.MALL -> return mTabMallFragment
            BaseInner.TabIndex.SETTING -> return mTabSettingFragment
        }
        return null
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater, parent, false)
}