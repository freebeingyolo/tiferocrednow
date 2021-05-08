package com.css.wondercorefit.ui.activity.setting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.css.base.uibase.BaseActivity
import com.css.base.uibase.viewmodel.DefaultYuboViewModel
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityPersonInformationBinding
import com.css.wondercorefit.viewmodel.SplashViewModel

class PersonInformationActivity : BaseActivity<DefaultYuboViewModel,ActivityPersonInformationBinding>() {
    companion object {
        fun starActivity(context: Context) {
            val intent = Intent(context, PersonInformationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView( savedInstanceState: Bundle?) {
        super.initView( savedInstanceState)
    }

    override fun getLayoutResId(): Int = R.layout.activity_person_information

    override fun initViewModel(): DefaultYuboViewModel =
        ViewModelProvider(this).get(DefaultYuboViewModel::class.java)

    override fun initViewBinding(): ActivityPersonInformationBinding =ActivityPersonInformationBinding.inflate(layoutInflater)
}