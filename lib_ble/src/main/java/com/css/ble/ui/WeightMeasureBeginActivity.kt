package com.css.ble.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
//WeightMeasueBeginFragment -> WeightMeasuring ->
class WeightMeasureBeginActivity : BaseActivity<WeightMeasureVM, ActivityWeightMeasureBeginBinding>(), View.OnClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("蓝牙体脂秤")
        mViewBinding.tvToMeasure.setOnClickListener(this)
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg = ToolBarView.ToolBarBg.GRAY

    override fun initViewBinding(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?
    ): ActivityWeightMeasureBeginBinding =
        ActivityWeightMeasureBeginBinding.inflate(layoutInflater, viewGroup, false)

    override fun initViewModel(): WeightMeasureVM {
        return ViewModelProvider(this).get(WeightMeasureVM::class.java)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_to_measure -> {
                WeightMeasureDoingActivity.starActivity(this)
            }
        }

    }

}