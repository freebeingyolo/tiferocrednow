package com.css.step.data

/**
 * Created by chenPan
 * Date: 2021/5/11
 * To do:
 */
class StepEntity() {
    var curDate: String? = null // 当天的日期
    var steps: String? = null   // 存入系统计步器的步数

    constructor(curDate: String, steps: String) : this() {
        this.curDate = curDate
        this.steps = steps
    }

    override fun toString(): String {
        return "StepEntity{" +
                "curDate='" + curDate + '\'' +
                ", steps=" + steps +
                '}'
    }
}