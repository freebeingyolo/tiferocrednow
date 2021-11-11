package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.utils.FragmentUtils
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityTermsBinding
import com.css.wondercorefit.ui.fragment.TermsLiabilityFragment
import com.css.wondercorefit.ui.fragment.TermsPrivacyFragment
import com.css.wondercorefit.ui.fragment.TermsServiceFragment

class TermsActivity : BaseActivity<DefaultViewModel, ActivityTermsBinding>() {

    companion object {
        const val TERMS_SERVICE = 1
        const val TERMS_PRIVACY = 2
        const val TERMS_LIABILITY = 3
        var CURRENT = 0
        fun starActivity(context: Context, type: Int) {
            CURRENT = type
            val intent = Intent(context, TermsActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //展示设备列表界面
        when (CURRENT) {
            TERMS_SERVICE -> {
                FragmentUtils.changeFragment(TermsServiceFragment::class.java, FragmentUtils.Option.OPT_REPLACE, R.id.content)
            }
            TERMS_PRIVACY -> {
                FragmentUtils.changeFragment(TermsPrivacyFragment::class.java, FragmentUtils.Option.OPT_REPLACE,R.id.content)
            }
            TERMS_LIABILITY -> {
                FragmentUtils.changeFragment(TermsLiabilityFragment::class.java, FragmentUtils.Option.OPT_REPLACE,R.id.content)
            }
        }

    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityTermsBinding = ActivityTermsBinding.inflate(inflater, parent, false)

}