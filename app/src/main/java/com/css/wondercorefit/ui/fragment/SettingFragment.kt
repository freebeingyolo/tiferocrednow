package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultYuboViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import kotlinx.android.synthetic.main.fragment_main.*


class SettingFragment : BaseFragment<DefaultYuboViewModel>() {

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, top_view)
    }

    override fun initViewModel(): DefaultYuboViewModel =
        ViewModelProvider(this).get(DefaultYuboViewModel::class.java)

    override fun getLayoutResId(): Int = R.layout.fragment_setting
}