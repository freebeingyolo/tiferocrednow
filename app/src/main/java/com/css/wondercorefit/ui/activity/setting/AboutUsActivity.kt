package com.css.wondercorefit.ui.activity.setting

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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.utils.DownloadUtil
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.wondercorefit.databinding.ActivityAboutUsBinding
import com.css.wondercorefit.viewmodel.AboutUsViewModel
import kotlinx.coroutines.launch
import razerdp.basepopup.BasePopupWindow
import java.io.File

@Suppress("DEPRECATION")
open class AboutUsActivity : BaseActivity<AboutUsViewModel, ActivityAboutUsBinding>(),
    View.OnClickListener {
    private val INSTALL_PERMISS_CODE: Int = 0
//    private val callbackMap: ConcurrentHashMap<DownloadInfo, DownloadCallback> =
//        ConcurrentHashMap<DownloadInfo, DownloadCallback>(5)

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, AboutUsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.WHITE
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("关于我们")
        mViewBinding.rlTermsService.setOnClickListener(this)
        mViewBinding.rlTermsLiability.setOnClickListener(this)
        mViewBinding.rlTermsPrivacy.setOnClickListener(this)
        mViewBinding.rlCheckUpdate.setOnClickListener(this)
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.upGradeData.observe(this, {
            val version = it.version
            val packVersion = AppUtils.getAppVersionName()
            if (packVersion != version) {
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
            } else {
                Toast.makeText(this, "当前为最新版本，不需要更新", Toast.LENGTH_SHORT).show()
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

    private fun toInstallPermissionSettingIntent() {
        val packageURI = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
        startActivityForResult(intent, INSTALL_PERMISS_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_PERMISS_CODE) {
            val successDownloadApkPath =
                "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/".trim() + "WonderCoreFit.apk"
            AppUtils.installApp(successDownloadApkPath)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isHasInstallPermissionWithO(context: Context?): Boolean {
        return context?.packageManager?.canRequestPackageInstalls() ?: false
    }


    override fun initData() {
        super.initData()
        mViewBinding.tvVersion.text = "V${AppUtils.getAppVersionName()}"
        mViewBinding.tvVersion1.text = "V${AppUtils.getAppVersionName()}"
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initViewModel(): AboutUsViewModel =
        ViewModelProvider(this).get(AboutUsViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityAboutUsBinding =
        ActivityAboutUsBinding.inflate(layoutInflater, parent, false)

    override fun onClick(v: View) {
        when (v) {
            mViewBinding.rlTermsService -> {
                TermsActivity.starActivity(this, TermsActivity.TERMS_SERVICE)
            }
            mViewBinding.rlTermsLiability -> {
                TermsActivity.starActivity(this, TermsActivity.TERMS_LIABILITY)
            }
            mViewBinding.rlTermsPrivacy -> {
                TermsActivity.starActivity(this, TermsActivity.TERMS_PRIVACY)
            }
            mViewBinding.rlCheckUpdate -> {
                if (NetworkUtils.isConnected()) {
                    mViewModel.getUpGrade()
                } else {
                    CommonAlertDialog(this).apply {
                        type = CommonAlertDialog.DialogType.Image
                        imageResources = R.mipmap.icon_error
                        content = getString(R.string.network_error)
                        onDismissListener = object : BasePopupWindow.OnDismissListener() {
                            override fun onDismiss() {

                            }
                        }
                    }.show()
                }
            }
        }
    }
}