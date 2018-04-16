package com.crakac.ofutodon.model.api

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.db.User
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MastodonBuilder() {
    companion object {
        val dispatcher: Dispatcher = Dispatcher()
    }

    private var userAccount: User? = null
    private var host: String = ""
    private var token: String? = null

    constructor(user: User) : this() {
        userAccount = user
        host = user.domain
    }

    fun setHost(host: String): MastodonBuilder {
        this.host = host
        return this
    }

    fun setAccessToken(accessToken: String?): MastodonBuilder {
        this.token = accessToken
        return this
    }

    fun build(): MastodonApi {
        val okHttpClient = createMastodonHttpClient(token ?: userAccount?.token)
        val retrofit = Retrofit.Builder()
                .baseUrl("https://$host")
                .client(okHttpClient)
                .addConverterFactory(
                        GsonConverterFactory.create(
                                GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create()))
                .build()
        return MastodonApi(retrofit.create(MastodonService::class.java), userAccount)
    }

    private fun createMastodonHttpClient(bearerToken: String?): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        val httpClientBuilder = OkHttpClient.Builder().addInterceptor(logger).dispatcher(dispatcher)
        if (bearerToken != null) {
            httpClientBuilder.addInterceptor {
                val org = it.request()
                val builder = org.newBuilder()
                builder.addHeader("Authorization", "Bearer $bearerToken")
                val newRequest = builder.build()
                it.proceed(newRequest)
            }
        }
        return httpClientBuilder.build()
    }
}