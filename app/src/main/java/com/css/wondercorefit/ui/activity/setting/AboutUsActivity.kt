package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.FragmentUtils
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityAboutUsBinding
import com.css.wondercorefit.ui.fragment.TermsServiceFragment

class AboutUsActivity : BaseActivity<DefaultViewModel, ActivityAboutUsBinding>(),
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
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

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
        }
    }

}