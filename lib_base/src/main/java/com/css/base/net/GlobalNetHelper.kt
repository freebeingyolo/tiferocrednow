package com.css.base.net

import com.blankj.utilcode.util.LogUtils
import com.css.base.utils.UICoreConfig
import com.google.gson.JsonSyntaxException
import com.css.base.uibase.inner.INetView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.EOFException
import java.net.SocketTimeoutException
import java.text.ParseException
import javax.net.ssl.SSLException

/**
 * @author Ruis
 * @date 2021/5/6
 */
object GlobalNetHelper : INetView {

    /**
     * 主线程回调
     */
    override fun launch(block: suspend () -> Unit, failed: suspend (Int, String?) -> Unit): Job {
        val job = GlobalScope.launch(Dispatchers.Main) {
            try {
                block()
            } catch (t: Throwable) {
                onFailSuspend(t, failed)
            }
        }
        return job
    }

    /**
     * 主线程回调
     */
    override fun <T> netLaunch(
        block: suspend () -> CommonResponse<T>,
        success: (msg: String?, d: T?) -> Unit,
        failed: (Int, String?, d: T?) -> Unit
    ): Job {
        val job = GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = block()
                response.process(success, failed)
            } catch (t: Throwable) {
                onFailException(t, failed)
            }
        }
        return job
    }

    /**
     * 子线程回调
     */
    override fun ioLaunch(block: suspend () -> Unit, failed: suspend (Int, String?) -> Unit): Job {
        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (t: Throwable) {
                onFailSuspend(t, failed)
            }
        }
        return job
    }

    /**
     * 子线程回调
     */
    override fun <T> ioNetLaunch(
        block: suspend () -> CommonResponse<T>,
        success: (msg: String?, d: T?) -> Unit,
        failed: (Int, String?, d: T?) -> Unit
    ): Job {
        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = block()
                response.process(success, failed)
            } catch (t: Throwable) {
                onFailException(t, failed)
            }
        }
        return job
    }

    private suspend fun onFailSuspend(t: Throwable, failed: suspend (Int, String?) -> Unit) {
        val loginExpired = t.message?.contains("HTTP 401") ?: false
        if (!loginExpired) {
            LogUtils.e(t)
            when (t) {
                is EOFException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常：${t.message}")
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常")
                    }
                }
                is SocketTimeoutException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_TIMEOUT, "网络超时：${t.message}")
                    } else {
                        failed(HttpNetCode.NET_TIMEOUT, "网络超时")
                    }
                }
                is SSLException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "SSL校验未通过：${t.message}")
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "SSL校验未通过")
                    }
                }
                is ParseException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.PARSE_ERROR, "Parse解析异常：${t.message}")
                    } else {
                        failed(HttpNetCode.PARSE_ERROR, "Parse解析异常")
                    }
                }
                is JsonSyntaxException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.JSON_ERROR, "Json解析异常：${t.message}")
                    } else {
                        failed(HttpNetCode.JSON_ERROR, "Json解析异常")
                    }
                }
                else -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络繁忙：${t.message}")
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络繁忙")
                    }
                }
            }
        }
    }

    private fun <T> onFailException(t: Throwable, failed: (Int, String?, d: T?) -> Unit) {
        val loginExpired = t.message?.contains("HTTP 401") ?: false
        if (!loginExpired) {
            LogUtils.e(t)
            when (t) {
                is EOFException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常：${t.message}", null)
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常", null)
                    }
                }
                is SocketTimeoutException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_TIMEOUT, "网络超时：${t.message}", null)
                    } else {
                        failed(HttpNetCode.NET_TIMEOUT, "网络超时", null)
                    }
                }
                is SSLException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "SSL校验未通过：${t.message}", null)
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "SSL校验未通过", null)
                    }
                }
                is ParseException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.PARSE_ERROR, "Parse解析异常：${t.message}", null)
                    } else {
                        failed(HttpNetCode.PARSE_ERROR, "Parse解析异常", null)
                    }
                }
                is JsonSyntaxException -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.JSON_ERROR, "Json解析异常：${t.message}", null)
                    } else {
                        failed(HttpNetCode.JSON_ERROR, "Json解析异常", null)
                    }
                }
                else -> {
                    if (UICoreConfig.mode) {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络繁忙：${t.message}", null)
                    } else {
                        failed(HttpNetCode.NET_CONNECT_ERROR, "网络繁忙", null)
                    }
                }
            }
        }
    }


}