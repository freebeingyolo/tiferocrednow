package com.css.step.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.css.step.service.TodayStepService
import com.css.step.utils.Logger
import com.css.step.utils.StepAlertManagerUtils

class TodayStepAlertReceive : BroadcastReceiver() {

    val ACTION_STEP_ALERT = "action_step_alert"

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_STEP_ALERT == intent.action) {
            val separate = intent.getBooleanExtra(TodayStepService().INTENT_NAME_0_SEPARATE, false)
            val stepInent = Intent(context, TodayStepService::class.java)
            stepInent.putExtra(TodayStepService().INTENT_NAME_0_SEPARATE, separate)
            context.startService(stepInent)
            StepAlertManagerUtils().set0SeparateAlertManager(context.applicationContext)
            Logger().e("TodayStepAlertReceive", "TodayStepAlertReceive")
        }
    }
}