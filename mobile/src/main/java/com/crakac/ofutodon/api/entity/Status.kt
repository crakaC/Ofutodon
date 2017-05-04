package com.crakac.ofutodon.api.entity

import android.text.Spanned
import com.crakac.ofutodon.util.HtmlUtil
import com.google.gson.annotations.SerializedName

class Status {
    enum class Visibility(val value: String) {
        Public("public"),
        UnListed("unlisted"),
        Private("private"),
        Direct("direct")
    }

    @SerializedName("id")
    val id: Long = 0L
    @SerializedName("uri")
    val uri: String = ""
    @SerializedName("url")
    val url: String = ""
    @SerializedName("account")
    val account: Account? = null
    @SerializedName("in_reply_to_id")
    val inReplyToId: Long = 0L
    @SerializedName("in_reply_to_account_id")
    val inReplyToAccountId: Long = 0L
    @SerializedName("reblog")
    val reblog: Status? = null
    @SerializedName("content")
    val content: String = ""
    @SerializedName("created_at")
    val createdAt: String = ""
    @SerializedName("reblogs_count")
    val reblogsCount: Long = 0L
    @SerializedName("favourites_count")
    val favouritesCount: Long = 0L
    @SerializedName("reblogged")
    val reblogged: Boolean = false
    @SerializedName("favourited")
    val favourited: Boolean = false
    @SerializedName("sensitive")
    val sensitive: Boolean = false
    @SerializedName("spoiler_text")
    val spoilerText: String = ""
    @SerializedName("visibility")
    val visibility: String = Visibility.Public.value
    @SerializedName("media_attachments")
    val mediaAttachments: List<Attachment>? = null
    @SerializedName("mentions")
    val mentions: List<Mention>? = null
    @SerializedName("tags")
    val tags: List<Tag>? = null
    @SerializedName("application")
    val application: Application? = null

    private var _spannedContent: Spanned? = null
    val spannedContent: Spanned?
        get() {
            if (_spannedContent == null) {
                _spannedContent = HtmlUtil.parse(content)
            }
            return _spannedContent
        }
}