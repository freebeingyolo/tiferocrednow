package com.css.ble.ui.fragment

import LogUtils
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
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM.State
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
    private var mCountTimeDialog: OptionsPickerView<String>? = null
    private val mCountTimeList by lazy { (1..30).map { it.toString() } }


    override fun initData() {
        super.initData()
        mViewBinding!!.let {
            it.view = this
            it.model = mViewModel as RopeVM
            it.lifecycleOwner = viewLifecycleOwner
        }
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
                                    mCountTimeDialog = OptionsPickerBuilder(activity) { options1, _, _, _ ->
                                        mViewModel2.mCountTime = mCountTimeList[options1].toInt() * 60
                                    }.setLayoutRes(R.layout.dialog_rope_count_time) { v ->
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
                                        .setSelectOptions(mCountTimeList.indexOf(mViewModel2.mCountTime.let { if (it == -1) 10 else it }.toString()))
                                        .setLineSpacingMultiplier(3.0F)
                                        .setTextColorCenter(R.color.color_e1251b)
                                        .setOutSideCancelable(true)//点击外部dismiss default true
                                        .isDialog(true)//是否显示为对话框样式
                                        .build()
                                    mCountTimeDialog?.setPicker(mCountTimeList)
                                    val lp =
                                        mCountTimeDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
                                    lp.leftMargin = 0
                                    lp.rightMargin = 0
                                    mCountTimeDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
                                    mCountTimeDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
                                    mCountTimeDialog?.show()
                                } else if (Mode.byCountNumber == modes[position]) {
                                    EditDialog(context).apply {
                                        title = "设置倒计数数量"
                                        hint = "请输入数量"
                                        leftBtnText = "取消"
                                        rightBtnText = "确定"
                                        inputType = InputType.TYPE_CLASS_NUMBER
                                        content = "100"
                                        listener = object : DialogClickListener.DefaultLisener() {
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
                                        }
                                    }.showPopupWindow()

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
                LogUtils.d("切换模式成功:${StringUtils.toHex(value)}")
                ToastUtils.showShort("切换${getString(mode.msgId)}成功")
                popupWindow.dismiss()
            }
        })
    }

    fun startConnectOrCancel() {
        if (mViewModel2.state == State.disconnected) {
            startConnect()
        } else {
            disconnect()
        }
    }

    fun startExercise() {
        if (mViewModel2.deviceState == RopeVM.DeviceState.SHUTDOWN) {
            showToast("设备已关机，请先开机才能训练")
            mViewModel2.sendGetBatteryCmd()
            return
        }
        mViewModel2.reset()
        mViewModel2.setIsStart(true)
        mViewModel2.changeExercise(RopeVM.MotionState.RESUME)
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
                }
            }
        }.show()
    }

    fun resumeOrPauseExercise() {
        if (mViewModel2.deviceState == RopeVM.DeviceState.MOTION_RESUME) {
            mViewModel2.changeExercise(RopeVM.MotionState.PAUSE)
        } else {
            mViewModel2.changeExercise(RopeVM.MotionState.RESUME)
        }
    }

}