package com.css.ble.ui.fragment

import LogUtils
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutRopeBinding
import com.css.ble.viewmodel.RopeVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.pickerview.builder.OptionsPickerBuilder
import com.css.pickerview.view.OptionsPickerView
import com.tencent.bugly.Bugly.applicationContext
import razerdp.basepopup.BasePopupWindow
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import com.css.ble.viewmodel.RopeVM.Mode

/**
 *@author chanpal
 *@time 2021-11-01
 *@description 跳绳器
 */
class RopeMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) : CommonMeasureBeginFragment<LayoutRopeBinding>(d, vm)  {

    override val vbCls: Class<LayoutRopeBinding> get() = LayoutRopeBinding::class.java
    private val mViewModel2: RopeVM get() = mViewModel as RopeVM
    private var connectControl: Int  = 0
    private var mCountTimeDialog: OptionsPickerView<String>? = null
    private var mCountTimeList = ArrayList<String>()
    private var mCountTime = ""
    private var mCountNumber = ""

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

    fun initUI () {
        when (mViewModel2.state) {
            BaseDeviceScan2ConnVM.State.discovered -> {
                discoveredBLE()
            }
            BaseDeviceScan2ConnVM.State.disconnected -> {
                disconnectBLE()
            }

        }
    }

    fun initState () {
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

    private fun discoveredBLE () {
        mViewModel2.doWriteCharacteristic("f55f06020200")
        mViewModel2.doWriteCharacteristic("f55f06021001")
        mViewModel2.doWriteCharacteristic("f55f10030100001")
        val dateStr = System.currentTimeMillis() / 1000L
        mViewModel2.doWriteCharacteristic("f55f10030100001${StringUtils.toHex(dateStr)}")
        mViewModel2.doWriteCharacteristic("f55f10030100001")
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

    fun disconnectBLE () {
        mViewBinding?.startExercise?.visibility = View.GONE
        mViewBinding?.startExercised?.visibility = View.GONE
        mViewBinding?.startExercise1?.visibility = View.VISIBLE
        mViewBinding?.connectControl?.visibility = View.VISIBLE
        mViewBinding?.connectControl?.text = "连接设备"
        mViewModel2.doWriteCharacteristic("f55f10030100000")
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
        val popUpWindow = object : BasePopupWindow(requireContext(), anchorView.width, AbsListView.LayoutParams.WRAP_CONTENT) {
            override fun onCreateContentView(): View {
                val listView = ListView(requireContext())
                listView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_while_radius4)
                listView.divider = null
                val datas = mViewModel2.getModels()
                listView.adapter = object : ArrayAdapter<String>(requireContext(), R.layout.layout_device_mode_switch_item, datas) {

                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val ret = super.getView(position, convertView, parent)
                        ret.setOnClickListener {
                            val modes = RopeVM.Mode.values()
                            if ("byCountTime" == modes[position].name) {//倒计时长
                                if (mCountTimeDialog == null) {
                                    for (index in 30 downTo 1) {
                                        mCountTimeList.add(index.toString())
                                    }
                                }
                                mCountTimeDialog = OptionsPickerBuilder(
                                    activity
                                ) { options1, options2, options3, v ->
                                    mCountTime = mCountTimeList[options1]
                                }.setLayoutRes(
                                    R.layout.dialog_rope_count_time
                                ) { v ->
                                    var title = v?.findViewById<TextView>(R.id.tv_title)
                                    var cancel = v?.findViewById<TextView>(R.id.btn_cancel)
                                    var submit = v?.findViewById<TextView>(R.id.btn_submit)
                                    title?.text = "设置倒计时时长"
                                    cancel?.setOnClickListener {
                                        mCountTimeDialog?.dismiss()
                                    }
                                    submit?.setOnClickListener {
                                        mCountTimeDialog?.returnData()
                                        mViewModel2.switchMode(modes[position], object : WriteCharacteristicCallback {
                                            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                                                ToastUtils.showShort("切换模式失败")
                                                popupWindow.dismiss()
                                            }

                                            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                                                LogUtils.d("切换模式成功:${StringUtils.toHex(value, "")}")
                                                popupWindow.dismiss()
                                            }
                                        })
                                        mCountTimeDialog?.dismiss()
                                    }
                                }.setLabels("分钟", "", "")
                                    .isCenterLabel(true)
                                    .setSelectOptions(mCountTimeList.indexOf(mCountTime))
                                    .setLineSpacingMultiplier(3.0F)
                                    .setTextColorCenter(Color.parseColor("#F2682A"))
                                    .setOutSideCancelable(true)//点击外部dismiss default true
                                    .isDialog(true)//是否显示为对话框样式
                                    .build()
                                mCountTimeDialog?.setPicker(mCountTimeList)
                                val lp: FrameLayout.LayoutParams = mCountTimeDialog!!.dialogContainerLayout.layoutParams as FrameLayout.LayoutParams
                                lp.leftMargin = 0
                                lp.rightMargin = 0
                                mCountTimeDialog!!.dialog.window?.setGravity(Gravity.BOTTOM)
                                mCountTimeDialog!!.dialog.window?.setWindowAnimations(R.style.picker_view_slide_anim)
                                mCountTimeDialog?.show()
                            } else if ("byCountNumber" == modes[position].name) {
                                CommonAlertDialog(context).apply {
                                    type = CommonAlertDialog.DialogType.Edit
                                    title = "设置倒计数数量"
                                    hint = "请输入数量"
                                    leftBtnText = "取消"
                                    rightBtnText = "确定"
                                    listener = object : DialogClickListener.DefaultLisener() {

                                        override fun onRightEditBtnClick(view: View, content: String?) {
                                            if (content == "") {
                                                showCenterToast("您还未输入倒计数个数")
                                                return
                                            }
                                            var p: Pattern = Pattern.compile("[0-9]*")
                                            var m: Matcher = p.matcher(content)
                                            if(m.matches() ) {
                                                var countNumber = Integer.parseInt(content.toString())
                                                if (countNumber < 1 || countNumber > 500) {
                                                    showCenterToast("倒计数只能是1-500的整数才可开始训练")
                                                    dialog?.dismiss()
                                                } else {
                                                    mCountNumber = content!!
                                                    mViewModel2.switchMode(modes[position], object : WriteCharacteristicCallback {
                                                        override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                                                            ToastUtils.showShort("切换模式失败")
                                                            popupWindow.dismiss()
                                                        }

                                                        override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                                                            LogUtils.d("切换模式成功:${StringUtils.toHex(value, "")}")
                                                            popupWindow.dismiss()
                                                        }
                                                    })
                                                    dialog?.dismiss()
                                                }
                                            } else {
                                                showCenterToast("倒计数只能是1-500的整数才可开始训练")
                                            }
                                        }
                                    }
                                }.show()

                            } else {
                                mViewModel2.switchMode(modes[position], object : WriteCharacteristicCallback {
                                    override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                                        ToastUtils.showShort("切换模式失败")
                                        popupWindow.dismiss()
                                    }

                                    override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                                        LogUtils.d("切换模式成功:${StringUtils.toHex(value, "")}")
                                        popupWindow.dismiss()
                                    }
                                })
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

    fun connectChange () {
        if (connectControl == 0) {
            startConnect()
            connectControl = 1
        } else {
            disconnect()
            connectControl = 0
        }
    }
    fun startExercise () {
        mViewModel2.reset()
        mViewModel2.setIsStart(true)
        when (mViewModel2.mode) {
            Mode.byFree -> {
                mViewModel2.doWriteCharacteristic("f55f060403010000")
            }
            Mode.byCountTime -> {
                if (mCountTime.isEmpty()) {
                    ToastUtils.showShort("请先选择运动模式")
                    return
                } else {
                    val hexTime = StringUtils.toHex(Integer.parseInt(mCountTime) * 60)
                    val data = StringUtils.fillZero(hexTime,4,true)
                    mViewModel2.doWriteCharacteristic("f55f06040302$data")
                }
            }
            Mode.byCountNumber -> {
                if (mCountNumber.isEmpty()) {
                    ToastUtils.showShort("请先选择运动模式")
                    return
                } else {
                    val hexTime2 = StringUtils.toHex(Integer.parseInt(mCountNumber))
                    val data2 = StringUtils.fillZero(hexTime2,4,true)
                    mViewModel2.doWriteCharacteristic("f55f06040303$data2")
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

    fun stopExercise () {
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
                    mViewModel2.changeExercise("06")
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

    fun startExercise2 () {
        if ("暂停训练" == mViewBinding?.pauseExercise?.text) {
            mViewModel2.changeExercise("04")
            mViewBinding?.pauseExercise?.text = "开始训练"
        } else {
            mViewModel2.changeExercise("05")
            mViewBinding?.pauseExercise?.text = "暂停训练"
        }
    }

}