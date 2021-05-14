package com.css.service.utils

/**
 * 控制点击，避免瞬间点击两下
 * author Ruis
 * date 2019/11/7
 */
class DoubleClickUtils private constructor() {

    private var limitTime = 500
    private var preClickTime: Long = 0

    companion object {
        val instance by lazy { DoubleClickUtils() }
    }

    fun isInvalidClick(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - preClickTime < limitTime) {
            return true
        }
        preClickTime = currentTimeMillis
        return false
    }

}