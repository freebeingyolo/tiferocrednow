package com.css.wondercorefit.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.utils.DownloadUtil
import com.css.service.BuildConfig
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
import kotlinx.coroutines.launch
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
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.upGradeData.observe(this, {
            val versionName = it.version
            val packVersion = AppUtils.getAppVersionName()
            if (packVersion != versionName) {
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
                            getFileFromServer(it.upgradePackage)
                        }

                        override fun onLeftBtnClick(view: View) {
                            super.onLeftBtnClick(view)
                            if (it.mandatoryUpgrade == "是") {
                                ActivityUtils.finishAllActivities()
                            }
                        }
                    }
                }.show()
            }
        })
    }

    private fun getFileFromServer(downUrl: String) {
        DownloadUtil.download(downUrl, object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(file: File) {
                lifecycleScope.launch {
                    showToast("升级包下载完成，开始安装")
                    startInstallApk(file)
                }
            }

            override fun onDownloading(progress: Int) {
            }

            override fun onDownloadFailed() {
                lifecycleScope.launch {
                    showToast("升级包下载失败")
                }
            }
        })
        /*var call: Call? = null
        var dlg: ProgressDialog? = null
        var dlLis: DownloadUtil.OnDownloadListener? = null
        val lambda_cancel: (DialogInterface, Int) -> Unit = { _, _ ->
            call?.cancel()
            ActivityUtils.finishAllActivities()
        }
        val lambda_retry: (DialogInterface, Int) -> Unit = { _, _ ->
            getFileFromServer(downUrl)
        }
        dlLis = object : DownloadUtil.OnDownloadListener {
            override fun onDownloadSuccess(file: File) {
                LogUtils.d(TAG, "onDownloadSuccess:start to install,isMain:")
                lifecycleScope.launch {
                    startInstallApk(file)
                    showToast("下载成功，开始安装...")
                    dlg!!.dismiss()
                }
            }

            override fun onDownloading(progress: Int) {
                LogUtils.d(TAG, "onDownloading:$progress,isMain:" + (Looper.myLooper() == Looper.getMainLooper()))
                lifecycleScope.launch {
                    dlg!!.progress = progress
                }
            }

            override fun onDownloadFailed() {
                LogUtils.d(TAG, "onDownloadFailed,isMain:" + (Looper.myLooper() == Looper.getMainLooper()))
                lifecycleScope.launch {
                    showToast("下载升级包失败,请重试")
                    dlg!!.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.VISIBLE
                }
            }
        }

        dlg = ProgressDialog(this).apply {
            setTitle("温馨提示")
            setMessage("正在下载...")
            setCancelable(false)
            setButton(DialogInterface.BUTTON_NEGATIVE, "重试", lambda_retry)
            setButton(DialogInterface.BUTTON_POSITIVE, "取消", lambda_cancel)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        call = DownloadUtil.download(downUrl, dlLis)
        dlg.setOnShowListener { dlg.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.INVISIBLE }
        dlg.show()*/

    }

    // 下载成功，开始安装,兼容8.0安装位置来源的权限
    private fun startInstallApk(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //是否有安装位置来源的权限
            val hasInstallPermission: Boolean = isHasInstallPermissionWithO(this)
            if (hasInstallPermission) {
                // "8.0手机已经拥有安装未知来源应用的权限，直接安装！"
                AppUtils.installApp(apkFile)
            } else {
                toInstallPermissionSettingIntent()
            }
        } else {
            AppUtils.installApp(apkFile)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isHasInstallPermissionWithO(context: Context?): Boolean {
        return context?.packageManager?.canRequestPackageInstalls() ?: false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_PERMISS_CODE) {
            val successDownloadApkPath =
                "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/".trim() + "WonderCoreFit.apk"
            AppUtils.installApp(successDownloadApkPath)
        }
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
            /*val fragment = getFragment(it)
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

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater, parent, false)
}