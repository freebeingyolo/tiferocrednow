package com.css.ble.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat

object BleUtils {

    fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager != null && locationManager.isLocationEnabled
        } else {
            try {
                val locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
                locationMode != Settings.Secure.LOCATION_MODE_OFF
            } catch (e: Settings.SettingNotFoundException) {
                false
            }
        }
    }

    fun isLocationAllowed(ctx: Context): Boolean {
        var neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //target sdk版本在29以上的需要精确定位权限才能搜索到蓝牙设备
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            neededPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        for (b in neededPermissions) {
            if (ActivityCompat.checkSelfPermission(ctx, b) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}