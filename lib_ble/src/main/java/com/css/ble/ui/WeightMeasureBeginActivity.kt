package com.css.ble.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.databinding.ActivityWeightMeasureBeginBinding
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-12
 */
//WeightMeasueBeginFragment -> WeightMeasuring ->
class WeightMeasureBeginActivity : BaseActivity<WeightMeasureVM, ActivityWeightMeasureBeginBinding>(), View.OnClickListener {

    companion object {

        fun toWeightBondOrMeasureAct(context: Context) {
            var data = WonderCoreCache.getData(WonderCoreCache.BOND_WEIGHT_INFO, BondDeviceData::class.java)
            if (data.mac.isNullOrEmpty()) {//跳转到绑定页面
                val intent = Intent(context, BleEntryActivity::class.java)
                context.startActivity(intent)
            } else {//跳转到测量页面
                val intent = Intent(context, WeightMeasureActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
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