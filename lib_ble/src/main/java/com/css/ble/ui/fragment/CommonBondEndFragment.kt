package com.css.ble.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutCommonBondBondedBinding
import com.css.ble.viewmodel.base.BaseDeviceScan2ConnVM
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class CommonBondEndFragment(d: DeviceType, val model: BaseDeviceScan2ConnVM) : BaseDeviceFragment<BaseDeviceScan2ConnVM,
        LayoutCommonBondBondedBinding>(d) {
    override val vmCls get() = BaseDeviceScan2ConnVM::class.java
    override val vbCls get() = LayoutCommonBondBondedBinding::class.java
    private var selfDestroyJob: Job? = null
    override fun initViewModel(): BaseDeviceScan2ConnVM = model

    var backListener = View.OnClickListener {
        onBackPressed()
    }

    override fun onBackPressed() {
        mViewModel.workMode = BaseDeviceScan2ConnVM.WorkMode.MEASURE
        //只保留本Activity和首页Activity
        val activities = ActivityUtils.getActivityList()
        for (i in 1 until activities.size - 1) {//后加的activity在队首
            ActivityUtils.finishActivity(activities[i])
        }
        //super.onBackPressed()
    }

    override fun initData() {
        super.initData()
        selfDestroyJob = startSelfDestroy()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //返回主页
        mViewBinding!!.back.setOnClickListener(backListener)
        mViewBinding!!.model = mViewModel
    }

    private fun startSelfDestroy(): Job {
        val sec = 3
        return lifecycleScope.launch {
            flow {
                for (i in sec downTo 0) {
                    emit(i)
                    delay(1000)
                }
            }.collect {
                mViewBinding!!.countdownTv.text = String.format("%d", it)
                if (it <= 0) {
                    mViewBinding!!.back.performClick()
                }
                selfDestroyJob = null
            }
        }
    }

    override fun onStop() {
        super.onStop()
        selfDestroyJob?.cancel()
    }

    override fun enabledVisibleToolBar() = true
}