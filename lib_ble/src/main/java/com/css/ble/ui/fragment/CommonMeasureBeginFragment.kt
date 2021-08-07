package com.css.ble.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityAbrollerBinding
import com.css.ble.databinding.LayoutPlayRecommendItemBinding
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.State
import com.css.service.data.CourseData
import com.css.service.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
abstract class CommonMeasureBeginFragment<VB : ViewDataBinding>(d: DeviceType, val vm: BaseDeviceScan2ConnVM) :
    BaseDeviceFragment<BaseDeviceScan2ConnVM, VB>(d) {

    override fun initViewModel(): BaseDeviceScan2ConnVM = vm
    override val vmCls: Class<BaseDeviceScan2ConnVM> get() = BaseDeviceScan2ConnVM::class.java

    companion object {


        fun getExplicitFragment(vm: BaseDeviceScan2ConnVM, t: DeviceType): CommonMeasureBeginFragment<out ViewDataBinding> {

            return when (t) {
                DeviceType.HORIZONTAL_BAR -> HorizontalBarMeasureBeginFragment(t, vm)
                DeviceType.PUSH_UP -> HorizontalBarMeasureBeginFragment(t, vm)
                DeviceType.COUNTER -> HorizontalBarMeasureBeginFragment(t, vm)
                else -> throw IllegalAccessException("illegal call,t:$t")
            }

        }
    }

    override fun initData() {
        super.initData()
        mViewModel.fetchRecommentation()
        mViewModel.recommentationData.observe(viewLifecycleOwner) {
            recommendationAdapter.setItems(it)
            recommendationAdapter.notifyDataSetChanged()
        }
    }

    val recommendationAdapter = object : BaseBindingAdapter<CourseData, LayoutPlayRecommendItemBinding>() {
        override fun getLayoutResId(viewType: Int): Int {
            return R.layout.layout_play_recommend_item
        }

        override fun onBindItem(binding: LayoutPlayRecommendItemBinding, item: CourseData, position: Int) {
            binding.courseData = item
            binding.rtvPlay.setOnClickListener {
                //val url = "https://www.baidu.com"
                val url = item.videoLink
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    ToastUtils.showShort("链接无效:${url}")
                }
            }
            binding.executePendingBindings()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setUpJumpToDeviceInfo()
        mViewBinding!!.root.findViewById<RecyclerView>(R.id.rv_play_recommend).let {
            it.adapter = recommendationAdapter
            it.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun enabledVisibleToolBar(): Boolean = true


    fun startConnect() {
        //检查环境并搜搜
        checkBleEnv()
        lifecycleScope.launch {
            while (!checkEnvDone) delay(100)
            if (BleEnvVM.isBleEnvironmentOk) {
                if (mViewModel.state == State.disconnected) {
                    mViewModel.connect()
                }
            } else {
                BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(deviceType)).create()
            }
        }
    }

    fun jumpToStatistic() {
        val type = deviceType

    }
}