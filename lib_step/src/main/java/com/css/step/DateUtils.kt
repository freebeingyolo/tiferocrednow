package com.css.step

import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    /**
     * 返回一定格式的当前时间
     *
     * @param pattern "yyyy-MM-dd HH:mm:ss E"
     * @return
     */
    fun getCurrentDate(pattern: String?): String? {
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = Date(System.currentTimeMillis())
        return simpleDateFormat.format(date)
    }

    fun getDateMillis(dateString: String?, pattern: String?): Long {
        val sdf = SimpleDateFormat(pattern)
        var millionSeconds: Long = 0
        try {
            millionSeconds = sdf.parse(dateString).time
        } catch (e: ParseException) {
            e.printStackTrace()
        } // 毫秒
        return millionSeconds
    }

    /**
     * 格式化输入的millis
     *
     * @param millis
     * @param pattern yyyy-MM-dd HH:mm:ss E
     * @return
     */
    fun dateFormat(millis: Long, pattern: String?): String {
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date = Date(millis)
        return simpleDateFormat.format(date)
    }

    /**
     * 将dateString原来old格式转换成new格式
     *
     * @param dateString
     * @param oldPattern yyyy-MM-dd HH:mm:ss E
     * @param newPattern
     * @return oldPattern和dateString形式不一样直接返回dateString
     */
    fun dateFormat(
        dateString: String?, oldPattern: String?,
        newPattern: String?
    ): String? {
        val millis: Long = getDateMillis(dateString, oldPattern)
        return if (0L == millis) {
            dateString
        } else dateFormat(millis, newPattern)
    }
}