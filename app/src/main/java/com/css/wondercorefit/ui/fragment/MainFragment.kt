package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.view.ToolBarView
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentMainBinding
import com.css.wondercorefit.viewmodel.MainViewModel


class MainFragment : BaseFragment<MainViewModel,FragmentMainBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
    }

    override fun initViewModel(): MainViewModel =
        ViewModelProvider(this).get(MainViewModel::class.java)

    override fun getLayoutResId(): Int = R.layout.fragment_main

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentMainBinding = FragmentMainBinding.inflate(inflater, viewGroup, false)
}