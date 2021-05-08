package com.css.base.uibase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.css.base.ActivityHolder
import com.css.base.uibase.base.BaseWonderActivity
import com.css.base.uibase.viewmodel.BaseViewModel

abstract class BaseActivity <VM : BaseViewModel,VB: ViewBinding> : BaseWonderActivity<VM,VB>(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.addActivity(this)
    }

    override fun onStop() {
        super.onStop()
        if (this.isFinishing) ActivityHolder.removeActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}