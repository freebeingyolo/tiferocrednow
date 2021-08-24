package com.css.ble.ui.fragment

import LogUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.ble.R
import com.css.ble.databinding.LayoutWeightBondFoundBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.WeightBondVM

/**
 * @author yuedong
 * @date 2021-05-17
 */
class WeightBondDoingFragment : BaseWeightFragment<WeightBondVM, LayoutWeightBondFoundBinding>() {

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutWeightBondFoundBinding {
        return LayoutWeightBondFoundBinding.inflate(inflater, parent, false).apply {
            research.setOnClickListener {
                LogUtils.d("research#mViewModel.state:${mViewModel.state.value}")
                if (mViewModel.state.value == WeightBondVM.State.begin) return@setOnClickListener
                mViewModel.stopScanBle()
                mViewModel.state.value = WeightBondVM.State.begin
            }
            bond.setOnClickListener {
                mViewModel.bindDevice(
                    { _, _ ->
                    },
                    { _, msg, _ ->
                        showCenterToast("$msg")
                        //showNetworkErrorDialog(msg)
                    })
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setToolBarLeftTitle(getString(R.string.device_weight))
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

    override fun initData() {
        super.initData()
        mViewModel.bondData.observe(viewLifecycleOwner) {
            mViewBinding!!.foundWeight.text = String.format("%.1fkg", it.weightKg)
        }
        mViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                WeightBondVM.State.begin -> {
                    FragmentUtils.changeFragment(WeightBondBeginFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
                WeightBondVM.State.done -> {
                    mViewModel.stopScanBle()
                    FragmentUtils.changeFragment(WeightBondEndFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
                }
            }
        }
    }

    override fun enabledVisibleToolBar() = true
}