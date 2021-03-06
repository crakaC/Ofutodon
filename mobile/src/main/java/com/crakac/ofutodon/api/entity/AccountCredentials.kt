package com.crakac.ofutodon.api.entity

import com.google.gson.annotations.SerializedName

class AccountCredentials (
        @SerializedName("display_name")
        var displayName: String? = null,
        @SerializedName("note")
        var note: String? = null,
        @SerializedName("avatar")
        var avatar: String? = null,
        @SerializedName("header")
        var header: String? = null
)