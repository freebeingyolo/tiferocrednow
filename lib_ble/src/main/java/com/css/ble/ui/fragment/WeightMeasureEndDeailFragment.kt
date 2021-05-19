package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.uibase.BaseFragment
import com.css.ble.BodyDetailAdapter
import com.css.ble.R
import com.css.ble.bean.WeightDetailsBean
import com.css.ble.databinding.ActivityWeightMeasureEndDetailBinding
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureEndDeailFragment : BaseFragment<WeightMeasureVM, ActivityWeightMeasureEndDetailBinding>() {
    lateinit var mBodyDetailAdapter: BodyDetailAdapter

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityWeightMeasureEndDetailBinding {

        return ActivityWeightMeasureEndDetailBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): WeightMeasureVM {

        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftText(getString(R.string.device_weight))
        mBodyDetailAdapter = BodyDetailAdapter(requireContext(), ArrayList<WeightDetailsBean>())
        mViewBinding!!.rvData.layoutManager = LinearLayoutManager(requireContext())
        mViewBinding!!.rvData.adapter = mBodyDetailAdapter
    }

    override fun initData() {
        super.initData()
        loadData()
    }

    fun loadData() {
        mBodyDetailAdapter.setItems(mViewModel.getBodyFatDataList())
        mBodyDetailAdapter.notifyDataSetChanged()
    }
}