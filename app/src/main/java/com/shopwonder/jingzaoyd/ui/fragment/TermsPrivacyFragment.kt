package com.shopwonder.jingzaoyd.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.FragmentTermsPrivacyBinding
import com.shopwonder.jingzaoyd.databinding.FragmentTermsServiceBinding

class TermsPrivacyFragment : BaseFragment<DefaultViewModel, FragmentTermsPrivacyBinding>() {
    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentTermsPrivacyBinding = FragmentTermsPrivacyBinding.inflate(inflater, parent, false)

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("隐私政策")
    }
}