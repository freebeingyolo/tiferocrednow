package com.css.ble.ui.fragment

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutPlayRecommendItemBinding
import com.css.ble.ui.DataStatisticsActivity
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.ble.ui.view.BaseRecyclerViewAdapter
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.State
import com.css.service.data.CourseData
import com.css.service.router.ARouterConst
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
abstract class CommonMeasureBeginFragment<VB : ViewDataBinding>(d: DeviceType, val vm: BaseDeviceScan2ConnVM) :
    BaseDeviceFragment<BaseDeviceScan2ConnVM, VB>(d) {
    private var alertDialog: CommonAlertDialog? = null
    private var active: Boolean = false
    override fun initViewModel(): BaseDeviceScan2ConnVM = vm
    override val vmCls: Class<BaseDeviceScan2ConnVM> get() = BaseDeviceScan2ConnVM::class.java
    private val lowPowerAlert: View by lazy {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_network_error, null)
        view.findViewById<TextView>(R.id.textView).text = getString(R.string.lowpower_error, 10)
        view.post {//这里延缓获取坐标，高度
            val location = IntArray(2)
            val anchor = mViewBinding!!.root.findViewById<View>(R.id.ll_parent)
            anchor.getLocationOnScreen(location)
            val lp =
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = location[1]
            view.layoutParams = lp
        }
        val root = requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        root.addView(view)
        view
    }

    companion object {

        fun getExplicitFragment(
            vm: BaseDeviceScan2ConnVM,
            t: DeviceType
        ): CommonMeasureBeginFragment<out ViewDataBinding> {

            return when (t) {
                DeviceType.HORIZONTAL_BAR -> HorizontalBarMeasureBeginFragment(t, vm)
                DeviceType.PUSH_UP -> PushUpMeasureBeginFragment(t, vm)
                DeviceType.COUNTER -> CounterMeasureBeginFragment(t, vm)
                DeviceType.ROPE,DeviceType.ROPE_BBT -> RopeMeasureBeginFragment(t, vm)
                else -> throw IllegalAccessException("illegal call,t:$t")
            }

        }
    }

    override fun initData() {
        super.initData()
        mViewModel.stateObsrv.observe(viewLifecycleOwner) {
            when (it) {
                State.disconnected -> {
                    if (!active) {
                        showReconnectDialog()
                        active = false
                    }
                }
            }
        }
    }

    private fun showReconnectDialog() {
        if (alertDialog == null) {
            alertDialog = CommonAlertDialog(requireContext()).apply {
                type = CommonAlertDialog.DialogType.Tip
                gravity = Gravity.BOTTOM
                outSideDismiss = false
                listener = object : DialogClickListener.DefaultLisener() {
                    override fun onRightBtnClick(view: View) {
                        //TODO 重新连接
                        startConnect()
                    }
                }
            }
        }
        alertDialog?.show()
    }

    fun recommendationAdapter() = object : BaseBindingAdapter<CourseData, LayoutPlayRecommendItemBinding>() {
        override fun getLayoutResId(viewType: Int): Int {
            return R.layout.layout_play_recommend_item
        }

        override fun onBindItem(binding: LayoutPlayRecommendItemBinding, item: CourseData, position: Int) {
            binding.courseData = item
            binding.itemContainer.setOnClickListener {
                playCourseVideo(item.videoLink)
            }
            binding.executePendingBindings()
        }

        private fun playCourseVideo(videoLink: String) {
            try {
                if (NetworkUtils.isConnected()) {
                    ARouter.getInstance()
                        .build(ARouterConst.PATH_APP_MAIN_COURSE)
                        .with(Bundle().apply { putString("videoLink", videoLink) })
                        .navigation()
                } else {
                    showNetworkErrorDialog()
                }
            } catch (e: ActivityNotFoundException) {
                ToastUtils.showShort("链接无效:${videoLink}")
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setUpJumpToDeviceInfo()
        mViewModel.batteryLevel.observe(viewLifecycleOwner) {
            if (it < 10 && it != -1) {
                lowPowerAlert.visibility = View.VISIBLE
            } else {
                lowPowerAlert.visibility = View.GONE
            }
        }
        setCourseView()
    }

    private fun setCourseView() {
        val viewPager2 = mViewBinding!!.root.findViewById<ViewPager2>(R.id.viewpager2)
        val courseTitle = mViewBinding!!.root.findViewById<TabLayout>(R.id.courseTitle)
        courseTitle?.apply {
            val it = mViewModel.recommentationMap.entries.iterator()
            while (it.hasNext()) {
                addTab(newTab().apply {
                    val next = it.next()
                    text = next.key
                    val tabLayoutCls = courseTitle::class.java
                    val tabTextSizeField = tabLayoutCls.getDeclaredField("tabTextSize")
                    tabTextSizeField.isAccessible = true
                    tabTextSizeField.setInt(courseTitle, ConvertUtils.sp2px(12f))
                })
            }
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewPager2?.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
        viewPager2?.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    courseTitle?.getTabAt(position)?.select()
                }
            })
            val items = mutableListOf<List<CourseData>>().apply {
                for (i in 0 until mViewModel.recommentationMap.size) add(mutableListOf())
            }
            val viewPager2Adapter = object : BaseRecyclerViewAdapter<List<CourseData>>(items) {

                override fun onCreateView(parent: ViewGroup, viewType: Int): View {
                    val view = RecyclerView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        isFocusableInTouchMode = false
                    }
                    return view
                }

                override fun onBindItem(itemView: View, item: List<CourseData>, position: Int) {
                    val recyclerView = itemView as RecyclerView
                    var adapter = recyclerView.adapter as BaseBindingAdapter<CourseData, *>?
                    if (adapter == null) {
                        adapter = recommendationAdapter()
                        recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        recyclerView.adapter = adapter
                        val scene = courseTitle.getTabAt(position)!!.text.toString()
                        mViewModel.fetchRecommentation(scene)
                        mViewModel.recommentationMap[scene]?.observe(viewLifecycleOwner) {
                            adapter!!.setItems(it)
                        }
                    } else {
                        adapter!!.setItems(item)
                    }
                }
            }
            adapter = viewPager2Adapter
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
                showToast(BleEnvVM.bleErrType.content)
                mViewModel.disconnect()
                //BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(deviceType)).create()
            }
        }
    }

    fun disconnect() {
        mViewModel.disconnect()
        active = true

    }

    fun jumpToStatistic() {
        DataStatisticsActivity.starActivity(
            requireContext(),
            Bundle().apply { putString("deviceType", deviceType.alias) })
    }

}