package com.css.base.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object DownloadUtil {
    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val TAG = javaClass.simpleName

    /**
     * @param url 下载连接
     * @param listener 下载监听
     */
    fun download(url: String, listener: OnDownloadListener) {
        // 需要token的时候可以这样做
        // Request request = new Request.Builder().header("token",token).url(url).build();
        val application = ActivityUtils.getTopActivity().application
        val request: Request = Request.Builder().url(url).tag("dl_upgrade_apk").build()
        val default_save_apk_path = "${application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/".trim()
        val default_apk_name = "WonderCoreFit.apk"
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                listener.onDownloadFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                var inputStream: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                try {
                    inputStream = response.body?.byteStream()
                    val total: Long = response.body?.contentLength()!!
                    val downloadPath = File(default_save_apk_path)
                    if (!downloadPath.mkdirs()) {
                        downloadPath.createNewFile()
                    }
                    val file = File(downloadPath.absoluteFile, default_apk_name)
                    Log.w(TAG, "最终路径：$file")
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (inputStream?.read(buf).also { len = it!! } != -1) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        listener.onDownloading(progress)
                    }
                    fos.flush()
                    listener.onDownloadSuccess(file)
                } catch (e: Exception) {
                    listener.onDownloadFailed()
                } finally {
                    try {
                        inputStream?.close()
                        fos?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

        })
    }

    class AppConfig(context: Context) {

    }

    interface OnDownloadListener {
        /**
         * 下载成功
         */
        fun onDownloadSuccess(file: File)

        /**
         * @param progress
         * 下载进度
         */
        fun onDownloading(progress: Int)

        /**
         * 下载失败
         */
        fun onDownloadFailed()
    }
}