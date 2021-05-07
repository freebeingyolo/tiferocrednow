package com.css.base.uibase.inner

import com.css.base.net.CommonResponse
import kotlinx.coroutines.Job


/**
 * @author Ruis
 * @date 2020/1/21
 */
interface INetView {

    fun launch(block: suspend () -> Unit, failed: suspend (Int, String?) -> Unit): Job

    fun <T> netLaunch(
        block: suspend () -> CommonResponse<T>,
        success: (msg: String?, d: T?) -> Unit,
        failed: (Int, String?, d: T?) -> Unit
    ): Job

    fun ioLaunch(block: suspend () -> Unit, failed: suspend (Int, String?) -> Unit): Job

    fun <T> ioNetLaunch(
        block: suspend () -> CommonResponse<T>,
        success: (msg: String?, d: T?) -> Unit,
        failed: (Int, String?, d: T?) -> Unit
    ): Job

}