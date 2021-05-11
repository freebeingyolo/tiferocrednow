package com.css.step.utils

import android.text.TextUtils
import android.util.Log

class Logger {
    private val TAG = "Logger"

    var sIsDebug: Boolean = true

    fun v(message: String?) {
        if (sIsDebug) Log.v(TAG, message!!)
    }

    fun v(tag: String?, message: String?) {
        if (sIsDebug) Log.v(tag, message!!)
    }

    fun d(message: String?) {
        if (sIsDebug) Log.d(TAG, message!!)
    }

    fun i(message: String?) {
        if (sIsDebug) Log.i(TAG, message!!)
    }

    fun i(tag: String?, message: String?) {
        if (sIsDebug) Log.i(tag, message!!)
    }

    fun w(message: String?) {
        if (sIsDebug) Log.w(TAG, message!!)
    }

    fun w(tag: String?, message: String?) {
        if (sIsDebug) Log.w(tag, message!!)
    }

    fun e(message: String?) {
        if (sIsDebug) Log.e(TAG, message!!)
    }

    fun e(tag: String?, message: String?) {
        if (sIsDebug) Log.e(tag, message!!)
    }

    fun d(tag: String?, message: String?) {
        if (!TextUtils.isEmpty(message) && sIsDebug) {
            Log.d(if (TextUtils.isEmpty(tag)) TAG else tag, message!!)
        }
    }
}