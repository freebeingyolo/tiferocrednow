package com.css.step.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.css.step.utils.Logger
import com.css.step.utils.PreferencesHelper

class TodayStepShutdownReceiver: BroadcastReceiver() {

    private val TAG = "TodayStepShutdownReceiver"

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            Logger().e(TAG, "TodayStepShutdownReceiver")
            context?.let { PreferencesHelper().setShutdown(it, true) }
        }
    }
}