package com.css.wondercorefit.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.SPUtils
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.inner.BaseInner
import com.css.step.ISportStepInterface
import com.css.step.TodayStepManager
import com.css.step.TodayStepService
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityMainBinding
import com.css.wondercorefit.ui.fragment.CourseFragment
import com.css.wondercorefit.ui.fragment.MainFragment
import com.css.wondercorefit.ui.fragment.MallFragment
import com.css.wondercorefit.ui.fragment.SettingFragment

class MainActivity : BaseActivity<DefaultViewModel,ActivityMainBinding>() {
    private var mCurFragment: Fragment? = null
    private lateinit var mTabMainFragment: MainFragment
    lateinit var mTabCourseFragment: CourseFragment
    private lateinit var mTabMallFragment: MallFragment
    private lateinit var mTabSettingFragment: SettingFragment

    private var iSportStepInterface: ISportStepInterface? = null
    private lateinit var stepArray:String
    private val mDelayHandler = Handler(TodayStepCounterCall())
    private val REFRESH_STEP_WHAT = 0
    private val TIME_INTERVAL_REFRESH: Long = 500

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initView( savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setWhiteFakeStatus(R.id.cl_parent,false)
        initTablayout()
        startStep()
    }

    private fun initTablayout() {
        mTabMainFragment = MainFragment()
        mTabCourseFragment = CourseFragment()
        mTabMallFragment = MallFragment()
        mTabSettingFragment = SettingFragment()
        mViewBinding.tablayout.initTab(callback = {
           mViewBinding.tablayout.tag = it
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

    private fun startStep() {
        TodayStepManager().init(application)
        //开启计步Service，同时绑定Activity进行aidl通信
    }

    inner class TodayStepCounterCall : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                REFRESH_STEP_WHAT -> {

                    //每隔500毫秒获取一次计步数据刷新UI
                    if (null != iSportStepInterface) {
                        var step: String? = null
                        try {
                            step = iSportStepInterface!!.todaySportStepArray
                        } catch (e: RemoteException) {
                            e.printStackTrace()
                        }
                        if (stepArray != step) {
                            stepArray = step.toString()
//                            updateStepCount(stepArray)
                        }
                    }
                    mDelayHandler.sendEmptyMessageDelayed(
                        REFRESH_STEP_WHAT,
                        TIME_INTERVAL_REFRESH
                    )
                }
            }
            return false
        }
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

    override fun initViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
}