package com.css.ble.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.databinding.ActivityWeightMeasureDoingBinding

class WeightMeasureDoingActivity :
    BaseActivity<DefaultViewModel, ActivityWeightMeasureDoingBinding>() {

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, WeightMeasureDoingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightMeasureDoingBinding = ActivityWeightMeasureDoingBinding.inflate(layoutInflater, parent, false)

}