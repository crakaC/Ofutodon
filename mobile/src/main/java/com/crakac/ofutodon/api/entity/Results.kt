package com.crakac.ofutodon.api.entity

import android.nfc.Tag
import com.google.gson.annotations.SerializedName

class Results {
    @SerializedName("accounts")
    val accounts: List<Account>? = null
    @SerializedName("statuses")
    val statuses: List<Status>? = null
    @SerializedName("hashtags")
    val hashtags: List<Tag>? = null
}