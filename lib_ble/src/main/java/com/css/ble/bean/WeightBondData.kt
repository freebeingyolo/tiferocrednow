package com.css.ble.bean

/**
 * @author yuedong
 * @date 2021-05-14
 */
class WeightBondData() {
    var status: Int = 0
    var tempUnit: Int = 0
    var weightUnit: Int = 0
    var weightDecimal: Int = 0
    var weightStatus: Int = 0
    var weightNegative: Int = 0
    var weight: Int = 0
    var adc: Int = 0
    var algorithmId: Int = 0
    var tempNegative: Int = 0
    var temp: Int = 0


    fun setValue(
        status: Int,
        tempUnit: Int,
        weightUnit: Int,
        weightDecimal: Int,
        weightStatus: Int,
        weightNegative: Int,
        weight: Int,
        adc: Int,
        algorithmId: Int,
        tempNegative: Int,
        temp: Int
    ) {
        this.status = status
        this.tempUnit = tempUnit
        this.weightUnit = weightUnit
        this.weightDecimal = weightDecimal
        this.weightStatus = weightStatus
        this.weightDecimal = weightDecimal
        this.weightNegative = weightNegative
        this.weight = weight
        this.adc = adc
        this.algorithmId = algorithmId
        this.tempNegative = tempNegative
        this.temp = temp
    }

    fun getWeightKg(){

    }


}