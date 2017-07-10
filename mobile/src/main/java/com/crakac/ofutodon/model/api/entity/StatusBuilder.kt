package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class StatusBuilder(
        @SerializedName("status")
        var text: String? = null,
        @SerializedName("in_reply_to_id")
        var replyTo: Long? = null,
        @SerializedName("media_ids")
        var mediaIds: List<Long>? = null,
        @SerializedName("sensitive")
        var isSensitive: Boolean? = null,
        @SerializedName("spoiler_text")
        var spoilerText: String? = null,
        @SerializedName("visibility")
        var visibility: String = Status.Visibility.Public.value
        ){

}