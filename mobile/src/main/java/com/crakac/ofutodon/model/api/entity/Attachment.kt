package com.crakac.ofutodon.model.api.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Kosuke on 2017/04/29.
 */
class Attachment {
    enum class Type(val v: String){
        Image("image"),
        Video("video"),
        Gifv("gifv")
    }
    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("type")
    val type: Type = Type.Image
    @SerializedName("url")
    val url: String = ""
    @SerializedName("remote_url")
    val remoteUrl: String = ""
    @SerializedName("preview_url")
    val previewUrl: String = ""
    @SerializedName("text_url")
    val textUrl: String = ""
}