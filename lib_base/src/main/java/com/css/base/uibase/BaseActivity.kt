package com.css.base.uibase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.css.base.ActivityHolder

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHolder.addActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHolder.removeActivity(this)
    }
}