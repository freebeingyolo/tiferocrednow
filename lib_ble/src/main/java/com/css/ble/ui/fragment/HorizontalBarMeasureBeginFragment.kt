package com.css.ble.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import cn.wandersnail.ble.Request
import cn.wandersnail.ble.callback.WriteCharacteristicCallback
import cn.wandersnail.commons.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
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
class HorizontalBarMeasureBeginFragment(d: DeviceType, vm: BaseDeviceScan2ConnVM) :
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
        if (mViewModel.state == BaseDeviceScan2ConnVM.State.disconnected) {
            mViewModel.connect()
        }
    }

    fun openSwitchSpinner(v: View) {
        val modes = HorizontalBarVM.Mode.values()
        mViewModel2.switchMode(modes[(mViewModel2.mode.ordinal + 1) % modes.size], object : WriteCharacteristicCallback {
            override fun onRequestFailed(request: Request, failType: Int, value: Any?) {
                ToastUtils.showShort("切换模式失败")
            }

            override fun onCharacteristicWrite(request: Request, value: ByteArray) {
                LogUtils.d("切换模式成功:${StringUtils.toHex(value, "")}")
            }
        })

//        val popUpWindow = object : BasePopupWindow(requireContext(),200,200) {
//            override fun onCreateContentView(): View {
//                val view = RecyclerView(requireContext())
//                view.layoutParams = LayoutParams(200, 200)
//                view.layoutManager = LinearLayoutManager(requireContext())
//                val datas = mViewModel2.getModels()
//                view.adapter = object : Adapter<ViewHolder>() {
//                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//                        val item = TextView(requireContext())
//                        item.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
//                        item.textSize = TypedValue.applyDimension(
//                            TypedValue.COMPLEX_UNIT_SP,
//                            25f,
//                            resources.displayMetrics
//                        );
//                        return VH(item)
//                    }
//
//                    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//                        val tv = holder.itemView as TextView
//                        tv.text = datas[position]
//                    }
//
//                    override fun getItemCount(): Int {
//                        return datas.size
//                    }
//
//                    inner class VH(v: View) : ViewHolder(v)
//                }
//                return view
//            }
//        }.setOutSideDismiss(true)
//        popUpWindow.showPopupWindow(mViewBinding!!.modeSwitchGroup)
    }
}