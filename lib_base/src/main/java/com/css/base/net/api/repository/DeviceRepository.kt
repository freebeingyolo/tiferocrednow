package com.css.base.net.api.repository

import com.css.base.net.CommonResponse
import com.css.base.net.NetManager
import com.css.base.net.RequestBodyBuilder
import com.css.base.net.api.WonderCoreApi
import com.css.service.data.DeviceData

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
        val map = HashMap<String, String>()
        map["userId"] = userId
        return api.queryBindDevice(map)
    }

    //绑定设备
    suspend fun bindDevice(category: String, name: String, mac: String): CommonResponse<Any> {
        return api.bindDevice(
            RequestBodyBuilder()
                .addParams("deviceCategory", category)
                .addParams("deviceName", name)
                .addParams("bluetoothAddress", mac)
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

}
