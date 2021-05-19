package com.css.qrscan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.css.base.constants.QRScanConstants
import com.css.base.uibase.BaseActivity
import com.css.qrscan.databinding.ActivityQrscanMainBinding
import com.css.qrscan.viewmodel.QrScanViewModel
import com.css.service.router.ARouterConst
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan

@Route(path = ARouterConst.PATH_APP_QRSCAN)
class QrScanActivity : BaseActivity<QrScanViewModel, ActivityQrscanMainBinding>() {

    var remoteView: RemoteView? = null;
    val permissions: Array<String> =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var metrics = resources.displayMetrics;
        var screenW = metrics.widthPixels;
        var screenH = metrics.heightPixels;
        var rect = Rect().apply {
            var scanFrameSize = (Math.min(screenW, screenH) * 0.8).toInt()
            this.left = (screenW / 2 - scanFrameSize / 2).toInt()
            this.right = (screenW / 2 + scanFrameSize / 2).toInt()
            this.top = (screenH / 2 - scanFrameSize / 2).toInt()
            this.bottom = (screenH / 2 + scanFrameSize / 2).toInt()
            var lp = mViewBinding.scanWindow.layoutParams
            lp.width = scanFrameSize
            lp.height = scanFrameSize
            mViewBinding.scanWindow.layoutParams = lp
        }
        remoteView = RemoteView.Builder().setContext(this).setBoundingBox(rect)
            .setFormat(HmsScan.QRCODE_SCAN_TYPE).build()
        remoteView?.setOnResultCallback { result ->
            if (result.isNotEmpty() && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                var rst = result[0]
                LogUtils.d("rst --> ${rst.originalValue}")
                val intent = Intent().apply {
                    putExtra(
                        QRScanConstants.QRSCAN_RESULT,
                        result[0].originalValue
                    )
                }
                setResult(Activity.RESULT_OK, intent)
                this.finish()
            }
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.CENTER;
        mViewBinding.scannerContainer.addView(remoteView, params)
        remoteView?.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        remoteView?.onStart()
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        remoteView?.onPause()
    }


    override fun onStop() {
        super.onStop()
        remoteView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteView?.onDestroy()
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.isGranted(*permissions)) {
                LogUtils.dTag("camera's permissions has been permitted")
                return;
            }
            this.requestPermissions(
                permissions, QRScanConstants.SCAN_REQUESTCODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.size < 2
            || grantResults[0] != PackageManager.PERMISSION_GRANTED
            || grantResults[1] != PackageManager.PERMISSION_GRANTED
        ) {
            var dlg = AlertDialog.Builder(baseContext)
                .setMessage(R.string.qrscan_require_permission)
                .setNegativeButton(R.string.exit) { _, _ -> this.finish() }
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions() }
                .create();
            dlg.show()
            return
        }
        if (requestCode == QRScanConstants.SCAN_REQUESTCODE) {
            LogUtils.d("requestPermissions success")
        }
    }

    override fun initViewModel(): QrScanViewModel {
        return ViewModelProvider(this).get(QrScanViewModel::class.java)
    }

    override fun initViewBinding(inflater: LayoutInflater, parent: ViewGroup?): ActivityQrscanMainBinding =
        ActivityQrscanMainBinding.inflate(layoutInflater,parent,false)
}