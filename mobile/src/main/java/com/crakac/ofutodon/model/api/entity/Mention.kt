package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class Mention {
    @SerializedName("url")
    val url: String = ""
    @SerializedName("username")
    val username: String = ""
    @SerializedName("acct")
    val acct: String = ""
    @SerializedName("id")
    val id: Long = 0L
}