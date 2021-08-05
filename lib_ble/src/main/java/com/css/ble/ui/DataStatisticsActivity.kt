package com.css.ble.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.css.base.uibase.BaseActivity
import com.css.ble.R
import com.css.ble.databinding.ActivityStatisticsBinding
import com.css.ble.ui.fragment.DataStatisticsFragment
import com.css.ble.viewmodel.DataStatisticsVM
import java.text.FieldPosition
import java.util.ArrayList

/**
 * Created by YH
 * Describe 数据统计（计数器/俯卧撑板）
 * on 2021/8/2.
 */
class DataStatisticsActivity : BaseActivity<DataStatisticsVM, ActivityStatisticsBinding>(),
    View.OnClickListener {

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, DataStatisticsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun enabledVisibleToolBar(): Boolean {
        return true
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle("数据统计")

        mViewBinding.tvWeek.setOnClickListener(this)
        mViewBinding.tvMonth.setOnClickListener(this)

        initListener()
    }

    private fun initListener() {
        val list: MutableList<DataStatisticsFragment> = ArrayList()
        for (i in 0..1) {
            val fragment = DataStatisticsFragment()
            val bundle = Bundle()
            if (i == 0) {
                bundle.putString("dataType", "周")
                bundle.putString("deviceType", "健腹轮")
            } else {
                bundle.putString("dataType", "月")
                bundle.putString("deviceType", "健腹轮")
            }
            fragment.arguments = bundle
            list.add(fragment)
        }

        mViewBinding.vpStatistics.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return list[position]
            }

            override fun getItemCount(): Int {
                return list.size
            }
        }

        mViewBinding.vpStatistics.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                showSelected(position)
            }
        })
    }

    override fun initData() {
        super.initData()

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(v: View?) {
        when (v) {
            mViewBinding.tvWeek -> {
                //选中周
                mViewBinding.vpStatistics.currentItem = 0;
                showSelected(0)
            }
            mViewBinding.tvMonth -> {
                //选中月
                mViewBinding.vpStatistics.currentItem = 1;
                showSelected(1)
            }
        }

    }

    private fun showSelected(position: Int) {
        if (position == 0) {
            mViewBinding.tvWeek.setTextColor(resources.getColor(R.color.colorAccent))
            mViewBinding.imgWeekIcon.visibility = View.VISIBLE
            mViewBinding.tvMonth.setTextColor(resources.getColor(R.color.color_262626))
            mViewBinding.tvMonthIcon.visibility = View.INVISIBLE
        } else {
            mViewBinding.tvWeek.setTextColor(resources.getColor(R.color.color_262626))
            mViewBinding.imgWeekIcon.visibility = View.INVISIBLE
            mViewBinding.tvMonth.setTextColor(resources.getColor(R.color.colorAccent))
            mViewBinding.tvMonthIcon.visibility = View.VISIBLE
        }

    }


    override fun initViewModel(): DataStatisticsVM =
        ViewModelProvider(this).get(DataStatisticsVM::class.java)

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ActivityStatisticsBinding = ActivityStatisticsBinding.inflate(layoutInflater, parent, false)


}