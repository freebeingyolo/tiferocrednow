package com.css.ble.bean

/**
 * @author yuedong
 * @date 2021-05-14
 */

/**
 * 体重数据(Stabilize weight)
 *
 * @param status         0x00 ：开始测试  start test
 *                       0x00 ：正在测量体重 （此时阻抗数值为 0）  weight is being measured (the impedance value is 0 at this time)
 *                       0x01 ：正在测量阻抗（此时阻抗数值为 0）  impedance is being measured (the impedance value is 0 at this time)
 *                       0x02 ：阻抗测量成功  impedance measurement succeeded
 *                       0x03 ：阻抗测量失败（此时阻抗数值为 0xFFFF） Impedance measurement failed (the impedance value is 0xFFFF at this time)
 *                       0xFF ：测试结束    end of test
 *                       只要Ailink协议才有这些状态。其他协议只要开始测量和结束两种状态   Only the Ailink protocol has these states. Other protocols only need to start measurement and end two states
 * @param tempUnit       温度单位 0=℃ ，1=℉   Temperature unit 0=℃, 1=℉
 * @param weightUnit     体重单位 0:kg 1斤 6:lb 4:st:lb   Weight unit 0: kg 1 catty 6: lb 4: st: lb
 * @param weightDecimal  体重小数点   Weight decimal point
 * @param weightStatus   0：实时重量，1：稳定重量  real-time weight, 1: stable weight
 * @param weightNegative 0 ：正重量; 1 ：负重量   positive weight; 1: negative weight
 * @param weight         原始数据(Raw data)   Raw data
 *                       注意：（单位是ST:LB时，原始数据是lb单位的值。需要自己转换为st:lb）
 *                       Note: (When the unit is ST:LB, the original data is the value of lb unit. You need to convert to st:lb yourself)
 * @param adc            阻抗 65535表示测量阻抗失败   Impedance 65535 means impedance measurement failed
 * @param algorithmId    算法id   Algorithm id
 * @param tempNegative   0 ：正温度;1 ：负单位 ;-1代表不支持  positive temperature; 1: negative unit; -1 means not supported
 * @param temp           温度值,精度 0.1 ;-1代表不支持   Temperature value, accuracy 0.1; -1 means not supported
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

    override fun toString(): String {
        return "WeightBondData(status=$status, tempUnit=$tempUnit, weightUnit=$weightUnit, weightDecimal=$weightDecimal, weightStatus=$weightStatus, weightNegative=$weightNegative, weight=$weight, adc=$adc, algorithmId=$algorithmId, tempNegative=$tempNegative, temp=$temp)"
    }

}