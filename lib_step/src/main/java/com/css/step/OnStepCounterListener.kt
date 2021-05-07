package com.css.step

interface OnStepCounterListener {

    /**
     * 用于显示步数
     * @param step
     */
    fun onChangeStepCounter(step: Int)

    /**
     * 步数清零监听，由于跨越0点需要重新计步
     */
    fun onStepCounterClean()

}