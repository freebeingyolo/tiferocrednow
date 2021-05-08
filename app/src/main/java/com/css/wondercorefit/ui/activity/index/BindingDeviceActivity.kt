package com.css.wondercorefit.ui.activity.index

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityBindingDeviceBinding

class BindingDeviceActivity : BaseActivity<DefaultViewModel, ActivityBindingDeviceBinding>(),
    OnToolBarClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setWhiteFakeStatus(R.id.ll_parent, false)
        mViewBinding.toolBarView.setCenterText("绑定设备")
        mViewBinding.toolBarView.setToolBarClickListener(this)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(): ActivityBindingDeviceBinding =
        ActivityBindingDeviceBinding.inflate(layoutInflater)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }
}