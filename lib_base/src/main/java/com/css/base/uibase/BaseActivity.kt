package com.css.base.uibase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.css.base.ActivityHolder
import com.css.base.uibase.base.BaseWonderActivity
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseActivity <VM : BaseViewModel> : BaseWonderActivity<VM>(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.removeActivity(this)
    }
}