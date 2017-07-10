package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class AccountCredentials @JvmOverloads constructor(
        @SerializedName("display_name")
        var displayName: String? = null,
        @SerializedName("note")
        var note: String? = null,
        @SerializedName("avatar")
        var avatar: String? = null,
        @SerializedName("header")
        var header: String? = null
)