package com.css.base.net

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Ruis
 * @date 2021/5/6
 */
object NetManager {

    private lateinit var retrofit: Retrofit

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

    class Builer(val baseUrl: String, val outTime: Long = 15L) {

        private var interceptors: ArrayList<Interceptor> = arrayListOf()
        private var callAdapterFactory: CallAdapter.Factory?=null

        /**
         * 添加拦截：
         * 注：不需要添加日志拦截打印。
         * @param interceptor Interceptor
         */
        fun addInterceptor(interceptor: Interceptor): Builer {
            interceptors.add(interceptor)
            return this
        }

        /**
         * 没有配置就用默认的RxJava2CallAdapterFactory
         * @param factory Factory
         */
        fun addCallAdapterFactory(factory: CallAdapter.Factory): Builer {
            callAdapterFactory = factory
            return this
        }


        fun build() {
            val okHttpBuilder = OkHttpClient.Builder()
            for (inteceptor in interceptors) {
                okHttpBuilder.addInterceptor(inteceptor)
            }

            //超时配置
            okHttpBuilder.connectTimeout(outTime, TimeUnit.SECONDS)
            okHttpBuilder.readTimeout(outTime, TimeUnit.SECONDS)
            okHttpBuilder.writeTimeout(outTime, TimeUnit.SECONDS)
            val sslParams = HttpsUtils.getSslSocketFactory()
            okHttpBuilder.sslSocketFactory(sslParams.sslSocketFactory, sslParams.trustManager)
            val okHttpClient = okHttpBuilder.build()
            if (callAdapterFactory != null){
                retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(callAdapterFactory!!)
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .build()
            }else{
                retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .build()
            }
        }
    }

}