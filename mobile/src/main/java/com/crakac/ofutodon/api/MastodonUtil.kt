package com.crakac.ofutodon.api

import android.net.Uri
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MastodonUtil {
    companion object{
        val TAG: String = "MastodonUtil"
        val dispatcher: Dispatcher = Dispatcher()
        fun createMastodonApi(domain: String, accessToken: String? = null): MastodonAPI {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            val clientBuilder = OkHttpClient.Builder()
                    .addInterceptor(logger)
            accessToken.let {
                clientBuilder.addInterceptor {
                    val org = it.request()
                    val builder = org.newBuilder()
                    builder.addHeader("Authorization", "Bearer $accessToken")
                    val newRequest = builder.build()
                    it.proceed(newRequest)
                }
            }

            val okHttpClient = clientBuilder.dispatcher(dispatcher).build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://${domain}")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(MastodonAPI::class.java)
        }

        fun createAuthenticationUri(domain: String, clientId: String, redirectUri: String): Uri{
            return Uri.Builder()
                    .scheme("https")
                    .authority(domain)
                    .path(C.ENDPOINT_AUTHORIZE)
                    .appendQueryParameter(C.CLIENT_ID, clientId)
                    .appendQueryParameter(C.REDIRECT_URI, redirectUri)
                    .appendQueryParameter(C.RESPONSE_TYPE, "code")
                    .appendQueryParameter(C.SCOPES, C.OAUTH_SCOPES)
                    .build()
        }
    }
}