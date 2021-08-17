package com.css.base.net.interceptor

import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.LogUtils
import com.css.base.net.NetLongLogger
import com.css.service.utils.WonderCoreCache
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequestBuilder = request.newBuilder()
        newRequestBuilder.header("contentType", "application/json")
        var session = WonderCoreCache.getLoginInfo()
        var mNonce = Random().nextInt(99999999) + Random().nextInt(9999)
        var mTimestamp = System.currentTimeMillis() + Random().nextInt(9999)
        if (mNonce == mNonce) {
            mNonce += Random().nextInt(99999)
        }
        if (mTimestamp == mTimestamp) {
            mTimestamp += 1000
        }
        var mMethod = request.method.toLowerCase(Locale.getDefault())
        if (session != null) {
            newRequestBuilder.header("token", session.token)
        }
        newRequestBuilder.header("method", mMethod)
        newRequestBuilder.header("nonce", mNonce.toString())
        newRequestBuilder.header("timestamp", mTimestamp.toString())
        newRequestBuilder.header("MD5", signatures(request,mNonce, mTimestamp,mMethod))

        val newRequest = newRequestBuilder.build()
        //NetLongLogger().log("newRequest-->$newRequest")
        return chain.proceed(newRequest)
    }

    //对【随机数、时间戳】进行签名加密
    private fun signatures(
        mNonce: Int,
        mTimestamp: Long,
    ): String {
        val clientSecret = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVRiDk"
        val signatureStr = "nonce=${mNonce}&timestamp=${mTimestamp}\$${clientSecret}"
        return EncryptUtils.encryptMD5ToString(signatureStr)
    }
    //对【请求体、随机数、时间戳、请求方法】进行签名加密
    private fun signatures(
        request: Request,
        mNonce: Int,
        mTimestamp: Long,
        mMethod: String
    ): String {
        val clientSecret = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVRiDk"
        val signatureStr =
            "nonce=${mNonce}&timestamp=${mTimestamp}\$${clientSecret}"
        return when (mMethod) {
            "post" -> {
              /*  var str = request.body?.let { getParamContent(it) }
                str = str?.substring(0, str.length - 1)
                val merge = "method=${str}&${signatureStr}"*/
                val merge = signatureStr
                val md5 = EncryptUtils.encryptMD5ToString(merge)
                LogUtils.v("signatures#post#merge-->$merge,md5:$md5")
                md5
            }
            "get" -> {
                var merge = ""
                if (request.url.query != null) {
                    val query: List<String> = ArrayList(request.url.query!!.split("&"))
                    Collections.sort(query) { str1, str2 -> // 按首字母倒序排
                        str2.compareTo(str1)
                    }
                    //重新拼接请求体参数query
                    var sortStr = ""
                    for (str in query) {
                        sortStr = "${sortStr}${str}&"
                    }
                    //去掉最后一个&
                    sortStr = sortStr.substring(0, sortStr.length - 1)
                    merge = "method=${sortStr}&${signatureStr}"
                } else {
                    merge = signatureStr
                }
                /* val strs: List<String> = merge.split("&")
                Collections.sort(strs) { str1, str2 -> // 按首字母升序排
                    str1.compareTo(str2);
                }
                LogUtils.v("suisui", "merge=" + merge)
                var sortStr = ""
                for (query in strs) {
                    sortStr = "${sortStr}${query}&"
                }*/
                val md5 = EncryptUtils.encryptMD5ToString(merge)
                LogUtils.v("signatures#get#merge-->$merge,md5:$md5")
                md5
            }
            else -> {
                EncryptUtils.encryptMD5ToString(signatureStr)
            }
        }
    }

    private fun getParamContent(body: RequestBody): String {
        val buffer = Buffer()
        body.writeTo(buffer)
        var json = buffer.readUtf8()
        var stringBuilder = StringBuilder()
        var jsonObject = JSONObject(json)
        var set = jsonObject.keys()
        for (item in set) {
            var value = jsonObject.get(item)
            stringBuilder.append(item).append("=").append(value).append("&")
        }
        return stringBuilder.toString()
    }

}