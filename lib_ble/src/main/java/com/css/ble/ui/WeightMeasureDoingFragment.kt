package com.css.ble.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWeightMeasureDoingBinding
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WeightMeasureDoingFragment : BaseFragment<WeightMeasureVM, FragmentWeightMeasureDoingBinding>(){

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightMeasureDoingBinding {
        return FragmentWeightMeasureDoingBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

}