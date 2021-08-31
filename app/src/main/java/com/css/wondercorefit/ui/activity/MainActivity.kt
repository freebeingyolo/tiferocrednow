package com.css.wondercorefit.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.AppUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.utils.DownloadUtil
import com.css.service.BuildConfig
import com.css.service.data.UpGradeData
import com.css.service.inner.BaseInner
import com.css.service.router.ARouterConst
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityMainBinding
import com.css.wondercorefit.ui.fragment.CourseFragment
import com.css.wondercorefit.ui.fragment.MainFragment
import com.css.wondercorefit.ui.fragment.MallFragment
import com.css.wondercorefit.ui.fragment.SettingFragment
import com.css.wondercorefit.viewmodel.MainActivityViewModel
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta.installApk
import java.io.File

@Route(path = ARouterConst.PATH_APP_MAIN)
class MainActivity : BaseActivity<MainActivityViewModel, ActivityMainBinding>() {
    private var mCurFragment: Fragment? = null
    private lateinit var mTabMainFragment: MainFragment
    lateinit var mTabCourseFragment: CourseFragment
    private lateinit var mTabMallFragment: MallFragment
    private lateinit var mTabSettingFragment: SettingFragment

    private val INSTALL_PERMISS_CODE: Int = 0

    override fun initViewModel(): MainActivityViewModel =
        ViewModelProvider(this).get(MainActivityViewModel::class.java)


    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Bugly.init(applicationContext, "718b817297", true)
        } else {
            Bugly.init(applicationContext, "718b817297", false)
        }
        initTablayout()
    }

    override fun initData() {
        super.initData()
        mViewModel.getUpGrade()
        mViewModel.fetchRemoteWeight()
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.upGradeData.observe(this, {
            if (!AppUtils.getAppVersionName().equals(it.version)) {
                CommonAlertDialog(this).apply {
                    gravity = Gravity.BOTTOM
                    type = CommonAlertDialog.DialogType.Confirm
                    title = "检测更新"
                    content = "检测到新版本${it.version}\n更新内容：\n${it.updateContent}"
                    leftBtnText = "暂不更新"
                    rightBtnText = "立即更新"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)
                            //更新操作
                            startUpgrade(it)
                        }
                    }
                }.show()
            }
        })
    }

    private fun startUpgrade(it: UpGradeData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasInstallPermission: Boolean = isHasInstallPermissionWithO(this)
            if (!hasInstallPermission) {
                Log.d("555", "enter hasInstallPermission   false")
                //弹框提示用户手动打开
                CommonAlertDialog(this).apply {
                    gravity = Gravity.CENTER
                    type = CommonAlertDialog.DialogType.Confirm
                    title = "安装权限"
                    content = "需要打开允许来自此来源，请去设置中开启此权限"
                    rightBtnText = "确认"
                    leftBtnText = "取消"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)
                            toInstallPermissionSettingIntent()
                        }

                        override fun onLeftBtnClick(view: View) {
                            super.onLeftBtnClick(view)
                            return
                        }
                    }
                }.show()
            } else {
                getFileFromServer(it.upgradePackage)
            }
        }
    }

    fun getFileFromServer(downUrl: String?) {
        DownloadUtil.download(this, downUrl, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(file: File) {
                Log.d("555", "onDownload   success ")
                installApk(file)
            }

            override fun onDownloading(progress: Int) {
                Log.d("555", "onDownloading$progress")
            }

            override fun onDownloadFailed() {
                Log.d("555", "onDownloadFailed")
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isHasInstallPermissionWithO(context: Context?): Boolean {
        return context?.packageManager?.canRequestPackageInstalls() ?: false
    }

    private fun toInstallPermissionSettingIntent() {
        val packageURI = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        startActivityForResult(intent, INSTALL_PERMISS_CODE)
    }

    private fun initTablayout() {
        mTabMainFragment = MainFragment()
        mTabCourseFragment = CourseFragment()
        mTabMallFragment = MallFragment()
        mTabSettingFragment = SettingFragment()
        mViewBinding.tablayout.initTab(callback = {
            mViewBinding.tablayout.tag = it
/*            val fragment = getFragment(it)
            when (it) {
                BaseInner.TabIndex.HOME -> {
                    if (mCurFragment == fragment) {
                        mTabMainFragment.scrollTopRefresh()
                    }

                }
                BaseInner.TabIndex.MALL -> {
                    if (curFragment == fragment) {
                        tabMallFragment.scrollTopRefresh()
                    }

                }
                BaseInner.TabIndex.COURSE -> {
                    if (curFragment == fragment) {
                        tabCartFragment.scrollTopRefresh()
                    }

                }
                BaseInner.TabIndex.SETTING -> {
                    if (curFragment == fragment) {
                        tabMyFragment.scrollTopRefresh()
                    }

                }
            }*/
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