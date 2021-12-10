package com.shopwonder.jingzaoyd.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.LogUtils
import com.css.base.constants.QRScanConstants
import com.css.base.uibase.BaseActivity
import com.shopwonder.jingzaoyd.R
import com.shopwonder.jingzaoyd.databinding.ActivityTestActvityBinding
import com.shopwonder.jingzaoyd.viewmodel.TestViewModel

class TestActivity : BaseActivity<TestViewModel, ActivityTestActvityBinding>() {

    fun startScan(view: View) {
        var intent = Intent("com.csss.qrscan")
        this.startActivityForResult(intent, QRScanConstants.SCAN_REQUESTCODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            LogUtils.d("onActivityResult failed")
            return
        }
        if (requestCode == QRScanConstants.SCAN_REQUESTCODE) {
            val hmsScan = data.getStringExtra(QRScanConstants.QRSCAN_RESULT)
            Toast.makeText(this, hmsScan, Toast.LENGTH_SHORT).show()
        }
    }

    override fun initViewModel(): TestViewModel {
        return ViewModelProvider(this).get(TestViewModel::class.java)
    }

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityTestActvityBinding =
        ActivityTestActvityBinding.inflate(layoutInflater,parent,false)
}