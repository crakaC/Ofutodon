package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class StreamingContent {
    enum class Event(val value: String) {
        Update("update"),
        Notification("notification"),
        Delete("delete"),
        Unknown("unknown")
    }

    @SerializedName("event")
    val event: String = Event.Unknown.value

    @SerializedName("payload")
    val payload: String? = null

    val eventType: Event
        get() = Event.values().firstOrNull { it.value == event } ?: Event.Unknown
}