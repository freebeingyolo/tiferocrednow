package com.css.ble.ui

import android.content.Context
import android.content.Intent
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

    companion object {


        fun starActivity(context: Context) {
            val intent = Intent(context, WeightMeasureEndDetailActivity::class.java)
            context.startActivity(intent)
        }
    }
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBodyDetailAdapter = BodyDetailAdapter(this, mData)
        mViewBinding.rvData.layoutManager = LinearLayoutManager(this)
        mViewBinding.rvData.adapter = mBodyDetailAdapter

        mViewBinding.pbWeight.setProgress(50)
    }

    override fun initViewModel(): WeightMeasureVM =
        ViewModelProvider(this).get(WeightMeasureVM::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightMeasureEndDetailBinding =
        ActivityWeightMeasureEndDetailBinding.inflate(layoutInflater, parent, false)

}