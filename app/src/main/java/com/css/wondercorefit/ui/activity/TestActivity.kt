package com.css.wondercorefit.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.LogUtils
import com.css.base.constants.QRScanConstants
import com.css.base.uibase.BaseActivity
import com.css.wondercorefit.R
import com.css.wondercorefit.databinding.ActivityTestActvityBinding
import com.css.wondercorefit.viewmodel.TestViewModel

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
        return TestViewModel()
    }

    override fun initViewBinding(): ActivityTestActvityBinding =
        ActivityTestActvityBinding.inflate(layoutInflater)
}