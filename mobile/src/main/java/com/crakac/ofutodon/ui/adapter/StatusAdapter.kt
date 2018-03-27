package com.crakac.ofutodon.ui.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.widget.ContentMovementMethod
import com.crakac.ofutodon.ui.widget.InlineImagePreview
import com.crakac.ofutodon.ui.widget.RefreshableViewHolder
import com.crakac.ofutodon.util.HtmlUtil
import com.crakac.ofutodon.util.TextUtil


class StatusAdapter(context: Context, val showBottomLoading: Boolean = true) : RefreshableAdapter<Status>(context) {
    val TAG: String = "StatusAdapter"
    val dummy = Status(-1)

    var statusListener: OnClickStatusListener? = null

    override fun getItem(position: Int): Status {
        if (isFooter(position)) {
            return dummy
        }
        return super.getItem(position)
    }

    override fun getItemCount(): Int {
        return when {
            isEmpty -> 0
            showBottomLoading -> super.getItemCount() + 1
            else -> super.getItemCount()
        }
    }

    private fun isFooter(position: Int): Boolean {
        return showBottomLoading && position >= itemCount - 1
    }

    override fun onBindViewHolder(holder: RefreshableViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is StatusHolder) {
            holder.setData(context, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        if (viewType == HolderType.Footer.rawValue) {
            return FooterHolder(View.inflate(context, R.layout.dummy_status, null))
        }
        val holder = StatusHolder(View.inflate(context, R.layout.status, null))
        holder.itemView.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            statusListener?.onItemClicked(status)
        }

        holder.icon.setOnClickListener { v ->
            statusListener?.onIconClicked(v as ImageView, getItem(holder.adapterPosition))
        }

        holder.reply.setOnClickListener { _ ->
            statusListener?.onReplyClicked(holder.reply, getItem(holder.adapterPosition))
        }

        holder.boost.setOnClickListener { _ ->
            statusListener?.onBoostClicked(holder.boost, getItem(holder.adapterPosition))
        }

        holder.favorite.setOnClickListener { _ ->
            statusListener?.onFavoriteClicked(holder.favorite, getItem(holder.adapterPosition))
        }

        holder.more.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            val popup = PopupMenu(context, holder.more)
            popup.inflate(R.menu.status_popup)
            popup.setOnMenuItemClickListener { item ->
                val menuItemId = item.itemId
                statusListener?.onMenuClicked(status, menuItemId)
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }

        holder.preview.setOnPreviewClickListener(object : InlineImagePreview.OnClickPreviewListener {
            override fun onClick(attachmentIndex: Int) {
                statusListener?.onClickAttachment(getItem(holder.adapterPosition), attachmentIndex)
            }
        })

        holder.readMore.setOnClickListener { _ ->
            val status = getItem(holder.adapterPosition)
            val st = if(status.reblog != null) status.reblog!! else status
            st.hasExpanded = !st.hasExpanded
            holder.toggleReadMore(context, st)
        }
        return holder
    }

    override fun onViewRecycled(holder: RefreshableViewHolder) {
        if (holder is StatusHolder) {
            holder.icon.setImageBitmap(null)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItemId(position) < 0) {
            HolderType.Footer.rawValue
        } else {
            HolderType.Status.rawValue
        }
    }

    private enum class HolderType(val rawValue: Int) {
        Status(3),
        Footer(4)
    }

    open class StatusViewHolder(v: View) : RefreshableViewHolder(v)

    class FooterHolder(v: View) : StatusViewHolder(v)

    class StatusHolder(v: View) : StatusViewHolder(v) {
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

    interface OnClickStatusListener {
        fun onItemClicked(status: Status)
        fun onIconClicked(icon: ImageView, status: Status)
        fun onReplyClicked(icon: ImageView, status: Status)
        fun onBoostClicked(icon: ImageView, status: Status)
        fun onFavoriteClicked(icon: ImageView, status: Status)
        fun onMenuClicked(status: Status, menuId: Int)
        fun onClickAttachment(status: Status, attachmentIndex: Int)
    }
}