package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.uibase.BaseActivity
import com.css.ble.BodyDetailAdapter
import com.css.ble.bean.WeightDetailBean
import com.css.ble.databinding.ActivityWeightMeasureEndDetailBinding
import com.css.ble.viewmodel.WeightMeasureVM

class WeightMeasureEndDetailActivity : BaseActivity<WeightMeasureVM, ActivityWeightMeasureEndDetailBinding>() {
    lateinit var mBodyDetailAdapter: BodyDetailAdapter
    var mData = ArrayList<WeightDetailBean>()
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBodyDetailAdapter = BodyDetailAdapter(this, mData)
        mViewBinding.rvData.layoutManager = LinearLayoutManager(this)
        mViewBinding.rvData.adapter = mBodyDetailAdapter
    }

    override fun initViewModel(): WeightMeasureVM =
        ViewModelProvider(this).get(WeightMeasureVM::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightMeasureEndDetailBinding =
        ActivityWeightMeasureEndDetailBinding.inflate(layoutInflater, parent, false)

}