package com.css.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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