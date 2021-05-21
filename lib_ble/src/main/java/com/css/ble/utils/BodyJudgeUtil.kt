package com.css.ble.utils

import android.text.TextUtils

object BodyJudgeUtil {

    //    enum class dataOne(var intPosition: Int, var title: String, var style: String, var information: String ) {
//        obesity ( 0,"肥胖度", "标准", "-6.7%"),
//        weight(1,"体重","标准","53.2%"),
//        bmi(2,"BMI","标准","19.5"),
//        fatPercent(3,"体脂率","标准","21.8%"),
//        fatWeight(4,"脂肪重量","标准","11.8%"),
//        skeletalMuscle(5,"骨骼肌率","优","42.5%"),
//        musclePercent(6,"肌肉率","优","22.6%"),
//        muscleWeight(7,"肌肉重量","标准","73.8%"),
//        visceralFat(8,"内脏脂肪","标准","1.5级"),
//        waterContent(9,"水份","标准","52.8%"),
//        moistureContent(10,"水含量","标准","28.1kg"),
//        basalMetabolism(11,"基础代谢","标准","1245.0kcal"),
//        boneMass(12,"骨量","标准","21kg"),
//        protein(13,"蛋白质","偏高","21.0%"),
//        fatFreeWeight(14,"去脂体重","","41.4kg"),
//        physicalAge(15,"身体年龄","","18岁"),;
//
//    }
//
//    private val itemBle = arrayListOf(
//        dataOne.obesity, dataOne.weight, dataOne.bmi, dataOne.fatPercent,
//        dataOne.fatWeight, dataOne.skeletalMuscle, dataOne.musclePercent, dataOne.muscleWeight,
//        dataOne.visceralFat, dataOne.waterContent, dataOne.moistureContent, dataOne.basalMetabolism,
//        dataOne.boneMass, dataOne.protein, dataOne.fatFreeWeight, dataOne.physicalAge
//    )
//
//    fun setEnum(enumPosition: Int , enumtitle: String, enumStyle:String, enumInformation:String) {
//        if (!TextUtils.isEmpty(enumtitle)) {
//            itemBle[enumPosition].title = enumtitle
//        }
//        if (!TextUtils.isEmpty(enumtitle)) {
//            itemBle[enumPosition].style = enumStyle
//        }
//        if (!TextUtils.isEmpty(enumtitle)) {
//            itemBle[enumPosition].information = enumInformation
//        }
//    }
//
//    fun toString(enumPosition:Int): String {
//        return itemBle[enumPosition].title + "   " +  itemBle[enumPosition].style + "   " +  itemBle[enumPosition].information
//    }
//
    // 	标准体重  	男性：(身高cm－80)×70﹪=标准体重  	女性：(身高cm－70)×60﹪=标准体重
    fun standardWeight(high: Float, sex: String): Float {
        return if (sex == "男") {
            ((high - 80) * 0.7).toFloat()
        } else {
            ((high - 80) * 0.6).toFloat()
        }
    }

    // 脂肪量
    fun fatContent(sex: String, age: Int, fatPercent: Float): String {
        var standard = ""
        if (sex == "男") {
            if (age < 30) {
                if (fatPercent < 0.1) {
                    standard = "偏低"
                } else if (0.1 <= fatPercent && fatPercent < 0.21) {
                    standard = "标准"
                } else if (0.21 <= fatPercent || fatPercent < 0.26) {
                    standard = "偏高"
                } else if (0.26 < fatPercent) {
                    standard = "高"
                }
            } else {
                if (fatPercent < 0.11) {
                    standard = "偏低"
                } else if (0.11 <= fatPercent && fatPercent < 0.22) {
                    standard = "标准"
                } else if (0.22 <= fatPercent || fatPercent < 0.27) {
                    standard = "偏高"
                } else if (0.27 < fatPercent) {
                    standard = "高"
                }
            }
        } else {
            if (age < 30) {
                if (fatPercent < 0.16) {
                    standard = "偏低"
                } else if (0.16 <= fatPercent && fatPercent < 0.24) {
                    standard = "标准"
                } else if (0.24 <= fatPercent || fatPercent < 0.30) {
                    standard = "偏高"
                } else if (0.30 < fatPercent) {
                    standard = "高"
                }
            } else {

                if (fatPercent < 0.19) {
                    standard = "偏低"
                } else if (0.19 <= fatPercent && fatPercent < 0.27) {
                    standard = "标准"
                } else if (0.27 <= fatPercent || fatPercent < 0.30) {
                    standard = "偏高"
                } else if (0.30 < fatPercent) {
                    standard = "高"
                }
            }

        }
        return standard
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

    fun fatLeveRate(weight: Float, high: Float, sex: String): Float {
        return (weight - standardWeight(high, sex)) / standardWeight(high, sex)
    }

    //  肥胖等级
    fun fatLevel(weight: Float, high: Float, sex: String): String {
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

    fun musleRate(rate: Float, sex: String) {
        when (sex) {
            "男" -> {
            }
        }
    }

}