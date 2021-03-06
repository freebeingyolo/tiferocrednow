package com.css.ble.bean

import cn.net.aicare.algorithmutil.AlgorithmUtil
import com.css.service.data.HistoryWeight
import com.css.service.utils.WonderCoreCache
import java.util.*

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
 * @param weightUnit     体重单位 0:kg 1:斤 6:lb 4:st:lb   Weight unit 0: kg 1:catty 6: lb 4: st: lb
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
    var weightUnit: Int = 0 //0:kg 1:斤 6:lb 4:st:lb
    var weightDecimal: Int = 0 //小数点
    var weightStatus: Int = 0
    var weightNegative: Int = 0
    var weight: Int = 0
    var adc: Int = 0
    var algorithmId: Int = 0
    var tempNegative: Int = 0
    var temp: Int = 0
    var timestamp: Long = 0

    constructor(d: HistoryWeight) : this() {
        this.adc = d.adc
        var f = d.bodyWeight
        var decimal = 0
        while (f.compareTo(f.toInt()) != 0) {
            decimal++
            f *= 10
        }
        this.weight = f.toInt()
        this.weightDecimal = decimal
        this.weightUnit = 0
        this.timestamp = d.weighingDate
    }

    val bodyFatData: BodyFatDataWrapper
        get() = run {
            val userInfo = WonderCoreCache.getUserInfo()
            val sex = userInfo.sexInt
            val age = userInfo.ageInt
            val weight_kg = weightKg * 1.0
            val height_cm = userInfo.height.toInt()
            val adc = adc
            val ret = AlgorithmUtil.getBodyFatData(
                AlgorithmUtil.AlgorithmType.TYPE_AICARE,
                sex,
                age,
                weight_kg,
                height_cm,
                adc
            )
            BodyFatDataWrapper(ret, weightKg, WonderCoreCache.getUserInfo())
        }

    fun getBodyFatDataList(): List<WeightDetailBean> {
        val dataWrapper = bodyFatData
        val datas = mutableListOf<WeightDetailBean>()
        for (m in dataWrapper.output) {
            val map = WeightDetailBean(m[0], m[1], m[2])
            datas.add(map)
        }
        return datas
    }

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
        this.weightNegative = weightNegative
        this.weight = weight
        this.adc = adc
        this.algorithmId = algorithmId
        this.tempNegative = tempNegative
        this.temp = temp
        this.timestamp = System.currentTimeMillis()
    }

    val weightKg: Float
        get() {
            //0:kg 1:斤 6:lb 4:st:lb
            var scale = 1f
            when (weightUnit) {
                1 -> scale *= 0.5f
                4, 6 -> scale *= 0.4536f
            }
            for (i in 0 until weightDecimal) {
                scale *= 0.1f
            }
            return weight * scale
        }
    val weightKgFmt get() = weightKgFmt("%.1f")
    fun weightKgFmt(fmt: String) = String.format(fmt, weightKg)

    override fun toString(): String {
        return "WeightBondData(status=$status, tempUnit=$tempUnit, weightUnit=$weightUnit, weightDecimal=$weightDecimal, weightStatus=$weightStatus, weightNegative=$weightNegative, weight=$weight, adc=$adc, algorithmId=$algorithmId, tempNegative=$tempNegative, temp=$temp)"
    }

}