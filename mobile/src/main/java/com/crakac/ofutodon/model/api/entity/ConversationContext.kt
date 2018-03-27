package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class ConversationContext {
    val TAG: String = "ConversationContext"
    @SerializedName("ancestors")
    val ancestors: List<Status> = emptyList()
    @SerializedName("descendants")
    val descendants: List<Status> = emptyList()
}