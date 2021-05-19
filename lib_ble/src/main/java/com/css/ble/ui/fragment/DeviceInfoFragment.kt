package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.R
import com.css.ble.databinding.ActivityDeviceInfoBinding

/**
 * @author yuedong
 * @date 2021-05-17
 */
class DeviceInfoFragment : BaseFragment<DefaultViewModel, ActivityDeviceInfoBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityDeviceInfoBinding {

        return ActivityDeviceInfoBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): DefaultViewModel {
        return ViewModelProvider(requireActivity()).get(DefaultViewModel::class.java)
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        setToolBarLeftText(getString(R.string.ble_weight_name))

    }
}