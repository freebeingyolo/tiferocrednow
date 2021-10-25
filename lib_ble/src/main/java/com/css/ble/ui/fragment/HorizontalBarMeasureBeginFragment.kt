package com.css.ble.ui.fragment

import LogUtils
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.util.StringUtils
import cn.wandersnail.commons.util.ToastUtils
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutHorizontalbarBinding
import com.css.ble.viewmodel.HorizontalBarVM
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import razerdp.basepopup.BasePopupWindow

/**
 *@author baoyuedong
 *@time 2021-08-06 11:34
 *@description 单杠开始测量
 */
open class HorizontalBarMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) :
    CommonMeasureBeginFragment<LayoutHorizontalbarBinding>(d, vm) {
    private val mViewModel2: HorizontalBarVM get() = mViewModel as HorizontalBarVM
    override val vbCls: Class<LayoutHorizontalbarBinding> get() = LayoutHorizontalbarBinding::class.java

    override fun initData() {
        super.initData()
        mViewBinding!!.let {
            it.view = this
            it.model = mViewModel as HorizontalBarVM
            it.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        lifecycleScope.launchWhenResumed {
            startConnect()
        }
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
                            val modes = HorizontalBarVM.Mode.values()
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
}