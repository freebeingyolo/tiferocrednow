package com.css.wondercorefit.ui.activity.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.BaseActivity
import com.css.base.utils.DownloadUtil
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.service.data.UpGradeData
import com.css.service.router.ARouterConst
import com.css.wondercorefit.databinding.ActivityAboutUsBinding
import com.css.wondercorefit.viewmodel.AboutUsViewModel
import razerdp.basepopup.BasePopupWindow
import java.io.File

@Suppress("DEPRECATION")
open class AboutUsActivity : BaseActivity<AboutUsViewModel, ActivityAboutUsBinding>(),
    View.OnClickListener {
    private val INSTALL_PERMISS_CODE: Int = 0

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
        mViewModel.upGradeData.observe(this, Observer {
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
                            val upgradeUrl = it.upgradePackage
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

    private fun installApk(apkFile: File) {
        Log.d("555", "installApk   $apkFile    ")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d("555", "enter   installApk ")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val contentUri = FileProvider.getUriForFile(this, "$packageName.FileProvider", apkFile)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            Log.d("555", "enter   installApk  and sdk < N")
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        }
        Log.d("555", "enter   installApk and start activity")
        startActivity(intent)
    }

    private fun toInstallPermissionSettingIntent() {

        Log.d("555", "enter toInstallPermissionSettingIntent")
        val packageURI = Uri.parse("package:$packageName")
        val intent =
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)

        Log.d("555", "start settings" + " toInstallPermissionSettingIntent")
        startActivityForResult(intent, INSTALL_PERMISS_CODE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {

            Toast.makeText(this, "安装权限已开启，请重新点击检查更新", Toast.LENGTH_SHORT).show()
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