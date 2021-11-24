package com.css.ble.ui.fragment

import LogUtils
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.ContextCompat
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.css.base.dialog.CommonAlertDialog
import com.css.base.dialog.inner.DialogClickListener
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.bean.WeightBondData
import com.css.ble.databinding.LayoutHorizontalbar2Binding
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.tencent.bugly.Bugly
import razerdp.basepopup.BasePopupWindow

/**
 *@author baoyuedong
 *@time 2021-08-06 11:34
 *@description 单杠开始测量
 */
open class HorizontalBar2MeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) :
    CommonMeasureBeginFragment<LayoutHorizontalbar2Binding>(d, vm) {
    private val mViewModel2: HorizontalBarVM get() = mViewModel as HorizontalBarVM
    override val vbCls: Class<LayoutHorizontalbar2Binding> get() = LayoutHorizontalbar2Binding::class.java

    override fun initData() {
        super.initData()
        mViewBinding!!.let {
            it.view = this
            it.model = mViewModel as HorizontalBarVM
            it.lifecycleOwner = viewLifecycleOwner
        }
        WonderCoreCache.getLiveData<WeightBondData>(CacheKey.LAST_WEIGHT_INFO).observe(viewLifecycleOwner) {
            mViewModel2.writeWeight()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }


    fun openSwitchSpinner(v: View) {
        val anchorView = mViewBinding!!.modeContainer
        val popUpWindow =
            object : BasePopupWindow(requireContext(), anchorView.width, AbsListView.LayoutParams.WRAP_CONTENT) {
                override fun onCreateContentView(): View {
                    val listView = ListView(requireContext())
                    listView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_while_radius4)
                    listView.divider = null
                    val datas = mViewModel2.getModels()
                    listView.adapter = object :
                        ArrayAdapter<String>(requireContext(), R.layout.layout_device_mode_switch_item, datas) {

                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val ret = super.getView(position, convertView, parent)
                            ret.setOnClickListener {
                                val modes = HorizontalBarVM.Mode.values()
                                mViewModel2.switchMode(modes[position], object : WriteCharacteristicCallback {
                                    override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                                        ToastUtils.showShort("切换模式失败")
                                        popupWindow.dismiss()
                                    }

                                    override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                                        LogUtils.d("切换模式成功:${StringUtils.toHex(value)}")
                                        popupWindow.dismiss()
                                    }
                                })
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

    fun resumeOrPauseExercise() {
        if ("暂停训练" == mViewBinding?.pauseExercise?.text) {
            mViewModel2.changeExercise(HorizontalBarVM.MotionState.PAUSE)
            mViewBinding?.pauseExercise?.text = "开始训练"
        } else {
            mViewModel2.changeExercise(HorizontalBarVM.MotionState.RESUME)
            mViewBinding?.pauseExercise?.text = "暂停训练"
        }
    }

    fun stopExercise() {
        CommonAlertDialog(Bugly.applicationContext).apply {
            type = CommonAlertDialog.DialogType.Confirm
            gravity = Gravity.BOTTOM
            title = "结束训练"
            content = "确认立即结束训练吗？"
            leftBtnText = "取消"
            rightBtnText = "确定"
            listener = object : DialogClickListener.DefaultLisener() {
                override fun onRightBtnClick(view: View) {
                    super.onRightBtnClick(view)
                    mViewModel2.changeExercise(HorizontalBarVM.MotionState.STOP)
                }
            }
        }.show()
    }
    fun startExercise(){
        mViewModel2.changeExercise(HorizontalBarVM.MotionState.RESUME)
    }

}