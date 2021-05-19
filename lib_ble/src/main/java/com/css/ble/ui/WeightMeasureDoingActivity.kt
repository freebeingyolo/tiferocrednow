package com.css.ble.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.ble.databinding.ActivityWeightMeasureDoingBinding
import com.css.ble.viewmodel.WeightMeasureVM

class WeightMeasureDoingActivity : BaseActivity<WeightMeasureVM, ActivityWeightMeasureDoingBinding>() {

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, WeightMeasureDoingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initViewModel(): WeightMeasureVM =
        ViewModelProvider(this).get(WeightMeasureVM::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightMeasureDoingBinding = ActivityWeightMeasureDoingBinding.inflate(layoutInflater, parent, false)

}