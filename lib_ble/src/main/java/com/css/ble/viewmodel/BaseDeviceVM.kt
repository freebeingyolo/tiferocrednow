package com.css.ble.viewmodel

import com.css.base.uibase.viewmodel.BaseViewModel

/**
 * @author yuedong
 * @date 2021-06-09
 */
abstract class BaseDeviceVM : BaseViewModel() {
    companion object {
        const val TIMEOUT_NEVER = -1L
    }

    class BondDeviceInfo {
        var mac: String = ""
        var name: String = ""
        var isAilink: Boolean = false
        var manifactureHex: String = ""

        override fun toString(): String {
            return "BondDeviceInfo(mac='$mac', manifactureHex='$manifactureHex')"
        }
    }

}