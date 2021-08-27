package com.css.ble.ui.fragment

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityAbrollerBinding
import com.css.ble.databinding.LayoutPlayRecommendItemBinding
import com.css.ble.ui.DataStatisticsActivity
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.ui.view.BaseBindingAdapter
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.DeviceVMFactory
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.ble.viewmodel.WheelMeasureVM.State
import com.css.service.data.CourseData
import com.css.service.router.ARouterConst
import com.css.service.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WheelMeasureBeginFragment : BaseDeviceFragment<WheelMeasureVM, ActivityAbrollerBinding>(DeviceType.WHEEL) {

    private val lowPowerAlert: View by lazy {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_network_error, null)
        view.findViewById<TextView>(R.id.textView).text = getString(R.string.lowpower_error, 10)
        view.post {//这里延缓获取坐标，高度
            val location = IntArray(2)
            val anchor = mViewBinding!!.root.findViewById<View>(R.id.ll_parent)
            anchor.getLocationOnScreen(location)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = location[1]
            view.layoutParams = lp
        }
        val root = requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        root.addView(view)
        view
    }

    override fun initViewModel(): WheelMeasureVM {
        return DeviceVMFactory.getViewModel(deviceType)
    }

    override fun initData() {
        super.initData()
        mViewModel.fetchRecommentation()
        //arguments?.takeIf { it.getBoolean("autoConnect") }?.let { mViewBinding!!.left.performClick() }
        mViewModel.state = mViewModel.state
        mViewBinding!!.model = mViewModel
        mViewBinding!!.view = this
        mViewBinding!!.lifecycleOwner = viewLifecycleOwner

        mViewModel.stateObsrv.observe(viewLifecycleOwner) {
            refreshBottom(it)
            when (it) {
                State.timeOut -> {
                    showReconnectDialog()
                }
            }
        }
        mViewModel.recommentationData.observe(viewLifecycleOwner) {
            recommendationAdapter.setItems(it)
            recommendationAdapter.notifyDataSetChanged()
        }
        mViewModel.batteryLevel.observe(viewLifecycleOwner) {
            if (it < 10 && it != -1) {
                lowPowerAlert.visibility = View.VISIBLE
            } else {
                lowPowerAlert.visibility = View.GONE
            }
        }
    }

    private fun showReconnectDialog() {
        CommonAlertDialog(requireContext()).apply {
            type = CommonAlertDialog.DialogType.Tip
            gravity = Gravity.BOTTOM
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    //TODO 重新连接
                    startConnect()
                }
            }
        }.show()
    }

    fun jumpToStatistic() {
        DataStatisticsActivity.starActivity(requireContext(), Bundle().apply { putString("deviceType", deviceType.alias) })
    }

    private fun refreshBottom(s: State) {
        mViewBinding?.apply {
            when (s) {
                State.exercise_start,
                State.exercise_pause,
                -> {
                    right.visibility = View.VISIBLE
                    when (s) {
                        State.exercise_start -> {
                            left.text = "暂停训练"
                            right.text = "结束训练"
                        }
                        State.exercise_pause -> {
                            left.text = "继续训练"
                            right.text = "结束训练"
                        }
                    }
                }
                else -> {
                    right.visibility = View.GONE
                    when (s) {
                        State.disconnected -> {
                            left.text = "连接设备"
                        }
                        State.connecting -> {
                            left.text = "取消连接"
                        }
                        State.discovered -> {
                            left.text = "开始训练"
                        }
                    }
                }
            }

        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(deviceType.name)
                    }
                }
            }
        })
        mViewBinding?.apply {
            left.setOnClickListener {
                when (mViewModel.stateObsrv.value) {
                    State.disconnected -> {
                        startConnect()
                    }
                    State.connecting -> {
                        mViewModel.disconnect()
                    }
                    State.discovered -> {
                        mViewModel.startExercise()
                    }
                    State.exercise_start -> {
                        mViewModel.pauseExercise()
                    }
                    State.exercise_pause -> {
                        mViewModel.resumeExercise()
                    }
                }
            }
            right.setOnClickListener {
                when (mViewModel.stateObsrv.value) {
                    State.exercise_start, State.exercise_pause -> {
                        mViewModel.finishExercise()
                    }
                }
            }
        }
        mViewBinding?.rvPlayRecommend!!.let {
            it.adapter = recommendationAdapter
            it.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    val recommendationAdapter = object : BaseBindingAdapter<CourseData, LayoutPlayRecommendItemBinding>() {

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
                    showNetworkErrorDialog();
                }
            } catch (e: ActivityNotFoundException) {
                ToastUtils.showShort("链接无效:${videoLink}")
            }
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
                //BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType).leftTitle(BondDeviceData.displayName(deviceType)).create()
                showToast(BleEnvVM.bleErrType.content) {
                    showReconnectDialog()
                }
            }
        }
    }


    override val vmCls: Class<WheelMeasureVM> get() = WheelMeasureVM::class.java
    override val vbCls: Class<ActivityAbrollerBinding> get() = ActivityAbrollerBinding::class.java
}