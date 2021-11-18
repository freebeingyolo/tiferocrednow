package com.css.ble.ui.fragment

import LogUtils
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.*
import androidx.core.content.ContextCompat
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.EditDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutRopeBinding
import com.css.ble.viewmodel.RopeVM
import com.css.ble.viewmodel.RopeVM.Mode
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.pickerview.builder.OptionsPickerBuilder
import com.css.pickerview.view.OptionsPickerView
import com.tencent.bugly.Bugly.applicationContext
import razerdp.basepopup.BasePopupWindow
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *@author chanpal
 *@time 2021-11-01
 *@description 跳绳器
 */
class RopeMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) :
    CommonMeasureBeginFragment<LayoutRopeBinding>(d, vm) {

    override val vbCls: Class<LayoutRopeBinding> get() = LayoutRopeBinding::class.java
    private val mViewModel2: RopeVM get() = mViewModel as RopeVM
    private var connectControl: Int = 0
    private var mCountTimeDialog: OptionsPickerView<String>? = null
    private val mCountTimeList by lazy { (30 downTo 1).map { it.toString() } }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initState()
        initUI()
    }

    override fun initData() {
        super.initData()
        mViewBinding!!.let {
            it.view = this
            it.model = mViewModel as RopeVM
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    fun initUI() {
        when (mViewModel2.state) {
            BaseDeviceScan2ConnVM.State.discovered -> {
                discoveredBLE()
            }
            BaseDeviceScan2ConnVM.State.disconnected -> {
                disconnectBLE()
            }
        }
    }

    fun initState() {
        mViewModel2.stateObsrv.observe(viewLifecycleOwner) {
            when (it) {
                BaseDeviceScan2ConnVM.State.disconnected -> {
                    disconnectBLE()
                }
                BaseDeviceScan2ConnVM.State.connected -> {
                    discoveredBLE()
                }
                BaseDeviceScan2ConnVM.State.connecting -> {
                    mViewBinding?.connectControl?.text = "取消连接"
                }
            }
        }
    }

    private fun discoveredBLE() {
        mViewBinding?.tv?.setTextColor(Color.BLACK)
        mViewBinding?.connectControl?.visibility = View.GONE
        mViewBinding?.startExercise?.visibility = View.VISIBLE
        mViewBinding?.modeSwitch?.setTextColor(Color.BLACK)
        mViewBinding?.modeSwitch2?.setTextColor(resources.getColor(R.color.colorAccent))
        mViewBinding?.ropeStatics?.setImageResource(R.mipmap.icon_statistics)
        mViewBinding?.modeSwitch3?.setImageResource(R.mipmap.icon_rope_mode)
        mViewBinding?.statisticGroup?.isEnabled = true
        mViewBinding?.modeContainer?.isEnabled = true
    }

    fun disconnectBLE() {
        mViewBinding?.startExercise?.visibility = View.GONE
        mViewBinding?.startExercised?.visibility = View.GONE
        mViewBinding?.startExercise1?.visibility = View.VISIBLE
        mViewBinding?.connectControl?.visibility = View.VISIBLE
        mViewBinding?.connectControl?.text = "连接设备"
        mViewBinding?.tv?.setTextColor(Color.GRAY)
        mViewBinding?.modeSwitch?.setTextColor(Color.GRAY)
        mViewBinding?.modeSwitch2?.setTextColor(resources.getColor(R.color.color_F8B698))
        mViewBinding?.ropeStatics?.setImageResource(R.mipmap.icon_statistics_gray)
        mViewBinding?.modeSwitch3?.setImageResource(R.mipmap.icon_rope_mode_gray)
        mViewBinding?.statisticGroup?.isEnabled = false
        mViewBinding?.modeContainer?.isEnabled = false
    }

    fun openSwitchSpinner(v: View) {
        val anchorView = mViewBinding!!.modeContainer
        val popUpWindow = object : BasePopupWindow(requireContext(), anchorView.width, LayoutParams.WRAP_CONTENT) {
            override fun onCreateContentView(): View {
                val listView = ListView(requireContext())
                listView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_while_radius4)
                listView.divider = null
                val datas = mViewModel2.getModels()
                listView.adapter =
                    object : ArrayAdapter<String>(requireContext(), R.layout.layout_device_mode_switch_item, datas) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val ret = super.getView(position, convertView, parent)
                            ret.setOnClickListener {
                                dismiss()
                                val modes = Mode.values()
                                if (Mode.byCountTime == modes[position]) {//倒计时长
                                    mCountTimeDialog = OptionsPickerBuilder(
                                        activity
                                    ) { options1, _, _, _ ->
                                        //mCountTime = mCountTimeList[options1]
                                        mViewModel2.mCountTime = mCountTimeList[options1].toInt()
                                    }.setLayoutRes(
                                        R.layout.dialog_rope_count_time
                                    ) { v ->
                                        val title = v?.findViewById<TextView>(R.id.tv_title)
                                        val cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                                        val submit = v?.findViewById<TextView>(R.id.btn_submit)
                                        title?.text = "设置倒计时时长"
                                        cancel?.setOnClickListener { mCountTimeDialog?.dismiss() }
                                        submit?.setOnClickListener {
                                            mCountTimeDialog?.returnData()
                                            switchMode(modes[position], popupWindow)
                                            mCountTimeDialog?.dismiss()
                                        }
                                    }.setLabels("分钟", "", "")
                                        .isCenterLabel(true)
                                        .setSelectOptions(mCountTimeList.indexOf(mViewModel2.mCountTime.toString()))
                                        .setLineSpacingMultiplier(3.0F)
                                        .setTextColorCenter(Color.parseColor("#F2682A"))
                                        .setOutSideCancelable(true)//点击外部dismiss default true
                                        .isDialog(true)//是否显示为对话框样式
                                        .build()
                                    mCountTimeDialog?.setPicker(mCountTimeList)
                                    val lp: FrameLayout.LayoutParams =
                                        mCountTimeDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
                                    lp.leftMargin = 0
                                    lp.rightMargin = 0
                                    mCountTimeDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
                                    mCountTimeDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
                                    mCountTimeDialog?.show()
                                } else if (Mode.byCountNumber == modes[position]) {
                                    EditDialog.Builder().apply {
                                        title = "设置倒计数数量"
                                        hint = "请输入数量"
                                        leftBtnText = "取消"
                                        rightBtnText = "确定"
                                        inputType = InputType.TYPE_CLASS_NUMBER
                                    }.build(context).apply {
                                        setListener(listener = object : DialogClickListener.DefaultLisener() {
                                            override fun onRightEditBtnClick(view: View, content: String?) {
                                                if (content == "") {
                                                    showCenterToast("您还未输入倒计数个数")
                                                    return
                                                }
                                                val p: Pattern = Pattern.compile("[0-9]*")
                                                val m: Matcher = p.matcher(content)
                                                if (m.matches()) {
                                                    val countNumber = Integer.parseInt(content.toString())
                                                    if (countNumber < 1 || countNumber > 500) {
                                                        showCenterToast("倒计数只能是1-500的整数才可开始训练")
                                                        dismiss()
                                                    } else {
                                                        mViewModel2.mCountNumber = content!!.toInt()
                                                        switchMode(modes[position], popupWindow)
                                                        dismiss()
                                                    }
                                                } else {
                                                    showCenterToast("倒计数只能是1-500的整数才可开始训练")
                                                }
                                            }
                                        })
                                    }.show()

                                } else {
                                    switchMode(modes[position], popupWindow)
                                }
                            }
                            return ret
                        }
                    }
                listView.choiceMode = ListView.CHOICE_MODE_SINGLE
                listView.setItemChecked(mViewModel2.mode.ordinal, true)
                return listView
            }
        }
            .setOutSideDismiss(true)
            .setPopupGravity(Gravity.TOP)
            .setBackground(null)
            .setOffsetY(-10)
            .setBackPressEnable(false)

        popUpWindow.showPopupWindow(anchorView)
    }

    private fun switchMode(mode: Mode, popupWindow: PopupWindow) {
        mViewModel2.switchMode(mode, object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                ToastUtils.showShort("切换模式失败")
                popupWindow.dismiss()
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("切换模式成功:${StringUtils.toHex(value, "")}")
                ToastUtils.showShort("切换${getString(mode.msgId)}成功")
                popupWindow.dismiss()
            }
        })
    }

    fun connectChange() {
        if (connectControl == 0) {
            startConnect()
            connectControl = 1
        } else {
            disconnect()
            connectControl = 0
        }
    }

    fun startExercise() {
        mViewModel2.reset()
        mViewModel2.setIsStart(true)
        when (mViewModel2.mode) {
            Mode.byFree -> {
                //开始时保证对端数据正确
                mViewModel2.switchMode(mViewModel2.mode)
            }
            Mode.byCountTime -> {
                if (mViewModel2.mCountTime == -1) {
                    ToastUtils.showShort("请先选择运动模式")
                    return
                } else {
                    //开始时保证对端数据正确
                    mViewModel2.switchMode(mViewModel2.mode)
                }
            }
            Mode.byCountNumber -> {
                if (mViewModel2.mCountNumber == -1) {
                    ToastUtils.showShort("请先选择运动模式")
                    return
                } else {
                    //开始时保证对端数据正确
                    mViewModel2.switchMode(mViewModel2.mode)
                }
            }
        }
        mViewBinding?.modeSwitch?.setTextColor(Color.GRAY)
        mViewBinding?.modeSwitch2?.setTextColor(resources.getColor(R.color.color_F8B698))
        mViewBinding?.modeSwitch3?.setImageResource(R.mipmap.icon_rope_mode_gray)
        mViewBinding?.modeContainer?.isEnabled = false
        mViewBinding?.startExercised?.visibility = View.VISIBLE
        mViewBinding?.startExercise1?.visibility = View.GONE
    }

    fun stopExercise() {
        CommonAlertDialog(applicationContext).apply {
            type = CommonAlertDialog.DialogType.Confirm
            gravity = Gravity.BOTTOM
            title = "结束训练"
            content = "确认立即结束训练吗？"
            leftBtnText = "取消"
            rightBtnText = "确定"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    mViewModel2.changeExercise(RopeVM.MotionState.STOP)
                    mViewModel2.setIsStart(false)
                    mViewBinding?.modeSwitch?.setTextColor(Color.BLACK)
                    mViewBinding?.modeSwitch2?.setTextColor(resources.getColor(R.color.colorAccent))
                    mViewBinding?.modeSwitch3?.setImageResource(R.mipmap.icon_rope_mode)
                    mViewBinding?.modeContainer?.isEnabled = true
                    mViewBinding?.startExercised?.visibility = View.GONE
                    mViewBinding?.startExercise1?.visibility = View.VISIBLE

                }
            }
        }.show()
    }

    fun startExercise2() {
        if ("暂停训练" == mViewBinding?.pauseExercise?.text) {
            mViewModel2.changeExercise(RopeVM.MotionState.PAUSE)
            mViewBinding?.pauseExercise?.text = "开始训练"
        } else {
            mViewModel2.changeExercise(RopeVM.MotionState.RESUME)
            mViewBinding?.pauseExercise?.text = "暂停训练"
        }
    }

}