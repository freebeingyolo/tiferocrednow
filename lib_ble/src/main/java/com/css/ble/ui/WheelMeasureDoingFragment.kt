package com.css.ble.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWheelMeasureBeginBinding
import com.css.ble.viewmodel.WheelMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WheelMeasureDoingFragment : BaseFragment<WheelMeasureVM, FragmentWheelMeasureBeginBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWheelMeasureBeginBinding {
        return FragmentWheelMeasureBeginBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WheelMeasureVM {
        return ViewModelProvider(requireActivity()).get(WheelMeasureVM::class.java)
    }


}