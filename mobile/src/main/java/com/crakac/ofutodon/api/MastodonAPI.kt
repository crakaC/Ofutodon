package com.crakac.ofutodon.api

import com.crakac.ofutodon.api.entity.AccessToken
import com.crakac.ofutodon.api.entity.AppCredentials
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Created by Kosuke on 2017/04/27.
 */
interface MastodonAPI {
    @FormUrlEncoded
    @POST("api/v1/apps")
    fun registerApplication(
            @Field("client_name")
            clientName: String,
            @Field("redirect_uris")
            redirectUris: String,
            @Field("scopes")
            scopes: String,
            @Field("website")
            website: String
    ): Call<AppCredentials>

    @FormUrlEncoded
    @POST("oauth/token")
    fun fetchOAuthToken(
            @Field("client_id")
            clientId: String,
            @Field("client_secret")
            clientSecret: String,
            @Field("redirect_uri")
            redirectUri: String,
            @Field("code")
            code: String,
            @Field("grant_type")
            grantType: String
    ): Call<AccessToken>
}