package com.css.ble.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.css.base.uibase.BaseFragment
import com.css.base.utils.DateTimeHelper
import com.css.ble.databinding.FragmentStatisticsBinding
import com.css.ble.ui.view.StatisticsAdapter
import com.css.ble.viewmodel.DataStatisticsVM
import com.css.service.data.PullUpData
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.util.*

/**
 * Created by YH
 * Describe 数据统计Fragment
 * on 2021/8/3.
 */
class DataStatisticsFragment : BaseFragment<DataStatisticsVM, FragmentStatisticsBinding>(),
    View.OnClickListener {

    //栏目：周/月
    private var dataType: String? = null

    //设备类型：计数器/俯卧撑板之类
    private var deviceType: String? = null

    //周/月 的起始日期和结束日期
    private var dateList: List<String> = ArrayList()

    //当天
    private val currentDay = Date()

    //选中日期
    private var selectedDay = Date()

    lateinit var mAdapter: StatisticsAdapter
    var mData = ArrayList<PullUpData>()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
//        //设置日期和当前日
//        updateCurrentDayView(DateTimeHelper.formatToString(currentDay, "mm-dd"))
//        updateDateView(list[0],list[1])

        mAdapter = StatisticsAdapter(mData)
        mViewBinding!!.rlHistoryData.layoutManager = LinearLayoutManager(requireContext())
        mViewBinding!!.rlHistoryData.adapter = mAdapter

        mViewBinding!!.imgLastDate.setOnClickListener(this)
        mViewBinding!!.imgNextDate.setOnClickListener(this)
    }

    override fun initData() {
        super.initData()
        //获取参数
        dataType = arguments?.getString("dataType")
        deviceType = arguments?.getString("deviceType")
        LogUtils.dTag("---->", "$dataType--$deviceType");

        loadDate(true, 1, currentDay)
    }

    private fun loadDate(isToDay: Boolean, dateType: Int, date: Date) {
        //根据参数拿数据
        dateList = if (TextUtils.equals(dataType, "周")) {
            //周
            DateTimeHelper.getCurrentWeek(dateType, date)
        } else {
            //月
            DateTimeHelper.getCurrentMonth(dateType, date)
        }
        updateData(true, dateList[0], dateList[1]);
    }

    private fun updateData(isToDay: Boolean, startDate: String, endDate: String) {
        //刷新时间
        updateDateView(startDate, endDate)

        if (isToDay) {
            updateCurrentDayView(DateTimeHelper.formatToString(currentDay, "MM月dd日"))
        } else {
            updateCurrentDayView(DateTimeHelper.formatToString(startDate, "MM月dd日"))
        }
        //获取数据
        mViewModel.queryPushUps(deviceType!!, startDate, endDate)
    }


    override fun registorUIChangeLiveDataCallBack() {
        super.registorUIChangeLiveDataCallBack()
        mViewModel.pullUpDataList.observe(this, {
            LogUtils.dTag("---->", it);
//            没有数据场景
//            单击事件
//            默认选中时间
            //历史数据
            mData.addAll(it)
            mAdapter.setItems(mData)

            //绘制图表
            initMPChat()
        })
    }

    private fun updateDateView(startDate: String, endDate: String) {
        val text = DateTimeHelper.formatToString(
            startDate,
            "yyyy年MM月dd日"
        ) + "-" + DateTimeHelper.formatToString(endDate, "yyyy年MM月dd日")
        mViewBinding!!.tvCurrentDate.text = text
    }

    private fun updateCurrentDayView(day: String) {
//        if (DateTimeHelper.isSameDay(currentDay, DateTimeHelper.parseStringToDate(day))) {
//            //是今天
//            mViewBinding!!.tvCurrentDay.text = DateTimeHelper.formatToString(currentDay, "MM月dd日")
//            selectedDay = currentDay
//        } else {
        //不是今天
        mViewBinding!!.tvCurrentDay.text = day
        selectedDay = DateTimeHelper.parseStringToDate(day)
//        }
    }

    private fun updateExerciseNumber(isShow: Boolean,number: Int) {
        if (isShow) {
            mViewBinding!!.tvType.visibility = View.VISIBLE
            mViewBinding!!.tvNumber.visibility = View.VISIBLE
            mViewBinding!!.tvUnit.visibility = View.VISIBLE

            mViewBinding!!.tvType.text = deviceType
            mViewBinding!!.tvNumber.text = number.toString()
        }else{
            mViewBinding!!.tvType.visibility = View.INVISIBLE
            mViewBinding!!.tvNumber.visibility = View.INVISIBLE
            mViewBinding!!.tvUnit.visibility = View.INVISIBLE
        }
    }


    private fun initMPChat() {
        //获取时间集合
        val carList = DateTimeHelper.getDates(
            DateTimeHelper.parseStringToDate(dateList[0]),
            DateTimeHelper.parseStringToDate(dateList[1])
        )

        val barList = ArrayList<BarEntry>()

        for (i in carList.indices) {

            //跟获取的数据做对比，是同一天，显示数据
            var exerciseNumber: Int = 0
            for (j in mData.indices) {
                if (carList[i].time == DateTimeHelper.parseStringToDate(mData[j].todayDate).time) {
                    exerciseNumber = mData[j].exerciseNumber
                    LogUtils.dTag("运动日期--->", mData[j].todayDate + "--次数：" + exerciseNumber)
                }
            }

            //其中两个数字对应的分别是   X轴   Y轴
            barList.add(BarEntry(i.toFloat(), exerciseNumber.toFloat()))
        }

        //柱子
        val barDataSet = BarDataSet(barList, "");
        barDataSet.color = Color.parseColor("#FFA43D") //柱子的颜色
        barDataSet.isHighlightEnabled = true //选中柱子是否高亮显示  默认为true
        barDataSet.setDrawValues(false) //不显示值

        val barData = BarData(barDataSet);
//        barData.setValueTextSize(10f);
        barData.barWidth = 0.3f //设置框高

        mViewBinding!!.barStatistics.data = barData //设置数据
        mViewBinding!!.barStatistics.description.isEnabled = false//隐藏右下角英文
//        bar.setTouchEnabled(false); // 设置是否可以触摸
//        mViewBinding!!.barStatistics.setTouchEnabled(false); // 设置是否可以触摸
        mViewBinding!!.barStatistics.isDragEnabled = false// 是否可以拖拽
        mViewBinding!!.barStatistics.setScaleEnabled(false) // 是否可以缩放
        mViewBinding!!.barStatistics.setPinchZoom(false) //y轴的值是否跟随图表变换缩放;如果禁止，y轴的值会跟随图表变换缩放

        //X轴
        mViewBinding!!.barStatistics.xAxis.setDrawGridLines(false)
        mViewBinding!!.barStatistics.xAxis.setDrawAxisLine(false)
//        mViewBinding!!.barStatistics.xAxis.setDrawLabels(false)
//        mViewBinding!!.barStatistics.xAxis.isEnabled = false
        mViewBinding!!.barStatistics.xAxis.position =
            XAxis.XAxisPosition.BOTTOM_INSIDE //X轴所在位置   默认为上面
        mViewBinding!!.barStatistics.xAxis.textColor = Color.parseColor("#000000")
        mViewBinding!!.barStatistics.xAxis.textSize = 10f
        mViewBinding!!.barStatistics.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(v: Float): String {
                return DateTimeHelper.formatToString(carList[v.toInt()], "MM/dd") //注意这里需要改成 ""
            }
        }
        //Y轴
        mViewBinding!!.barStatistics.axisRight.gridColor = Color.parseColor("#CCCCCC")
        mViewBinding!!.barStatistics.axisRight.setDrawGridLines(true) //是否绘制Y轴上的网格线（背景里面的横线）
        mViewBinding!!.barStatistics.axisRight.setDrawAxisLine(false)
        mViewBinding!!.barStatistics.axisRight.setDrawLabels(true) //右侧是否显示Y轴数值
        mViewBinding!!.barStatistics.axisRight.isEnabled = true //是否显示最右侧竖线
        mViewBinding!!.barStatistics.axisRight.textSize = 10f
        //Y轴自定义坐标
        mViewBinding!!.barStatistics.axisRight.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(v: Float): String {
                return "  " + v
            }
        }
//        mViewBinding!!.barStatistics.axisLeft.gridColor = Color.parseColor("#CCCCCC")
        mViewBinding!!.barStatistics.axisLeft.setDrawGridLines(false) //是否绘制Y轴上的网格线（背景里面的横线）
        mViewBinding!!.barStatistics.axisLeft.setDrawAxisLine(false)
        mViewBinding!!.barStatistics.axisLeft.setDrawLabels(false) //右侧是否显示Y轴数值
        mViewBinding!!.barStatistics.axisLeft.isEnabled = false //是否显示最右侧竖线

        //数据更新
        mViewBinding!!.barStatistics.notifyDataSetChanged()
        mViewBinding!!.barStatistics.invalidate()
        //动画（如果使用了动画可以则省去更新数据的那一步）
        mViewBinding!!.barStatistics.animateY(1500) //在Y轴的动画  参数是动画执行时间 毫秒为单位

        mViewBinding!!.barStatistics.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                updateExerciseNumber(true,e.y.toInt())
                updateCurrentDayView(DateTimeHelper.formatToString(carList[e.x.toInt()], "MM月dd日"))
            }

            override fun onNothingSelected() {
                updateExerciseNumber(false,0)
            }
        })
    }

    override fun initViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentStatisticsBinding = FragmentStatisticsBinding.inflate(layoutInflater, parent, false)


    override fun initViewModel(): DataStatisticsVM =
        ViewModelProvider(this).get(DataStatisticsVM::class.java)

    override fun onClick(v: View?) {
        when (v) {
            mViewBinding!!.imgLastDate -> {
                loadDate(false, 0, selectedDay)
            }
            mViewBinding!!.imgNextDate -> {
                loadDate(false, 2, selectedDay)
            }
        }
    }

}