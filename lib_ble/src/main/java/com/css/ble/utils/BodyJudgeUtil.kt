package com.css.ble.utils

object BodyJudgeUtil {



//    private val itemBle = dataOne.values()
//
//    fun toString(enumPosition: Int): String {
//        return itemBle[enumPosition].title + "   " + itemBle[enumPosition].style + "   " + itemBle[enumPosition].information
//    }

    // 	标准体重  	男性：(身高cm－80)×70﹪=标准体重  	女性：(身高cm－70)×60﹪=标准体重
    fun standardWeight(high: Float, sex: String): Float {
        return if (sex == "男") {
            ((high - 80) * 0.7).toFloat()
        } else {
            ((high - 80) * 0.6).toFloat()
        }
    }

    //	体重控制量   体重控制量=实际体重-标准体重
    fun weightControl(weight: Float, high: Float, sex: String): Float {
        var stand = standardWeight(high, sex)
        return weight - stand
    }

    // 去脂体重   去脂体重=（1-体脂率）* 实际体重
    fun fatFreeWeight(weight: Float, fatPercent: Float): Float {
        return (1 - fatPercent) * weight
    }

    //	肌肉量
    fun muscleContent(weight: Float, musclePercent: Float): Float {
        return weight * musclePercent
    }

    //  蛋白量
    fun proteinContent(weight: Float, proteinPercent: Float): Float {
        return weight * proteinPercent
    }

    fun fatLevel(weight: Float, high: Float, sex: String): Float {
        return (weight - standardWeight(high, sex)) / standardWeight(high, sex)
    }

    //  肥胖等级
    fun fatLevelJudge(weight: Float, high: Float, sex: String): String {
        var fatWeight = (weight - standardWeight(high, sex)) / standardWeight(high, sex)
        var fatLevel = ""
        if (fatWeight < -0.2) {
            fatLevel = "体重不足"
        } else if (-0.2 <= fatWeight && fatWeight < -0.1) {
            fatLevel = "偏瘦"
        } else if (-0.1 <= fatWeight && fatWeight <= 0.1) {
            fatLevel = "标准"
        } else if (0.1 < fatWeight && fatWeight <= 0.2) {
            fatLevel = "偏重"
        } else if (0.2 < fatWeight) {
            fatLevel = "超重"
        }
        return fatLevel
    }

    //体重判定
    fun weightJudge(weight: Float, height: Float, sex: String): String {
        var ret = ""
        val input = weight
        if (input < 18.5 * height * height) {
            ret = "偏瘦"
        } else if (18.5 * height * height <= input && input < 25 * height * height) {
            ret = "标准"
        } else if (25 * height * height <= input && input < 30 * height * height) {
            ret = "超重"
        } else {
            ret = "肥胖"
        }
        return ret
    }

    //身体质量指数判定
    fun bmiJudge(bmi: Float): String {
        var ret = ""
        var input = bmi
        if (input < 18.5) {
            ret = "偏瘦"
        } else if (18.5 <= input && input < 25) {
            ret = "标准"
        } else if (25 <= input && input < 30) {
            ret = "超重"
        } else {
            ret = "肥胖"
        }
        return ret
    }

    //体脂率判定
    fun bfrJudge(bfr: Float, sex: String, age: Int): String {
        var standard = ""
        if (sex == "男") {
            if (age < 30) {
                standard = if (bfr < 0.1) {
                    "偏低"
                } else if (0.1 <= bfr && bfr < 0.21) {
                    "标准"
                } else if (0.21 <= bfr && bfr < 0.26) {
                    "偏高"
                } else {
                    "高"
                }
            } else {
                standard = if (bfr < 0.11) {
                    "偏低"
                } else if (0.11 <= bfr && bfr < 0.22) {
                    "标准"
                } else if (0.22 <= bfr && bfr < 0.27) {
                    "偏高"
                } else {
                    "高"
                }
            }
        } else {
            if (age < 30) {
                standard = if (bfr < 0.16) {
                    "偏低"
                } else if (0.16 <= bfr && bfr < 0.24) {
                    "标准"
                } else if (0.24 <= bfr && bfr < 0.30) {
                    "偏高"
                } else {
                    "高"
                }
            } else {
                standard = if (bfr < 0.19) {
                    "偏低"
                } else if (0.19 <= bfr && bfr < 0.27) {
                    "标准"
                } else if (0.27 <= bfr && bfr < 0.30) {
                    "偏高"
                } else {
                    "高"
                }
            }

        }
        return standard
    }

    //脂肪重量
    fun fatMassJudge(bfr: Float, sex: String, age: Int) = ::bfrJudge


    fun musleRate(rate: Float, sex: String) {
        when (sex) {
            "男" -> {
            }
        }
    }

}