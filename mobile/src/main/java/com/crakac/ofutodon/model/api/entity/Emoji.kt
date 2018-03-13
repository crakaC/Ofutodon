package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

class Emoji {
    @SerializedName("shortcode")
    val shortCode: String = ""
    @SerializedName("url")
    val url: String = ""
    @SerializedName("static_url")
    val staticUrl: String = ""
    @SerializedName("visible_in_picker")
    val isVisibleInPicker: Boolean = false
}