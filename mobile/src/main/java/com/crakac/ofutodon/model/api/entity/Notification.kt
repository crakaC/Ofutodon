package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class Notification {
    enum class Type(val value: String){
        Mention("mention"),
        ReBlog("reblog"),
        Favourite("favourite"),
        Follow("follow")
    }

    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("type")
    val type: String = Type.Mention.value
    @SerializedName("created_at")
    val createdAt: String = ""
    @SerializedName("account")
    val account: Account? = null
    @SerializedName("status")
    val status: Status? = null
}