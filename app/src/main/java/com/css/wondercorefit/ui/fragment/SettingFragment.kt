package com.css.wondercorefit.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultYuboViewModel
import com.css.service.utils.SystemBarHelper
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.FragmentSettingBinding
import com.css.wondercorefit.ui.activity.setting.PersonInformationActivity


class SettingFragment : BaseFragment<DefaultYuboViewModel, FragmentSettingBinding>(),
    View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        SystemBarHelper.immersiveStatusBar(activity, 0f)
        SystemBarHelper.setHeightAndPadding(activity, mViewBinding?.topView)
        mViewBinding?.rlPersonInfo?.setOnClickListener(this)
    }

    override fun initViewModel(): DefaultYuboViewModel =
        ViewModelProvider(this).get(DefaultYuboViewModel::class.java)

    override fun getLayoutResId(): Int = R.layout.fragment_setting
    override fun onClick(v: View) {
        when (v.id) {
            R.id.rl_person_info -> {
                activity?.let { PersonInformationActivity.starActivity(it) }
            }
        }
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): FragmentSettingBinding = FragmentSettingBinding.inflate(inflater, viewGroup, false)
}