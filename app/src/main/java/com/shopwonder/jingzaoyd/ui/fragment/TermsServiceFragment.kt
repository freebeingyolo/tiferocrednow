package com.shopwonder.jingzaoyd.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.shopwonder.jingzaoyd.databinding.FragmentCourseBinding
import com.shopwonder.jingzaoyd.databinding.FragmentTermsServiceBinding

class TermsServiceFragment : BaseFragment<DefaultViewModel, FragmentTermsServiceBinding>() {
    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentTermsServiceBinding = FragmentTermsServiceBinding.inflate(inflater, parent, false)

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("服务条款")
    }
}