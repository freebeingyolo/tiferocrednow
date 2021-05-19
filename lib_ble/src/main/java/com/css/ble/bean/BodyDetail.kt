package com.css.ble.bean

/**
 * @author yuedong
 * @date 2021-05-13
 */
data class BodyDetail(
    val bfr:Float = 12.1f,  //体脂率
    val bm:Float = 2.6f,    //骨量
    val bmi:Float = 20.7f,  //BMI
    val bmr:Int = 1471,     //基础代谢率
    val bodyAge:Int = 17,   //身体年龄
    val pp:Float = 18.8f,   //蛋白质
    val rom:Float = 47.8f,  //肌肉率
    val sfr:Float = 10.8f,  //皮下脂肪
    val uvi:Int = 4,        //内脏脂肪指数
    val vwc:Float = 64.2f,  //身体水分
){



}