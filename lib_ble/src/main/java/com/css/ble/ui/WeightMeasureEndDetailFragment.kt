package com.css.ble.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWeightMeasureEndDetailBinding
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WeightMeasureEndDetailFragment : BaseFragment<WeightMeasureVM, FragmentWeightMeasureEndDetailBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightMeasureEndDetailBinding {
        return FragmentWeightMeasureEndDetailBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

}