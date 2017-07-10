package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Kosuke on 2017/04/27.
 */
open class AccessToken {
    @SerializedName("access_token")
    val accessToken: String = ""
    @SerializedName("token_type")
    var tokenType: String? = null
    @SerializedName("scope")
    var scope: String? = null
    @SerializedName("createdAt")
    var createdAt: Long? = null
}