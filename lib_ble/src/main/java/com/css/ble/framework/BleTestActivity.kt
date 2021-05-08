package com.css.ble.framework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.css.ble.R
import com.pingwang.bluetoothlib.BaseBlueToothActivity

class BleTestActivity : BaseBlueToothActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_test)
    }

    override fun onScanTimeOut() {

    }

    override fun onServiceSuccess() {

    }

    override fun onServiceErr() {

    }

    override fun unbindServices() {

    }

    override fun onBroadCastData(p0: String?, p1: String?, p2: ByteArray?, p3: Boolean) {

    }

}