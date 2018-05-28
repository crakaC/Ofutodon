package com.crakac.ofutodon.ui.widget

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.entity.Account
import com.crakac.ofutodon.api.entity.Notification
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.util.HtmlUtil
import com.crakac.ofutodon.util.TextUtil

class StatusViewHolder(v: View) : RecyclerView.ViewHolder(v), RefreshableAdapter.Refreshable {
    val context get() = itemView.context
    val roundedCorners = RequestOptions().transform(RoundedCorners(8))
    val actionedBy: TextView = v.findViewById(R.id.actioned_text)
    val actionedByIcon: ImageView = v.findViewById(R.id.actioned_by_icon)
    val actionedIcon: ImageView = v.findViewById(R.id.actioned_icon)
    val name: TextView = v.findViewById(R.id.display_name)
    val content: TextView = v.findViewById(R.id.status)
    val spoilerText: TextView = v.findViewById(R.id.spoiler_text)
    val readMore: Button = v.findViewById(R.id.read_more)
    val icon: ImageView = v.findViewById(R.id.icon)
    val originalIcon: ImageView = v.findViewById(R.id.original_user_icon)
    val createdAt: TextView = v.findViewById(R.id.createdAt)
    val reply: ImageView = v.findViewById(R.id.reply)
    val boost: ImageView = v.findViewById(R.id.boost)
    val followersOnly: ImageView = v.findViewById(R.id.followers_only)
    val direct: ImageView = v.findViewById(R.id.direct)
    val favorite: ImageView = v.findViewById(R.id.favorite)
    val more: ImageView = v.findViewById(R.id.more)
    val preview: InlineImagePreview = v.findViewById(R.id.preview)
    val statusActions: View = v.findViewById(R.id.status_actions)

    private var createdAtString: String? = null

    private val accrAppearance = TextAppearanceSpan(v.context, R.style.TextAppearance_AppCompat_Caption)

    init {
        content.movementMethod = ContentMovementMethod.instance
    }

    fun setStatus(status: Status) {
        if (status.reblog != null) {
            val original = status.reblog!!
            setup(original)
            enableActionedView(true)
            setupIcons(status.reblog!!.account, status.account)
            setRebloggedText(status.account)
        } else {
            setup(status)
            enableActionedView(false)
        }
        clearFilter()
    }

    fun setNotification(notification: Notification) {
        setup(notification.status!!)
        enableActionedView(true)
        setupIcons(notification.status!!.account, notification.account!!)
        when (notification.type) {
            Notification.Type.Favourite.value -> {
                setFavoritedText(notification.account)
            }
            Notification.Type.ReBlog.value -> {
                setRebloggedText(notification.account)
            }
        }
        setFilter()
    }

    override fun refresh() {
        createdAt.text = TextUtil.parseCreatedAt(createdAtString!!)
    }

    private fun setup(status: Status) {
        val sb = SpannableStringBuilder()
        sb.append(HtmlUtil.emojify(name, status.account.displayName, status.account.emojis))

        val start = sb.length
        sb.append(" @${status.account.unicodeAcct}")
        sb.setSpan(accrAppearance, start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        name.text = sb

        if (status.spannedContent == null) {
            status.spannedContent = HtmlUtil.emojify(content, status)
        }
        content.text = status.spannedContent
        Glide.with(context)
                .load(status.account.avatar)
                .apply(roundedCorners)
                .into(icon)
        createdAt.text = TextUtil.parseCreatedAt(status.createdAt)

        if (status.inReplyToId > 0) {
            reply.setImageResource(R.drawable.ic_reply_all)
        } else {
            reply.setImageResource(R.drawable.ic_reply)
        }

        if (status.isReblogged) {
            boost.setColorFilter(ContextCompat.getColor(context, R.color.boosted))
        } else {
            boost.clearColorFilter()
        }

        if (status.isFavourited) {
            favorite.setColorFilter(ContextCompat.getColor(context, R.color.favourited))
        } else {
            favorite.clearColorFilter()
        }

        when (status.visibility) {
            Status.Visibility.Direct.value -> {
                boost.visibility = View.GONE
                followersOnly.visibility = View.GONE
                direct.visibility = View.VISIBLE
            }
            Status.Visibility.Private.value -> {
                boost.visibility = View.GONE
                followersOnly.visibility = View.VISIBLE
                direct.visibility = View.GONE
            }
            Status.Visibility.UnListed.value -> {
                boost.visibility = View.VISIBLE
                followersOnly.visibility = View.GONE
                direct.visibility = View.GONE
            }
            else -> {
                boost.visibility = View.VISIBLE
                followersOnly.visibility = View.GONE
                direct.visibility = View.GONE
            }
        }

        if (status.mediaAttachments.isNotEmpty()) {
            preview.visibility = View.VISIBLE
            preview.setMedia(status.mediaAttachments, status.sensitive)
        } else {
            preview.visibility = View.GONE
        }
        toggleReadMore(status)
        createdAtString = status.createdAt
    }

    private fun enableActionedView(isEnabled: Boolean) {
        for (v in arrayOf(actionedBy, actionedByIcon, actionedIcon, originalIcon)) {
            v.visibility = if (isEnabled) View.VISIBLE else View.GONE
        }

        if (isEnabled) {
            Glide.with(icon).clear(icon)
        }
    }

    private fun setupIcons(originalAccount: Account, actionedAccount: Account) {
        Glide.with(context.applicationContext)
                .load(actionedAccount.avatar)
                .apply(roundedCorners)
                .into(actionedByIcon)
        Glide.with(context.applicationContext)
                .load(originalAccount.avatar)
                .apply(roundedCorners)
                .into(originalIcon)
    }

    fun toggleReadMore(status: Status) {
        if (status.hasSpoileredText()) {
            spoilerText.visibility = View.VISIBLE
            spoilerText.text = status.spoilerText
            readMore.visibility = View.VISIBLE
            if (status.hasExpanded) {
                readMore.text = context.getString(R.string.hide)
                content.visibility = View.VISIBLE
            } else {
                readMore.text = context.getString(R.string.read_more)
                content.visibility = View.GONE
            }
        } else {
            content.visibility = View.VISIBLE
            spoilerText.visibility = View.GONE
            readMore.visibility = View.GONE
        }
    }

    private fun setRebloggedText(account: Account) {
        actionedBy.text = context.getString(R.string.boosted_by)?.format(HtmlUtil.emojify(actionedBy, account.displayName, account.emojis))
        actionedIcon.setImageResource(R.drawable.ic_boost)
        actionedIcon.setColorFilter(ContextCompat.getColor(context, R.color.boosted))
    }

    private fun setFavoritedText(account: Account) {
        actionedBy.text = context.getString(R.string.favourited_by)?.format(HtmlUtil.emojify(actionedBy, account.displayName, account.emojis))
        actionedIcon.setImageResource(R.drawable.ic_star)
        actionedIcon.setColorFilter(ContextCompat.getColor(context, R.color.favourited))
    }

    private fun clearFilter() {
        for (v in arrayOf(content, statusActions, createdAt, spoilerText, readMore, name)) {
            v.alpha = 1f
        }
    }

    private fun setFilter() {
        originalIcon.setColorFilter(ContextCompat.getColor(context, R.color.notification_icon_mask), PorterDuff.Mode.SRC_ATOP)
        actionedByIcon.setColorFilter(ContextCompat.getColor(context, R.color.notification_icon_mask), PorterDuff.Mode.SRC_ATOP)
        for (v in arrayOf(content, statusActions, createdAt, spoilerText, readMore, name)) {
            v.alpha = 0.625f
        }
    }
}