package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Kosuke on 2017/04/27.
 */
class AppCredentials {
    @SerializedName("id")
    val id: Long = 0
    @SerializedName("client_id")
    val clientId: String = ""
    @SerializedName("client_secret")
    val clientSecret: String = ""
    @SerializedName("redirect_uri")
    val redirectUri: String = ""
}