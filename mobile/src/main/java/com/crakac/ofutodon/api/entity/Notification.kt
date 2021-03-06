package com.crakac.ofutodon.api.entity

import com.google.gson.annotations.SerializedName

class Notification(id: Long = 0) : Identifiable(id) {
    enum class Type(val value: String) {
        Mention("mention"),
        ReBlog("reblog"),
        Favourite("favourite"),
        Follow("follow")
    }

    @SerializedName("type")
    val type: String = Type.Mention.value
    @SerializedName("created_at")
    val createdAt: String = ""
    @SerializedName("account")
    val account: Account? = null
    @SerializedName("status")
    var status: Status? = null

    fun getType() = Type.values().first { e -> e.value == type }
}