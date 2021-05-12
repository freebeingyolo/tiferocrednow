package com.css.ble.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.uibase.BaseFragment
import com.css.ble.databinding.FragmentWeightBoundBinding
import com.css.ble.databinding.FragmentWheelBoundBinding
import com.css.ble.utils.BleUtils
import com.css.ble.viewmodel.WeightBondVM

/**
 * @author yuedong
 * @date 2021-05-12
 */
class WheelBondFragment : BaseFragment<WeightBondVM, FragmentWheelBoundBinding>() {

    //TODO: 2021-05-12 initViewBinding和initViewModel这两个方法能否省掉？

    override fun initViewBinding(inflater: LayoutInflater, viewGroup: ViewGroup?): FragmentWheelBoundBinding {

        return FragmentWheelBoundBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun initViewModel(): WeightBondVM {
        return ViewModelProvider(requireActivity()).get(WeightBondVM::class.java)
    }

}