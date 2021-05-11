package com.css.step.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.css.step.receiver.TodayStepAlertReceive
import com.css.step.service.TodayStepService
import java.util.*

class StepAlertManagerUtils {
    private val TAG = "StepAlertManagerUtils"

    /**
     * 设置0点分隔Alert，当前天+1天的0点启动
     *
     * @param application
     */
    fun set0SeparateAlertManager(application: Context) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow: String = DateUtils().dateFormat(calendar.timeInMillis, "yyyy-MM-dd")
        val timeInMillis: Long =
            DateUtils().getDateMillis("$tomorrow 00:00:00", "yyyy-MM-dd HH:mm:ss")
        Logger().e(TAG, DateUtils().dateFormat(timeInMillis, "yyyy-MM-dd HH:mm:ss"))
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i1 = Intent(application, TodayStepAlertReceive::class.java)
        i1.putExtra(TodayStepService().INTENT_NAME_0_SEPARATE, true)
        i1.action = TodayStepAlertReceive().ACTION_STEP_ALERT
        val operation =
            PendingIntent.getBroadcast(application, 0, i1, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, operation)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, operation)
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, timeInMillis] = operation
        }
    }
}
