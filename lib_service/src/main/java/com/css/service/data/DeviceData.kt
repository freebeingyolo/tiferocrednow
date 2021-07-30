package com.css.service.data

data class DeviceData(
    val id: Int,
    val bluetoothAddress: String,
    val deviceCategory: String,
    val deviceName: String,
    val isBind: String,
    val isDel: String,
    val mcuVersion: Any,
    val moduleType: Any,
    val moduleVersion: Any,
    val productType: Any,
    val status: Any
)