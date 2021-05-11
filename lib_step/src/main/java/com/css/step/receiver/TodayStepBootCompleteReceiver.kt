package com.css.step.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.css.step.service.TodayStepService
import com.css.step.utils.Logger

class TodayStepBootCompleteReceiver: BroadcastReceiver() {
    private val TAG = "TodayStepBootCompleteReceiver"

    override fun onReceive(context: Context, intent: Intent?) {
        val todayStepIntent = Intent(context, TodayStepService::class.java)
        todayStepIntent.putExtra(TodayStepService().INTENT_NAME_BOOT, true)
        context.startService(todayStepIntent)
        Logger().e(TAG, "TodayStepBootCompleteReceiver")
    }
}