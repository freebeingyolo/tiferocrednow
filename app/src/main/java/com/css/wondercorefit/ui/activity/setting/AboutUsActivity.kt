package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.FragmentUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.blankj.utilcode.util.AppUtils
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.wondercorefit.databinding.ActivityAboutUsBinding
import com.css.wondercorefit.viewmodel.AboutUsViewModel

class AboutUsActivity : BaseActivity<AboutUsViewModel, ActivityAboutUsBinding>(),
    View.OnClickListener {
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
        mViewBinding.rlCheckUpdate.setOnClickListener(this)
    }

    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.upGradeData.observe(this, {
            CommonAlertDialog(this).apply {
                gravity = Gravity.BOTTOM
                type = CommonAlertDialog.DialogType.Confirm
                title = "检测更新?"
                content = "检测到新版本${it.version}\n更新内容：\n${it.updateContent}"
                leftBtnText = "暂不更新"
                rightBtnText = "立即更新"
                listener = object : DialogClickListener.DefaultLisener() {
                    override fun onRightBtnClick(view: View) {
                        super.onRightBtnClick(view)
                        //更新操作
                    }
                }
            }.show()
        })
    }

    override fun initData() {
        super.initData()
        mViewBinding.tvVersion.text = "V${AppUtils.getAppVersionName()}"
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
                CommonAlertDialog(baseContext).apply {
                    type = CommonAlertDialog.DialogType.Confirm
                    gravity = Gravity.BOTTOM
                    title = "检测更新"
                    content = "检测到新版本$"
                    leftBtnText = "暂不更新"
                    rightBtnText = "立即更新"
                    listener = object : DialogClickListener.DefaultLisener() {
                        override fun onRightBtnClick(view: View) {
                            super.onRightBtnClick(view)

                        }
                    }
                }.show()
            }
            mViewBinding.rlCheckUpdate -> {
                mViewModel.getUpGrade()
            }
        }
    }

}