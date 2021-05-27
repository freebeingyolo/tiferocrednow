package com.css.ble.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


object BleUtils {


    //绑定
    fun createBond(bleDevice: BluetoothDevice): Boolean? {
        var result = false
        if (Build.VERSION.SDK_INT > 18) {
            result = bleDevice.createBond()
        } else {
            try {
                val createBond: Method = bleDevice.javaClass.getMethod("createBond")
                val invoke = createBond.invoke(bleDevice) as Boolean
                result = invoke
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return result
    }

    //与设备解除配对
    fun removeBond(bleDevice: BluetoothDevice): Boolean? {
        var result = false
        try {
            val removeBond: Method = bleDevice.javaClass.getMethod("removeBond")
            val returnValue = removeBond.invoke(bleDevice) as Boolean
            result = returnValue
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun isLocationEnabled2(context: Context): Boolean {
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

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isGpsEnabled
    }

    fun isLocationAllowed(ctx: Context, cb: PermissionUtils.FullCallback): Boolean {
        var neededPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //target sdk版本在29以上的需要精确定位权限才能搜索到蓝牙设备
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            neededPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        var neededPermissions2 = mutableListOf<String>()
        for (b in neededPermissions) {
            if (ContextCompat.checkSelfPermission(ctx, b) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions2.add(b)
            }
        }
        if (neededPermissions2.isEmpty()) {
            cb.onGranted(neededPermissions)
            return true
        } else {
            PermissionUtils.permission(PermissionConstants.LOCATION)
                .rationale { _, shouldRequest ->
                    shouldRequest.again(true)
                }.callback(cb).request()
            return false
        }
    }


}