package com.css.ble.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.uibase.BaseFragment
import com.css.ble.BodyDetailAdapter
import com.css.ble.R
import com.css.ble.bean.WeightDetailBean
import com.css.ble.databinding.ActivityWeightMeasureEndDetailBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.utils.BodyJudgeUtil
import com.css.ble.viewmodel.WeightMeasureVM
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureEndDeailFragment : BaseFragment<WeightMeasureVM, ActivityWeightMeasureEndDetailBinding>() {
    private val TAG: String = "WeightMeasureEndDeailFragment"
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
        mBodyDetailAdapter = BodyDetailAdapter(requireContext(), ArrayList<WeightDetailBean>())
        mViewBinding!!.rvData.layoutManager = LinearLayoutManager(requireContext())
        mViewBinding!!.rvData.adapter = mBodyDetailAdapter
        mViewBinding?.apply {
            btnMeasureWeight.setOnClickListener {
                FragmentUtils.changeFragment(WeightMeasureFragment::class.java)
                Log.d(TAG, "btnMeasureWeight#click")
            }
            mViewModel.bondData.value!!.apply {
                tvWeightNum.text = weightKgFmt
                var bodyFatData = getBodyFatData()
                var userInfo = WonderCoreCache.getUserInfo()
                tvTodayBodyStatus.text = String.format(
                    "BMI%.1f|%s", bodyFatData.bmi, BodyJudgeUtil.fatLevel(
                        weightKg,
                        userInfo.stature.toFloat(),
                        userInfo.sex
                    )
                )
                pbWeight.setProgress(
                    BodyJudgeUtil.fatLeveRate(
                        weightKg, userInfo.statureFloat, userInfo.sex
                    ).toInt()
                )
            }


        }

    }

    override fun initData() {
        super.initData()
        loadData()
    }

    fun loadData() {
        mBodyDetailAdapter.setItems(mViewModel.bondData.value!!.getBodyFatDataList())
        mBodyDetailAdapter.notifyDataSetChanged()
    }
}