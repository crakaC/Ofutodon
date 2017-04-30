package com.crakac.ofutodon

import com.crakac.ofutodon.api.Mastodon
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TestUtil{
    fun createApi(useToken: Boolean = false): Mastodon {
        System.out.println(BuildConfig.BUILD_TYPE)
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(logger)

        if(useToken) {
            httpClientBuilder.addInterceptor {
                val org = it.request()
                val builder = org.newBuilder()
                builder.addHeader("Authorization", "Bearer ${BuildConfig.DEBUG_TOKEN}")
                val newRequest = builder.build()
                it.proceed(newRequest)
            }
        }

        val retrofit = Retrofit.Builder()
                .baseUrl("https://mstdn.jp")
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit.create(Mastodon::class.java)
    }

}