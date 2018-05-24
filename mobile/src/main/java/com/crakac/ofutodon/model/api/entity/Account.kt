package com.crakac.ofutodon.model.api.entity

import android.text.Spanned
import com.google.gson.annotations.SerializedName
import java.net.IDN

/**
 * Created by Kosuke on 2017/04/27.
 */
class Account {
    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("username")
    val username: String = ""
    @SerializedName("acct")
    val acct: String = ""
    @SerializedName("display_name")
    val displayName: String = ""
    @SerializedName("locked")
    val locked: Boolean = false
    @SerializedName("created_at")
    val createdAt: String = ""
    @SerializedName("followers_count")
    val followersCount: Long = 0L
    @SerializedName("following_count")
    val followingCount: Long = 0L
    @SerializedName("statuses_count")
    val statusesCount: Long = 0L
    @SerializedName("note")
    val note: String = ""
    @SerializedName("url")
    val url: String = ""
    @SerializedName("avatar")
    val avatar: String = ""
    @SerializedName("avatar_static")
    val avatarStatic: String = ""
    @SerializedName("header")
    val header: String = ""
    @SerializedName("header_static")
    val headerStatic: String = ""
    @SerializedName("emojis")
    val emojis: List<Emoji> = emptyList()

    @Transient
    var spannedDisplayName: Spanned? = null

    val unicodeAcct: String
    get() {
        val splitted = acct.split("@")
        return if (splitted.size == 1) {
            acct
        } else {
            splitted[0] + "@" + IDN.toUnicode(splitted[1])
        }
    }
}