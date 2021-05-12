package com.css.ble.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWeightMeasureBeginBinding
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
//WeightMeasueBeginFragment -> WeightMeasuring ->
class WeightMeasureBeginFragment : BaseFragment<WeightMeasureVM,FragmentWeightMeasureBeginBinding>() {


    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWeightMeasureBeginBinding {

        return FragmentWeightMeasureBeginBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

}