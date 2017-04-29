package com.crakac.ofutodon.api.entity

import com.google.gson.annotations.SerializedName

/**
 * Created by Kosuke on 2017/04/29.
 */
class Instance{
    @SerializedName("uri")
    val uri: String = ""
    @SerializedName("title")
    val title: String = ""
    @SerializedName("description")
    val description: String = ""
    @SerializedName("email")
    val email: String = ""
}