package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentCourseBinding

class CourseFragment : BaseFragment<DefaultViewModel, FragmentCourseBinding>() {

    override fun initView( savedInstanceState: Bundle?) {
        super.initView( savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun getLayoutResId(): Int = R.layout.fragment_course
    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentCourseBinding = FragmentCourseBinding.inflate(inflater, viewGroup, false)
}