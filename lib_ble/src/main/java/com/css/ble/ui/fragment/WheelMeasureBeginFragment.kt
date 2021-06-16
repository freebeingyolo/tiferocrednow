package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.view.ToolBarView
import com.css.ble.R
import com.css.ble.bean.BondDeviceData
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.ActivityAbrollerBinding
import com.css.ble.ui.DeviceInfoActivity
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.ble.viewmodel.WheelMeasureVM.State
import com.css.service.utils.CacheKey
import com.css.service.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WheelMeasureBeginFragment :
    BaseDeviceFragment<WheelMeasureVM, ActivityAbrollerBinding>(DeviceType.WHEEL) {
    private var startTime: Long = 0

    override fun initData() {
        super.initData()
        mViewModel.batteryLevel.observe(viewLifecycleOwner) {
            mViewBinding!!.batteryLevel.text = if (it == -1f) {
                "--"
            } else {
                String.format("%d%%", (it * 100).toInt())
            }
        }
        mViewModel.exerciseDuration.observe(viewLifecycleOwner) {
            mViewBinding!!.exerciseDuration.text = if (it == -1L) {
                "--"
            } else {

                String.format("%d", it)
            }

        }
        mViewModel.exerciseCount.observe(viewLifecycleOwner) {
            mViewBinding!!.exerciseCount.text = if (it == -1) {
                "--"
            } else {
                String.format("%d", it)
            }
            mViewBinding!!.exerciseKcal.text = if (it == -1) {
                "--"
            } else {
                String.format("%f", mViewModel.exerciseKcal)
            }
        }

        mViewModel.state.observe(viewLifecycleOwner) {
            refreshBottom(it)
            when (it) {
                State.connecting -> {
                    mViewBinding!!.connectState.setText(R.string.device_connecting)
                }
                State.discovered -> {
                    mViewBinding!!.connectState.setText(R.string.device_connected)
                }
                State.begin, State.disconnected -> {
                    mViewBinding!!.connectState.setText(R.string.device_disconnected)
                }
                State.timeOut -> {
                    CommonAlertDialog(requireContext()).apply {
                        type = CommonAlertDialog.DialogType.Tip
                        gravity = Gravity.BOTTOM
                        listener = object : DialogClickListener.DefaultLisener() {
                            override fun onLeftBtnClick(view: View) {
                            }

                            override fun onRightBtnClick(view: View) {
                                //TODO 重新连接
                                mViewModel.connect()
                            }
                        }
                    }.show()
                }
            }
        }
    }

    private fun refreshBottom(s: State) {
        mViewBinding?.apply {
            when (s) {
                State.exercise_start,
                State.exercise_pause,
                State.exercise_finish -> {
                    right.visibility = View.VISIBLE
                }
                else -> {
                    right.visibility = View.GONE
                    when (s) {
                        State.begin, State.disconnected -> {
                            left.text = "连接设备"
                        }
                        State.connecting -> {
                            left.text = "取消连接"
                        }
                        State.discovered -> {
                            left.text = "开始训练"

                        }
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
            }

        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_weight_measure_header, null, false)
        setRightImage(ImageUtils.getBitmap(view))
        getCommonToolBarView()?.setToolBarClickListener(object : OnToolBarClickListener {
            override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
                when (event) {
                    ToolBarView.ViewType.LEFT_IMAGE -> onBackPressed()
                    ToolBarView.ViewType.RIGHT_IMAGE -> {
                        DeviceInfoActivity.start(CacheKey.BOND_WHEEL_INFO.k)
                    }
                }
            }
        })
        mViewBinding?.apply {
            left.setOnClickListener {
                when (mViewModel.state.value) {
                    State.begin, State.disconnected -> {
                        startConnect()
                    }
                    State.connecting -> {
                        mViewModel.stopConnect()
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
                when (mViewModel.state.value) {
                    State.exercise_start, State.exercise_pause -> {
                        mViewModel.finishExercise()
                    }
                }
            }
        }
    }

    override fun initCommonToolBarBg(): ToolBarView.ToolBarBg {
        return ToolBarView.ToolBarBg.GRAY
    }

    override fun enabledVisibleToolBar(): Boolean = true


    fun startConnect() {
        //检查环境并搜搜
        startTime = System.currentTimeMillis()
        checkBleEnv()
        lifecycleScope.launch {
            while (!checkEnvDone) delay(100)
            if (BleEnvVM.isBleEnvironmentOk) {
                //至少停留200ms
                if (System.currentTimeMillis() - startTime < 200) delay(startTime + 200 - System.currentTimeMillis())
                mViewModel.connect()
            } else {
                BleErrorFragment.Builder.errorType(BleEnvVM.bleErrType)
                    .leftTitle(BondDeviceData.displayName(deviceType)).create()
            }
        }
    }


    override val vmCls: Class<WheelMeasureVM>
        get() = WheelMeasureVM::class.java
    override val vbCls: Class<ActivityAbrollerBinding>
        get() = ActivityAbrollerBinding::class.java
}