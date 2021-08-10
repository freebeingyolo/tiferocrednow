package com.css.ble.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.css.base.view.ToolBarView
import com.css.ble.BodyDetailAdapter
import com.css.ble.bean.WeightDetailBean
import com.css.ble.databinding.ActivityWeightMeasureEndDetailBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightMeasureVM

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightMeasureEndDeailFragment :
    BaseWeightFragment<WeightMeasureVM, ActivityWeightMeasureEndDetailBinding>() {
    private val TAG: String = "WeightMeasureEndDeailFragment"
    lateinit var mBodyDetailAdapter: BodyDetailAdapter

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityWeightMeasureEndDetailBinding {
        return ActivityWeightMeasureEndDetailBinding.inflate(inflater, parent, false)
    }

    override fun initViewModel(): WeightMeasureVM {

        return ViewModelProvider(requireActivity()).get(WeightMeasureVM::class.java)
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.WHITE
    }

    override fun enabledVisibleToolBar() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setUpJumpToDeviceInfo()
        mBodyDetailAdapter = BodyDetailAdapter(requireContext(), ArrayList<WeightDetailBean>())
        mViewBinding!!.rvData.layoutManager = LinearLayoutManager(requireContext())
        mViewBinding!!.rvData.adapter = mBodyDetailAdapter
        mViewBinding?.apply {
            btnMeasureWeight.setOnClickListener {
                FragmentUtils.changeFragment(WeightMeasureDoingFragment::class.java, FragmentUtils.Option.OPT_ADD)
                Log.d(TAG, "btnMeasureWeight#click")
            }
            mViewModel.bondData.observe(viewLifecycleOwner) {
                it.apply {
                    val weightList = weightKgFmt.split(".")
                    tvWeightNum.text = " ${weightList[0]}."
                    tvWeightFloatNum.text = weightList[1]
                    tvTodayBodyStatus.text = String.format("BMI%.1f|%s", bodyFatData.bmi, bodyFatData.bmiJudge)
                    pbWeight.setProgress(bodyFatData.weightProgress.toInt())
                    score.text = bodyFatData.bodyScore.toString()
                    loadData()
                }
            }

        }
    }

    fun loadData() {
        mBodyDetailAdapter.setItems(mViewModel.bondData.value!!.getBodyFatDataList())
        mBodyDetailAdapter.notifyDataSetChanged()
    }

    override fun onVisible() {
        super.onVisible()
        mViewBinding!!.scrollView.scrollTo(0, 0)
    }

}