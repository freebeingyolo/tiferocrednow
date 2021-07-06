package com.css.ble.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.ble.databinding.ActivityAbrollerBinding

class ABRollerActivity : BaseActivity<DefaultViewModel, ActivityAbrollerBinding>() ,
    OnToolBarClickListener {
    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, ABRollerActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mViewBinding.toolBarView.setCenterText("测量结果")
        mViewBinding.toolBarView.setToolBarClickListener(this)
    }
    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityAbrollerBinding =
        ActivityAbrollerBinding.inflate(layoutInflater,parent,false)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }

}