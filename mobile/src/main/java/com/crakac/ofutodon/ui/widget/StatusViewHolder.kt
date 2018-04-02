package com.crakac.ofutodon.ui.widget

import android.content.Context
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
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.util.HtmlUtil
import com.crakac.ofutodon.util.TextUtil

class StatusViewHolder(v: View) : RecyclerView.ViewHolder(v), RefreshableAdapter.Refreshable {
    val roundedCorners = RequestOptions().transform(RoundedCorners(8))

    val actionedBy: TextView = v.findViewById(R.id.actioned_text)
    val actionedByIcon: ImageView = v.findViewById(R.id.actioned_by_icon)
    val actionedIcon: ImageView = v.findViewById(R.id.actioned_icon)
    val name: TextView = v.findViewById(R.id.displayName)
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

    private var createdAtString: String? = null

    private val accrAppearance = TextAppearanceSpan(v.context, R.style.TextAppearance_AppCompat_Caption)

    init {
        content.movementMethod = ContentMovementMethod.instance
    }

    fun setData(context: Context, status: Status) {
        if (status.reblog != null) {
            val original = status.reblog!!
            setup(context, original)
            enableReblogView(true)
            setupRebloggedStatus(context, status)
        } else {
            setup(context, status)
            enableReblogView(false)
        }
    }

    override fun refresh() {
        createdAt.text = TextUtil.parseCreatedAt(createdAtString!!)
    }

    private fun setup(context: Context, status: Status) {
        val sb = SpannableStringBuilder()
        sb.append(status.account.dispNameWithEmoji)

        val start = sb.length
        sb.append(" @${status.account.acct}")
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
        toggleReadMore(context, status)
        createdAtString = status.createdAt
    }

    private fun enableReblogView(isEnabled: Boolean) {
        for (v in arrayOf(actionedBy, actionedByIcon, actionedIcon, originalIcon)) {
            v.visibility = if (isEnabled) View.VISIBLE else View.GONE
        }

        if(isEnabled){
            Glide.with(icon).clear(icon)
        }
    }

    private fun setupRebloggedStatus(context: Context, status: Status) {
        Glide.with(context)
                .load(status.account.avatar)
                .apply(roundedCorners)
                .into(actionedByIcon)
        Glide.with(context)
                .load(status.reblog!!.account.avatar)
                .apply(roundedCorners)
                .into(originalIcon)
        actionedBy.text = context.getString(R.string.boosted_by).format(status.account.dispNameWithEmoji)
    }

    fun toggleReadMore(context: Context, status: Status) {
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
}