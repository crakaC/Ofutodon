package com.crakac.ofutodon.api

import android.net.Uri
import android.text.Html
import android.text.Spanned
import com.crakac.ofutodon.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MastodonUtil {
    companion object{
        private var instance: Mastodon? = null
        val api: Mastodon?
        get() {
            return instance
        }
        val TAG: String = "MastodonUtil"
        val dispatcher: Dispatcher = Dispatcher()
        fun createMastodonApi(domain: String, accessToken: String? = null): Mastodon {

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
            val gson = GsonBuilder().registerTypeAdapter(Spanned::class.java,
                    JsonDeserializer<Spanned> { json, typeOfT, context ->
                        fun trimWhiteSpace (source: CharSequence) : CharSequence{
                            var i = source.length
                            do{
                                --i
                            }while(i >= 0 && Character.isWhitespace(source[i]))
                            return source.subSequence(0, i + 1)
                        }
                        trimWhiteSpace(Html.fromHtml(json.asString)) as Spanned
                    }).create()

            val okHttpClient = clientBuilder.dispatcher(dispatcher).build()
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://${domain}")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            instance = retrofit.create(Mastodon::class.java)
            return instance!!
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

        private fun getLoggableHttpClientBuilder(): OkHttpClient.Builder{
            val logger = HttpLoggingInterceptor()
            if(BuildConfig.DEBUG){
                logger.level = HttpLoggingInterceptor.Level.BODY
            } else {
                logger.level = HttpLoggingInterceptor.Level.NONE
            }
            return OkHttpClient.Builder().addInterceptor(logger)
        }
    }
}