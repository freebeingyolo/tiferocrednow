package com.css.ble.bean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DeviceInfo(
    val name: String,
    @DrawableRes val icon: Int
)