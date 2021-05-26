package com.css.ble.viewmodel

import com.css.ble.R

/**
 * @author yuedong
 * @date 2021-05-26
 */
enum class ErrorType(val content: Int) {
    BLE_OFF(R.string.error_ble_off),
    LOCATION_OFF(R.string.error_location_off),
    LOCATION_PERMISSION_OFF(R.string.error_location_notallowed),
    SEARCH_TIMEOUT(R.string.error_search_timeout),
}