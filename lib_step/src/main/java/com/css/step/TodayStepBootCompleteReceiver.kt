package com.css.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TodayStepBootCompleteReceiver: BroadcastReceiver() {
    private val TAG = "TodayStepBootCompleteReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        val todayStepIntent = Intent(context, TodayStepService::class.java)
        todayStepIntent.putExtra(TodayStepService().INTENT_NAME_BOOT, true)
        context.startService(todayStepIntent)
        Logger().e(TAG, "TodayStepBootCompleteReceiver")
    }
}