package com.css.wondercorefit.viewmodel

import android.text.TextUtils
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.wondercorefit.databinding.ActivityPersonInformationBinding
import java.text.FieldPosition
import java.util.*

class BodyScaleViewModel: BaseViewModel() {
    enum class dataOne(var intPosition: Int, var title: String, var style: String, var information: String ) {
        obesity ( 0,"肥胖度", "标准", "-6.7%"),
        weight(1,"体重","标准","53.2%"),
        bmi(2,"BMI","标准","19.5"),
        fatPercent(3,"体脂率","标准","21.8%"),
        fatWeight(4,"脂肪重量","标准","11.8%"),
        skeletalMuscle(5,"骨骼肌率","优","42.5%"),
        musclePercent(6,"肌肉率","优","22.6%"),
        muscleWeight(7,"肌肉重量","标准","73.8%"),
        visceralFat(8,"内脏脂肪","标准","1.5级"),
        waterContent(9,"水份","标准","52.8%"),
        moistureContent(10,"水含量","标准","28.1kg"),
        basalMetabolism(11,"基础代谢","标准","1245.0kcal"),
        boneMass(12,"骨量","标准","21kg"),
        protein(13,"蛋白质","偏高","21.0%"),
        fatFreeWeight(14,"去脂体重","","41.4kg"),
        physicalAge(15,"身体年龄","","18岁"),;

    }

    val itemBle = arrayListOf(
        dataOne.obesity, dataOne.weight, dataOne.bmi, dataOne.fatPercent,
        dataOne.fatWeight, dataOne.skeletalMuscle, dataOne.musclePercent, dataOne.muscleWeight,
        dataOne.visceralFat, dataOne.waterContent, dataOne.moistureContent, dataOne.basalMetabolism,
        dataOne.boneMass, dataOne.protein, dataOne.fatFreeWeight, dataOne.physicalAge)

    fun setEnum(enumPosition: Int , enumtitle: String, enumStyle:String, enumInformation:String) {
        if (!TextUtils.isEmpty(enumtitle)) {
            itemBle[enumPosition].title = enumtitle
        }
        if (!TextUtils.isEmpty(enumtitle)) {
            itemBle[enumPosition].style = enumStyle
        }
        if (!TextUtils.isEmpty(enumtitle)) {
            itemBle[enumPosition].information = enumInformation
        }
    }

    fun toString(enumPosition:Int): String {
        return itemBle[enumPosition].title + "   " +  itemBle[enumPosition].style + "   " +  itemBle[enumPosition].information
    }
}