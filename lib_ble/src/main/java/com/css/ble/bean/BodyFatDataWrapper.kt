package com.css.ble.bean

import cn.net.aicare.algorithmutil.BodyFatData
import com.css.service.data.UserData
import com.css.service.utils.WonderCoreCache

/**
 * @author yuedong
 * @date 2021-05-20
 */
class BodyFatDataWrapper(var data: BodyFatData, var weight: Float, var ui: UserData) {

    val bodyScore = if (bmi < 21.6) (bmi / 21.6 * 100).toInt()
    else (21.6 / data.bmi * 100).toInt()

    val output = arrayOf(
        arrayOf("肥胖度", feipangdengjiJudge, String.format("%.1f%%", feipangdengji * 100)),
        arrayOf("体重", tizhongJudge, String.format("%.1fkg", weight)),
        arrayOf("BMI", bmiJudge, String.format("%.1f", bmi)),
        arrayOf("体脂率", tizhilvJudge, String.format("%.1f%%", tizhilv * 100)),
        arrayOf("脂肪重量", zhifangliangJudge, String.format("%.1fkg", zhifangliang)),
        arrayOf("骨骼肌率", gugejilvJudge, String.format("%.1f%%", gugejilv * 100)),
        arrayOf("肌肉率", jiroulvJudge, String.format("%.1f%%", jiroulv * 100)),
        arrayOf("肌肉重量", jirouzhongliangJudge, String.format("%.1fkg", jirouzhongliang)),
        arrayOf("内脏脂肪", neizhangzhifangJudge, String.format("%d级", neizhangzhifang)),
        arrayOf("水份", shuifenJudge, String.format("%.1f%%", shuifen * 100)),
        arrayOf("水含量", shuihanlinagJudge, String.format("%.1fkg", shuihanliangKg)),
        arrayOf("基础代谢", jichudaixieJudge, String.format("%.1fkcal", jichudaixielv)),
        arrayOf("骨量", guliangJudge, String.format("%.1fkg", guliang)),
        arrayOf("蛋白质", danbaiJudge, String.format("%.1f%%", danbai * 100)),
        arrayOf("去脂体重", "", String.format("%.1fkg", quzhitizhong)),
        arrayOf("身体年龄", "", String.format("%d岁", shentinianling)),
    )
    val weightProgress
        get() = run {
            var ui = WonderCoreCache.getUserInfo()
            var fi = WeightBondData.firstWeightInfo!!
            //橙色/整圆=（初始体重-当前体重）/（初始体重-目标体重）
            Math.max(Math.min((fi.weight - weight) / (fi.weight - WonderCoreCache.getUserInfo().targetWeightFloat), 1f), 0f) * 100
        }

    //肥胖等级
    val feipangdengji get() = (weight - standardWeight) / standardWeight
    val feipangdengjiJudge
        get() = run {
            var input = feipangdengji
            val keyRegion = floatArrayOf(-0.2f, -0.1f, 0.1f, 0.2f, Float.MAX_VALUE)
            val valueRegion = arrayOf("体重不足", "偏瘦", "标准", "偏重", "超重")
            findT(input, keyRegion, valueRegion)
        }
    val tizhongJudge
        get() = run {
            var height = ui.statureFloat
            val input = weight
            val keyRegion = floatArrayOf(18.5f * height * height, 25 * height * height, 30 * height * height, Float.MAX_VALUE)
            val valueRegion = arrayOf("偏瘦", "标准", "偏胖", "肥胖")
            findT(input, keyRegion, valueRegion)
        }

    val bmi get() = data.bmi
    val bmiJudge
        get() = run {
            val input = bmi
            val keyRegion = floatArrayOf(18.5f, 25f, 30f, Float.MAX_VALUE)
            val valueRegion = arrayOf("偏瘦", "标准", "偏胖", "肥胖")
            findT(input, keyRegion, valueRegion)
        }

    //体脂率
    val tizhilv get() = data.bfr / 100f
    val tizhilvJudge
        get() = run {
            val input = data.bfr / 100f
            val keyRegion = when (ui.sex) {
                "男" -> {
                    if (ui.ageInt < 30) {
                        floatArrayOf(0.10f, 0.21f, 0.26f, Float.MAX_VALUE)
                    } else {
                        floatArrayOf(0.11f, 0.22f, 0.27f, Float.MAX_VALUE)
                    }
                }
                else -> {
                    if (ui.ageInt < 30) {
                        floatArrayOf(0.16f, 0.24f, 0.30f, Float.MAX_VALUE)
                    } else {
                        floatArrayOf(0.19f, 0.27f, 0.30f, Float.MAX_VALUE)
                    }
                }
            }
            val valueRegion = arrayOf("偏低", "标准", "偏高", "高")
            findT(input, keyRegion, valueRegion)
        }

    //脂肪量
    val zhifangliang get() = weight * data.bfr / 100f
    val zhifangliangJudge get() = tizhilvJudge

    //骨骼肌率
    val gugejilv get() = 22.6f / 100f
    val gugejilvJudge
        get() = run {
            val input = gugejilv
            val keyRegion = intArrayOf(25, 30, Int.MAX_VALUE)
            val valueRegion = arrayOf("不足", "标准", "优")
            findT(input, keyRegion, valueRegion)
        }

    //肌肉率rate of muscle
    val jiroulv get() = data.rom / 100f
    val jiroulvJudge
        get() = run {
            val input = jiroulv
            val keyRegion = when (ui.sex) {
                "男" -> {
                    floatArrayOf(0.4f, 0.6f, Float.MAX_VALUE)
                }
                else -> {
                    floatArrayOf(0.3f, 0.5f, Float.MAX_VALUE)
                }
            }
            val valueRegion = arrayOf("不足", "标准", "优")
            findT(input, keyRegion, valueRegion)
        }

    //肌肉重量
    val jirouzhongliang get() = weight * data.rom / 100f
    val jirouzhongliangJudge get() = jiroulvJudge

    //内脏脂肪
    val neizhangzhifang get() = data.uvi
    val neizhangzhifangJudge
        get() = run {
            val input = neizhangzhifang
            val keyRegion = floatArrayOf(9f, 14f, Float.MAX_VALUE)
            val valueRegion = arrayOf("标准", "警惕", "危险")
            findT(input, keyRegion, valueRegion)
        }

    //水份
    val shuifen get() = data.vwc / 100f
    val shuifenJudge
        get() = run {
            val input = shuifen
            val keyRegion = when (ui.sex) {
                "男" -> {
                    floatArrayOf(0.55f, 0.65f, Float.MAX_VALUE)
                }
                else -> {
                    floatArrayOf(0.45f, 0.60f, Float.MAX_VALUE)
                }
            }
            val valueRegion = arrayOf("不足", "标准", "优")
            findT(input, keyRegion, valueRegion)
        }

    //水含量
    val shuihanliangKg get() = data.vwc * weight / 100f
    val shuihanlinagJudge get() = shuifenJudge

    //基础代谢
    val jichudaixielv get() = data.bmr * 1.0f
    val jichudaixieJudge
        get() = run {
            val input = data.bmr
            val BMRStandard = if (ui.ageInt < 3) {
                if (ui.sex == "男") 60.9f * weight - 54 else 61.0f * weight - 51
            } else if (ui.ageInt < 10) {
                if (ui.sex == "男") 22.7f * weight + 495 else 22.5f * weight + 499
            } else if (ui.ageInt < 18) {
                if (ui.sex == "男") 17.5f * weight + 651 else 12.2f * weight + 746
            } else if (ui.ageInt < 30) {
                if (ui.sex == "男") 15.3f * weight + 679 else 14.7f * weight + 496
            } else {
                if (ui.sex == "男") 11.6f * weight + 879 else 8.7f * weight + 820
            }
            val keyRegion = floatArrayOf(BMRStandard, Float.MAX_VALUE)
            val valueRegion = arrayOf("偏低", "优")
            findT(input, keyRegion, valueRegion)
        }

    //骨量
    val guliang get() = data.bm
    val guliangJudge
        get() = run {
            val input = data.bm
            val keyRegion = when (ui.sex) {
                "男" -> {
                    if (weight < 60) {
                        floatArrayOf(2.4f, 2.6f, Float.MAX_VALUE)
                    } else if (weight < 75) {
                        floatArrayOf(2.8f, 3.0f, Float.MAX_VALUE)
                    } else {
                        floatArrayOf(3.1f, 3.3f, Float.MAX_VALUE)
                    }
                }
                else -> {
                    if (weight < 45) {
                        floatArrayOf(1.7f, 1.9f, Float.MAX_VALUE)
                    } else if (weight < 60) {
                        floatArrayOf(2.1f, 2.3f, Float.MAX_VALUE)
                    } else {
                        floatArrayOf(2.4f, 2.6f, Float.MAX_VALUE)
                    }
                }
            }
            val valueRegion = arrayOf("不足", "标准", "优")
            findT(input, keyRegion, valueRegion)
        }

    //蛋白质
    val danbai get() = data.pp / 100f
    val danbaiJudge
        get() = run {
            val input = data.pp / 100f
            val keyRegion = when (ui.sex) {
                "男" -> {
                    floatArrayOf(0.16f, 0.18f, Float.MAX_VALUE)
                }
                else -> {
                    floatArrayOf(0.14f, 0.16f, Float.MAX_VALUE)
                }
            }
            val valueRegion = arrayOf("不足", "标准", "优")
            findT(input, keyRegion, valueRegion)
        }

    //去脂体重
    val quzhitizhong get() = (1 - data.bfr / 100f) * weight

    //身体年龄
    val shentinianling get() = data.bodyAge


    private fun <T, R> findT(input: T, keyRegion: FloatArray, valueRegion: Array<R>): R where T : Number, T : Comparable<T> {
        if (keyRegion.size != valueRegion.size) throw IllegalArgumentException(
            "keyRegion's size:${keyRegion.size} != valueRegion's size:${valueRegion.size}"
        )
        var ret: R = valueRegion[0]
        for (i in keyRegion.indices) {
            if (input.toFloat() < keyRegion[i]) {
                ret = valueRegion[i]
                break
            }
        }
        return ret
    }

    private fun <T, R> findT(input: T, keyRegion: DoubleArray, valueRegion: Array<R>): R where T : Number, T : Comparable<T> {
        if (keyRegion.size != valueRegion.size) throw IllegalArgumentException(
            "keyRegion's size:${keyRegion.size} != valueRegion's size:${valueRegion.size}"
        )
        var ret: R = valueRegion[0]
        for (i in keyRegion.indices) {
            if (input.toDouble() < keyRegion[i]) {
                ret = valueRegion[i]
                break
            }
        }
        return ret
    }

    private fun <T, R> findT(input: T, keyRegion: IntArray, valueRegion: Array<R>): R where T : Number, T : Comparable<T> {
        if (keyRegion.size != valueRegion.size) throw IllegalArgumentException(
            "keyRegion's size:${keyRegion.size} != valueRegion's size:${valueRegion.size}"
        )
        var ret: R = valueRegion[0]
        for (i in keyRegion.indices) {
            if (input.toInt() < keyRegion[i]) {
                ret = valueRegion[i]
                break
            }
        }
        return ret
    }

    val standardWeight
        get() = if (ui.sex == "男") {
            ((ui.statureFloat - 80) * 0.7).toFloat()
        } else {
            ((ui.statureFloat - 80) * 0.6).toFloat()
        }
}