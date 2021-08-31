package com.css.base.utils

import android.content.Context
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object  DownloadUtil {
    private val okHttpClient: OkHttpClient = OkHttpClient()
    private var context: Context? = null
    private val TAG = javaClass.simpleName

    /**
     * @param url 下载连接
     * @param listener 下载监听
     */
    fun download(context: Context?, url: String?, listener: OnDownloadListener) {
        this.context = context
        // 需要token的时候可以这样做
        // Request request = new Request.Builder().header("token",token).url(url).build();
        val request: Request = Request.Builder().url(url!!).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e?.printStackTrace()
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
                    var downloadPath = File(AppConfig().DEFAULT_SAVE_APK_PATH)
                    if (!downloadPath.mkdirs()) {
                        downloadPath.createNewFile()
                    }
                    val file = File(downloadPath.absoluteFile,AppConfig().DEFAULT_APK_NAME)
                    Log.w(TAG, "最终路径：$file")
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (inputStream?.read(buf).also { len = it!! } != -1) {
                        fos?.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = (sum * 1.0f / total * 100).toInt()
                        listener.onDownloading(progress)
                    }
                    fos?.flush()
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

    class AppConfig {
        var DEFAULT_SAVE_APK_PATH = "${context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/".trim()
        var DEFAULT_APK_NAME = "Wondercare.apk"
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