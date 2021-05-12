package com.css.ble.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWheelMeasureEndBinding
import com.css.ble.viewmodel.WheelMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WheelMeasureEndFragment : BaseFragment<WheelMeasureVM, FragmentWheelMeasureEndBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWheelMeasureEndBinding {
        return FragmentWheelMeasureEndBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WheelMeasureVM {
        return ViewModelProvider(requireActivity()).get(WheelMeasureVM::class.java)
    }


}