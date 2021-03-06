package com.crakac.ofutodon.api.entity

import android.text.Spanned
import com.google.gson.annotations.SerializedName

class Status(id: Long = 0L) : Identifiable(id) {
    enum class Visibility(val value: String) {
        Public("public"),
        UnListed("unlisted"),
        Private("private"),
        Direct("direct")
    }

    @SerializedName("uri")
    val uri: String = ""
    @SerializedName("url")
    val url: String = ""
    @SerializedName("account")
    val account: Account = Account()
    @SerializedName("in_reply_to_id")
    val inReplyToId: Long = 0L
    @SerializedName("in_reply_to_account_id")
    val inReplyToAccountId: Long = 0L
    @SerializedName("reblog")
    var reblog: Status? = null
    @SerializedName("content")
    val content: String = ""
    @SerializedName("created_at")
    val createdAt: String = ""
    @SerializedName("reblogs_count")
    val reblogsCount: Long = 0L
    @SerializedName("favourites_count")
    val favouritesCount: Long = 0L
    @SerializedName("reblogged")
    var isReblogged: Boolean = false
    @SerializedName("favourited")
    var isFavourited: Boolean = false
    @SerializedName("sensitive")
    val sensitive: Boolean = false
    @SerializedName("spoiler_text")
    val spoilerText: String = ""
    @SerializedName("visibility")
    val visibility: String = Visibility.Public.value
    @SerializedName("media_attachments")
    val mediaAttachments: List<Attachment> = emptyList()
    @SerializedName("mentions")
    val mentions: List<Mention> = emptyList()
    @SerializedName("tags")
    val tags: List<Tag> = emptyList()
    @SerializedName("application")
    val application: Application = Application()
    @SerializedName("emojis")
    val emojis: List<Emoji> = emptyList()

    @Transient
    var spannedContent: Spanned? = null

    fun getVisibility(): Visibility {
        return Visibility.values().first { e -> e.value == visibility }
    }

    val originalId: Long
        get() {
            return reblog?.id ?: id
        }

    val isFaved: Boolean
        get() {
            return reblog?.isFavourited ?: isFavourited
        }

    val isBoosted: Boolean
        get() {
            return reblog?.isReblogged ?: isReblogged
        }

    fun hasSpoileredText() = spoilerText.isNotEmpty()
    var hasExpanded = false
}