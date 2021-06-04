package com.css.ble.ui.fragment

import LogUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.callback.NavCallback
import com.alibaba.android.arouter.facade.callback.NavigationCallback
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.css.ble.R
import com.css.ble.databinding.LayoutWeightBondBondedBinding
import com.css.ble.viewmodel.WeightBondVM
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
class WeightBondEndFragment : BaseWeightFragment<WeightBondVM, LayoutWeightBondBondedBinding>() {
    companion object {
        val TAG = "WeightBond"
    }

    private var selfDestroyJob: Job? = null

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondBondedBinding {
        return LayoutWeightBondBondedBinding.inflate(inflater, parent, false).apply {
            //返回主页
            back.setOnClickListener {
                ARouter.getInstance() //测量首页
                    .build(ARouterConst.PATH_APP_BLE_WEIGHTMEASURE)
                    .navigation(requireContext(), object : NavCallback() {
                        override fun onArrival(postcard: Postcard?) {
                            //Log.d("MainActivity" , "onArrival : " + postcard?.getPath());
                            val activities = ActivityUtils.getActivityList()
                            for (i in 0 until activities.size-1) {//后加的activity在队首
                                ActivityUtils.finishActivity(activities[i])
                            }
                        }
                    })
                selfDestroyJob?.cancel()
            }
        }
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initData() {
        super.initData()
        selfDestroyJob = startSelfDestroy()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
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