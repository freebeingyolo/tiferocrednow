package com.css.ble.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.net.aicare.algorithmutil.AlgorithmUtil
import cn.net.aicare.algorithmutil.BodyFatData
import com.css.base.uibase.viewmodel.BaseViewModel
import com.css.ble.bean.WeightInfo
import com.css.service.utils.WonderCoreCache


class WeightMeasureVM : BaseViewModel() {

    private val weightInfo: LiveData<WeightInfo> by lazy {
        MutableLiveData<WeightInfo>().apply {
            value = WeightInfo(60.0, 500)
        }
    }


    fun getBodyFatData(): BodyFatData {
        var userInfo = WonderCoreCache.getUserInfo()
        val sex = userInfo.setInt
        val age = userInfo.age.toInt()
        val weight_kg = weightInfo.value!!.weight
        val height_cm = userInfo.stature.toInt()
        val adc = weightInfo.value!!.adc
        var data: BodyFatData = AlgorithmUtil.getBodyFatData(AlgorithmUtil.AlgorithmType.TYPE_AICARE, sex, age, weight_kg, height_cm, adc);
        return data
    }

    fun getBodyFatDataList(): List<Map<String, Any?>> {
        var data: BodyFatData = getBodyFatData();
        var datas = mutableListOf<Map<String, Any?>>()
        var clazz = data.javaClass
        for (m in clazz.declaredFields) {
            m.isAccessible = true
            var map = mutableMapOf<String, Any?>()
            map["key"] = m.name
            map["judge"] = ""
            map["value"] = m.get(data)
            datas.add(map)
        }
        return datas
    }
}