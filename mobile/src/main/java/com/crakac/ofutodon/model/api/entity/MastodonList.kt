package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class MastodonList {
    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("title")
    val acct: String = ""
}