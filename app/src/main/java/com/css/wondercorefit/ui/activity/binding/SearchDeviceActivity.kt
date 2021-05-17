package com.css.wondercorefit.ui.activity.binding

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivitySearchDeviceBinding
import com.css.wondercorefit.ui.activity.index.ABRollerActivity

class SearchDeviceActivity : BaseActivity<DefaultViewModel, ActivitySearchDeviceBinding>(),
    OnToolBarClickListener {

    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, SearchDeviceActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setWhiteFakeStatus(R.id.ll_parent, false)
        mViewBinding.toolBarView.setCenterText("测量结果")
        mViewBinding.toolBarView.setToolBarClickListener(this)
    }
    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivitySearchDeviceBinding =
        ActivitySearchDeviceBinding.inflate(layoutInflater,parent,false)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }
}