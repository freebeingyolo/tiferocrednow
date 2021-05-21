package com.css.ble.bean

import cn.net.aicare.algorithmutil.BodyFatData

/**
 * @author yuedong
 * @date 2021-05-20
 */
class BodyFatDataWrapper(val data: BodyFatData) {


    fun judge(name: String, value: Any): Any {
        return when (name) {
            "bmi" ->        value
            "bfr" ->        value
            "sfr" ->        value
            "uvi" ->        value
            "sfr" ->        value
            "uvi" ->        value
            "rom" ->        value
            "bmr" ->        value
            "bm" ->         value
            "vwc" ->        value
            "bodyAge" ->    value
            "pp" ->         value
            "weight" ->     value
            else -> name
        }
    }
}