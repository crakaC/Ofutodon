package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class Relationship {
    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("following")
    val following: Boolean = false
    @SerializedName("followed_by")
    val followedBy: Boolean = false
    @SerializedName("blocking")
    val blocking: Boolean = false
    @SerializedName("muting")
    val muting: Boolean = false
    @SerializedName("requested")
    val requested: Boolean = false
}