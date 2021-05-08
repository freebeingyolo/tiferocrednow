package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.inner.OnToolBarClickListener
import com.css.base.uibase.viewmodel.DefaultViewModel
import com.css.base.view.ToolBarView
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityPersonInformationBinding
import com.css.wondercorefit.viewmodel.SplashViewModel

class PersonInformationActivity : BaseActivity<DefaultViewModel,ActivityPersonInformationBinding>(),OnToolBarClickListener {
    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, PersonInformationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView( savedInstanceState: Bundle?) {
        super.initView( savedInstanceState)
        setWhiteFakeStatus(R.id.ll_parent,false)
        mViewBinding.toolBarView.setCenterText( "个人信息")
        mViewBinding.toolBarView.setToolBarClickListener(this)
    }

    override fun initViewModel(): DefaultViewModel =
        ViewModelProvider(this).get(DefaultViewModel::class.java)

    override fun initViewBinding(): ActivityPersonInformationBinding =ActivityPersonInformationBinding.inflate(layoutInflater)

    override fun onClickToolBarView(view: View, event: ToolBarView.ViewType) {
        when (event) {
            //支持默认返回按钮和事件
            ToolBarView.ViewType.LEFT_IMAGE -> {
                finishAc()
            }
        }
    }
}