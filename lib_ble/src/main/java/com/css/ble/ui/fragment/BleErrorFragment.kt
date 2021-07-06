package com.css.ble.ui.fragment

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.css.base.uibase.BaseFragment
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.ble.databinding.LayoutBleErrorBinding
import com.css.ble.utils.FragmentUtils
import com.css.ble.viewmodel.BleEnvVM
import com.css.ble.viewmodel.ErrorType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author yuedong
 * @date 2021-05-17
 */
class BleErrorFragment : BaseFragment<DefaultViewModel, LayoutBleErrorBinding>() {
    private lateinit var errorType: ErrorType;
    private lateinit var builder: Builder


    object Builder {
        var leftTitle: String = ""
        var errorType: ErrorType = ErrorType.SEARCH_TIMEOUT

        fun leftTitle(v: String): Builder {
            leftTitle = v
            return this
        }
        fun errorType(v: ErrorType): Builder {
            errorType = v
            return this
        }

        fun create(): BleErrorFragment {
            var fragment = FragmentUtils.changeFragment(BleErrorFragment::class.java, FragmentUtils.Option.OPT_REPLACE)
            fragment.builder = this
            return fragment
        }
    }

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): LayoutBleErrorBinding {
        return LayoutBleErrorBinding.inflate(inflater, parent, false).also {
            errorType = builder.errorType
            it.error.text = getString(errorType.content)
        }
    }

    override fun enabledVisibleToolBar() = true

    override fun initViewModel(): DefaultViewModel {
        return ViewModelProvider(requireActivity()).get(DefaultViewModel::class.java)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        startSelfDestroy()
        setToolBarLeftText(builder.leftTitle)
    }

    private fun startSelfDestroy() {//启动1s自毁
        lifecycleScope.launch {
            delay(1000)
            onBackPressed()
        }
    }

}