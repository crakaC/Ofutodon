package com.crakac.ofutodon.api.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Kosuke on 2017/04/29.
 */
class Attachment : Serializable {
    enum class Type(val value: String) {
        Image("image"),
        Video("video"),
        Gifv("gifv")
    }

    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("type")
    val type: String = Type.Image.value
    @SerializedName("url")
    val url: String = ""
    @SerializedName("remote_url")
    val remoteUrl: String = ""
    @SerializedName("preview_url")
    val previewUrl: String = ""
    @SerializedName("text_url")
    val textUrl: String = ""

    fun getType(): Type = Type.values().first { e -> e.value == type }
}