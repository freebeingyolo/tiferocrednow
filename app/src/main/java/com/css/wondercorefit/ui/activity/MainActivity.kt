package com.css.wondercorefit.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.SPUtils
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultYuboViewModel
import com.css.service.inner.BaseInner
import com.css.wondercorefit.R
import com.css.wondercorefit.ui.fragment.CourseFragment
import com.css.wondercorefit.ui.fragment.MainFragment
import com.css.wondercorefit.ui.fragment.MallFragment
import com.css.wondercorefit.ui.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<DefaultYuboViewModel>() {
    private var mCurFragment: Fragment? = null
    private lateinit var mTabMainFragment: MainFragment
    lateinit var mTabCourseFragment: CourseFragment
    private lateinit var mTabMallFragment: MallFragment
    private lateinit var mTabSettingFragment: SettingFragment
    override fun getLayoutResId(): Int = R.layout.activity_main

    override fun initViewModel(): DefaultYuboViewModel =
        ViewModelProvider(this).get(DefaultYuboViewModel::class.java)

    override fun enabledVisibleToolBar(): Boolean = false

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        initTablayout()
    }

    private fun initTablayout() {
        mTabMainFragment = MainFragment()
        mTabCourseFragment = CourseFragment()
        mTabMallFragment = MallFragment()
        mTabSettingFragment = SettingFragment()
        tablayout.initTab(callback = {
            tablayout.tag = it
//            val fragment = getFragment(it)
            when (it) {
                BaseInner.TabIndex.HOME -> {
//                    if (mCurFragment == fragment) {
//                        mTabMainFragment.scrollTopRefresh()
//                    }

                }
                BaseInner.TabIndex.MALL -> {
//                    if (curFragment == fragment) {
//                        tabMallFragment.scrollTopRefresh()
//                    }

                }
                BaseInner.TabIndex.COURSE -> {
//                    if (curFragment == fragment) {
//                        tabCartFragment.scrollTopRefresh()
//                    }

                }
                BaseInner.TabIndex.SETTING -> {
//                    if (curFragment == fragment) {
//                        tabMyFragment.scrollTopRefresh()
//                    }

                }
            }
            changeFragment(it)
        })

    }


    fun changeFragment(tabIndex: Int) {
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

}