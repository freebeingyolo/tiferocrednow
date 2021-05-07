package com.css.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.e

class TodayStepShutdownReceiver: BroadcastReceiver() {

    private val TAG = "TodayStepShutdownReceiver"

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            Logger().e(TAG, "TodayStepShutdownReceiver")
            context?.let { PreferencesHelper().setShutdown(it, true) }
        }
    }
}