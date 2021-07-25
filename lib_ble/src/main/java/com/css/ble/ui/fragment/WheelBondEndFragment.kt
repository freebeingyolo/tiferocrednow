package com.css.ble.ui.fragment

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.ble.bean.DeviceType
import com.css.ble.databinding.LayoutWeightBondBondedBinding
import com.css.ble.viewmodel.WheelMeasureVM
import com.css.service.router.ARouterConst
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WheelBondEndFragment : BaseDeviceFragment<WheelMeasureVM, LayoutWeightBondBondedBinding>(DeviceType.WHEEL) {
    override val vmCls get() = WheelMeasureVM::class.java
    override val vbCls get() = LayoutWeightBondBondedBinding::class.java

    private var selfDestroyJob: Job? = null

    override fun initViewModel(): WheelMeasureVM  = WheelMeasureVM

    override fun initData() {
        super.initData()
        selfDestroyJob = startSelfDestroy()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        //返回主页
        mViewBinding!!.back.setOnClickListener {
            ARouter.getInstance() //测量首页
                .build(ARouterConst.PATH_APP_BLE_WHEELMEASURE)
                .withBoolean("autoConnect", true)
                .navigation(requireContext(), object : NavCallback() {
                    override fun onArrival(postcard: Postcard?) {
                        //Log.d("MainActivity" , "onArrival : " + postcard?.getPath());
                        val activities = ActivityUtils.getActivityList()
                        for (i in 0 until activities.size - 1) {//后加的activity在队首
                            ActivityUtils.finishActivity(activities[i])
                        }
                    }
                })
            selfDestroyJob?.cancel()
        }
        mViewBinding!!.tv1.text = getString(R.string.wheel_bonded_tip)
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

    override fun enabledVisibleToolBar() = true
}