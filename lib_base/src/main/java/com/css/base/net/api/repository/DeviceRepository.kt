package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.DeviceData
import com.css.service.data.PullUpData
import retrofit2.http.QueryMap

/**
 *@author baoyuedong
 *@time 2021-07-15 17:34
 *@description
 */
object DeviceRepository {

    private val api: WonderCoreApi.Device by lazy {
        NetManager.create(WonderCoreApi.Device::class.java)
    }

    //查询绑定的设备
    suspend fun queryBindDevice(userId: String): CommonResponse<List<DeviceData>> {
        return api.queryBindDevice(mapOf("userId" to userId))
    }

    //绑定设备
    //eg: {"deviceCategory":"单杠","bluetoothAddress":"12:32:00:00:06:D5","moduleType":"","deviceName":"单杠","moduleVersion":""}
    suspend fun bindDevice(map: HashMap<String, Any?>): CommonResponse<DeviceData> {
        return api.bindDevice(
            RequestBodyBuilder()
                .addParams(map)
                .build()
        )
    }

    //解绑设备
    suspend fun unbindDevice(deviceId: Int, category: String): CommonResponse<Any> {
        return api.unbindDevice(
            RequestBodyBuilder()
                .addParams("id", deviceId)
                .addParams("deviceCategory", category)
                .build()
        )
    }

    //修改设备名字
    suspend fun updateDeviceName(id: Int, name: String): CommonResponse<Any> {
        return api.updateDeviceName(
            RequestBodyBuilder()
                .addParams("id", id)
                .addParams("deviceName", name)
                .build()
        )
    }

    //上传健腹轮的运动数据
    suspend fun addAbroller(time: Int, num: Int, calory: Float, deviceType: String): CommonResponse<PullUpData> {
        return addPushUps(time, num, calory, deviceType)
    }

    //上传单杠的运动数据
    suspend fun addPushUps(time: Int, num: Int, calory: Float, deviceType: String): CommonResponse<PullUpData> {
        return api.addPushUps(
            RequestBodyBuilder()
                .addParams("exerciseTime", time)
                .addParams("exerciseNumber", num)
                .addParams("burnCalories", calory)
                .addParams("deviceType", deviceType)
                .build()
        )
    }

    suspend fun queryAbroller(deviceType: String, startDate: String, endDate: String): CommonResponse<List<PullUpData>> {
        return queryPushUps(deviceType, startDate, endDate)
    }

    /*
    * 查询期间内的单杠数据
    * eg:http://192.168.65.156:8081/wondercore/appHistory/queryPushUps?deviceType=健腹轮&startDate=2021-07-25&endDate=2021-07-28
    */
    suspend fun queryPushUps(deviceType: String, startDate: String, endDate: String): CommonResponse<List<PullUpData>> {
        return api.queryPushUps(
            mapOf(
                "deviceType" to deviceType,
                "startDate" to startDate,
                "endDate" to endDate,
            )
        )
    }

}
