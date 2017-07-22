package com.crakac.ofutodon.model.api

import android.net.Uri
import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.model.api.entity.AccessToken
import com.crakac.ofutodon.model.api.entity.AppCredentials
import com.crakac.ofutodon.util.PrefsUtil
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MastodonUtil private constructor() {
    companion object {
        private var instance: Mastodon? = null
        val api: Mastodon?
            get() {
                return instance
            }
        val TAG: String = "MastodonUtil"
        val dispatcher: Dispatcher = Dispatcher()
        fun api(domain: String, accessToken: String? = null): Mastodon {

            val clientBuilder = getLoggableHttpClientBuilder()
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
                    .baseUrl("https://$domain")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            instance = retrofit.create(Mastodon::class.java)
            return instance!!
        }

        fun createAuthenticationUri(domain: String, redirectUri: String): Uri {
            return Uri.Builder()
                    .scheme("https")
                    .authority(domain)
                    .path(C.ENDPOINT_AUTHORIZE)
                    .appendQueryParameter(C.CLIENT_ID, getClientId(domain))
                    .appendQueryParameter(C.REDIRECT_URI, redirectUri)
                    .appendQueryParameter(C.RESPONSE_TYPE, "code")
                    .appendQueryParameter(C.SCOPE, C.OAUTH_SCOPES)
                    .build()
        }

        fun saveAppCredential(domain: String, credentials: AppCredentials) {
            PrefsUtil.putString("$domain.${C.CLIENT_ID}", credentials.clientId)
            PrefsUtil.putString("$domain.${C.CLIENT_SECRET}", credentials.clientSecret)
            PrefsUtil.putLong("$domain.id", credentials.id)
        }

        fun hasAppCredential(domain: String): Boolean {
            val clientId = getClientId(domain)
            val clientSecret = PrefsUtil.getString("$domain.${C.CLIENT_SECRET}")
            return clientId != null && clientSecret != null
        }

        fun hasAccessToken(domain: String): Boolean {
            return getAccessToken(domain) != null
        }

        fun getAccessToken(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.ACCESS_TOKEN}")
        }

        fun saveAccessToken(domain: String, accessToken: String) {
            PrefsUtil.putString("$domain.${C.ACCESS_TOKEN}", accessToken)
        }

        fun getClientId(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_ID}")
        }

        fun getClientSecret(domain: String): String? {
            return PrefsUtil.getString("$domain.${C.CLIENT_SECRET}")
        }

        fun registerApplication(domain: String, clientName: String, redirectUris: String, website: String): Call<AppCredentials> {
            return api(domain).registerApplication(
                    clientName,
                    redirectUris,
                    C.OAUTH_SCOPES,
                    website
            )
        }

        fun fetchAccessToken(domain: String, redirectUri: String, code: String): Call<AccessToken> {
            val id = getClientId(domain)!!
            val secret = getClientSecret(domain)!!
            return api(domain).fetchAccessToken(id, secret, redirectUri, code, C.AUTHORIZATION_CODE)
        }

        private fun getLoggableHttpClientBuilder(): OkHttpClient.Builder {
            val logger = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                logger.level = HttpLoggingInterceptor.Level.HEADERS
            } else {
                logger.level = HttpLoggingInterceptor.Level.NONE
            }
            return OkHttpClient.Builder().addInterceptor(logger)
        }
    }
}