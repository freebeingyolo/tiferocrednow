package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment<MainViewModel>() {

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, top_view)
    }

    override fun initViewModel(): MainViewModel =
        ViewModelProvider(this).get(MainViewModel::class.java)

    override fun getLayoutResId(): Int = R.layout.fragment_main
}