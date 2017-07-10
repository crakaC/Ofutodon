package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class Context {
    val TAG: String = "Context"
    @SerializedName("ancestors")
    val ancestors: List<Status>? = null
    @SerializedName("descendants")
    val descendants: List<Status>? = null
}