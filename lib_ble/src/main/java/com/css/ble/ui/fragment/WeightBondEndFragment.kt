package com.css.ble.ui.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.css.base.uibase.BaseFragment
import com.css.ble.R
import com.css.ble.databinding.LayoutWeightBondBondedBinding
import com.css.ble.viewmodel.WeightBondVM
import com.css.service.router.ARouterConst
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

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondBondedBinding {
        return LayoutWeightBondBondedBinding.inflate(inflater, parent, false).apply {
            //返回主页
            back.setOnClickListener {
                ARouter.getInstance().build(ARouterConst.PATH_APP_MAIN).navigation()
            }
        }
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initData() {
        super.initData()
        startSelfDestroy()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
    }

    private fun startSelfDestroy() {
        var sec = 3
        lifecycleScope.launch {
            flow {
                for (i in sec downTo 0) {
                    emit(i)
                    delay(1000)
                }
            }.collect {
                mViewBinding!!.countdownTv.text = Html.fromHtml(getString(R.string.count_down_tips, it))
                if (it <= 0) {
                    mViewBinding!!.back.performClick()
                }
            }
        }
    }

    override fun enabledVisibleToolBar() = true
}