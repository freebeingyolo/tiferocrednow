package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.databinding.FragmentWeightMeasureEndDetailBinding
import com.css.ble.databinding.LayoutWeightMeasureEndDetailItemBinding
import com.css.ble.ui.view.BaseBindingAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val datas = mViewModel.getBodyFatDataList()
        mViewBinding ?.apply {
            lv.layoutManager = LinearLayoutManager(requireContext())
            lv.adapter = object : BaseBindingAdapter<Map<String,Any?>,LayoutWeightMeasureEndDetailItemBinding>(datas){
                override fun getLayoutResId(viewType: Int): Int {
                    return R.layout.layout_weight_measure_end_detail_item
                }

                override fun onBindItem(binding: LayoutWeightMeasureEndDetailItemBinding, item: Map<String, Any?>, position: Int) {
                    binding.apply {
                        tv1.text = item["key"].toString();
                        tv2.text = item["judge"].toString();
                        tv3.text = item["value"].toString();
                        executePendingBindings();//解决RecycleView刷新数据闪烁问题
                    }
                }
            }
        }
    }



}