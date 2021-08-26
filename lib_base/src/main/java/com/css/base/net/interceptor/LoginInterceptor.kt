package com.css.base.net.interceptor

import LogUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.css.base.net.CommonResponse
import com.css.base.net.HttpNetCode
import com.css.service.router.ARouterUtil
import com.css.service.utils.CacheKey
import com.css.service.utils.WonderCoreCache
import com.google.gson.Gson
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.EOFException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException


class LoginInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == HttpNetCode.LOGIN_EXPIRED) {
            LogUtils.d("receive server code:${HttpNetCode.LOGIN_EXPIRED},do logout()")
            parseLogout(response).let { ToastUtils.showShort(it?.msg ?: "该用户已经别的设备登录,如果非本人操作请修改密码！") }
            logout()
        }
        return response
    }

    private fun logout() {
        WonderCoreCache.removeKey(CacheKey.LOGIN_DATA)
        WonderCoreCache.removeKey(CacheKey.USER_INFO)
        ActivityUtils.finishAllActivities()
        ARouterUtil.openLogin()
    }

    private fun parseLogout(response: Response): CommonResponse<*>? {
        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val UTF8 = Charset.forName("UTF-8")
        if (!bodyEncoded(response.headers)) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer: Buffer = source.buffer
            var charset: Charset = UTF8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)!!
                } catch (e: UnsupportedCharsetException) {
                    return null
                }
            }
            if (!isPlaintext(buffer)) {
                return null
            }
            if (contentLength != 0L) {
                val result: String = buffer.clone().readString(charset)
                //LogUtils.d(" response.url():" + response.request.url)
                //LogUtils.d(" response.body():$result")
                //得到所需的string，开始判断是否异常
                //***********************do something*****************************
                return Gson().fromJson(result, CommonResponse::class.java)
            }
        }
        return null
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding: String? = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    @Throws(EOFException::class)
    fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64.toLong()
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }
}