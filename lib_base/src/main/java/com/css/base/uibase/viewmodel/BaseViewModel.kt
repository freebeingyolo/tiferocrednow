package com.css.base.uibase.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.blankj.utilcode.util.LogUtils
import com.css.base.net.CommonResponse
import com.css.base.net.HttpNetCode
import com.css.base.net.process
import com.css.base.uibase.inner.IBaseViewModel
import com.css.base.uibase.inner.INetView
import com.css.base.uibase.inner.IResource
import com.css.base.utils.UICoreConfig
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.EOFException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.ParseException
import javax.net.ssl.SSLException

abstract class BaseViewModel : ViewModel(), IBaseViewModel, INetView, IResource {

    val showToastStrEvent = MutableLiveData<String?>()
    val showLongToastStrEvent = MutableLiveData<String?>()
    val showToastResEvent = MutableLiveData<Int>()
    val showLongToastResEvent = MutableLiveData<Int>()
    val showCenterToastStrEvent = MutableLiveData<String?>()
    val showCenterLongToastStrEvent = MutableLiveData<String?>()
    val showLoadingEvent = MutableLiveData<String?>()
    val hideLoadingEvent = MutableLiveData<String?>()
    val showCenterToastResEvent = MutableLiveData<Int>()
    val showCenterLongToastResEvent = MutableLiveData<Int>()
    val finishAcEvent = MutableLiveData<String>()
    val callUILiveData: LiveData<Any?> by lazy { MutableLiveData() }//调用UI

    var isViewDestroyed = false

    override fun onAny(owner: LifecycleOwner, event: Lifecycle.Event) {
    }

    override fun onCreate() {
    }

    override fun onStart() {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun onDestroy() {
        isViewDestroyed = true
    }

    override fun showLoading() {
        showLoadingEvent.value = ""
    }

    override fun hideLoading() {
        hideLoadingEvent.value = ""
    }

    override fun showToast(msg: String?, onDismiss: (() -> Unit)?) {
        showToastStrEvent.value = msg
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 2000)
        }
    }

    override fun showLongToast(msg: String?, onDismiss: (() -> Unit)?) {
        showLongToastStrEvent.value = msg
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 3500)
        }
    }

    override fun showToast(@StringRes resId: Int, onDismiss: (() -> Unit)?) {
        showToastResEvent.value = resId
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 2000)
        }
    }

    override fun showLongToast(resId: Int, onDismiss: (() -> Unit)?) {
        showLongToastResEvent.value = resId
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 3500)
        }
    }

    override fun showCenterToast(msg: String?, onDismiss: (() -> Unit)?) {
        showCenterToastStrEvent.value = msg
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 2000)
        }
    }

    override fun showCenterLongToast(msg: String?, onDismiss: (() -> Unit)?) {
        showCenterLongToastStrEvent.value = msg
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 3500)
        }
    }

    override fun showCenterToast(resId: Int, onDismiss: (() -> Unit)?) {
        showCenterToastResEvent.value = resId
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 2000)
        }
    }

    override fun showCenterLongToast(resId: Int, onDismiss: (() -> Unit)?) {
        showCenterLongToastResEvent.value = resId
        onDismiss?.let {
            Handler(Looper.getMainLooper()).postDelayed({ it() }, 3500)
        }
    }

    override fun finishAc() {
        finishAcEvent.value = ""
    }

    /**
     * 主线程回调
     */
    override fun launch(block: suspend () -> Unit, failed: suspend (Int, String?) -> Unit): Job {
        val job = viewModelScope.launch(Dispatchers.Main) {
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
        val job = viewModelScope.launch(Dispatchers.Main) {
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
        val job = viewModelScope.launch(Dispatchers.IO) {
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
        val job = viewModelScope.launch(Dispatchers.IO) {
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
        val loginExpired = t.message?.contains("HTTP ${HttpNetCode.LOGIN_EXPIRED}") ?: false
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
                        failed(HttpNetCode.SSL_ERROR, "SSL校验未通过：${t.message}")
                    } else {
                        failed(HttpNetCode.SSL_ERROR, "SSL校验未通过")
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
                is ConnectException -> {
                    failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常")
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
        val loginExpired = t.message?.contains("HTTP ${HttpNetCode.LOGIN_EXPIRED}") ?: false
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
                is ConnectException -> {
                    failed(HttpNetCode.NET_CONNECT_ERROR, "网络异常", null)
                }
                is HttpException -> {
                    failed(HttpNetCode.NET_CONNECT_ERROR, "请求失败，您访问的资源可能不存在", null)
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