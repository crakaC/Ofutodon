package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.widget.InlineImagePreview
import com.crakac.ofutodon.util.TextUtil
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.util.*


class StatusAdapter(val context: Context) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {
    val TAG: String = "StatusAdapter"
    val statusArray = ArrayList<Status>()
    val ids = TreeSet<Long>()
    val dummy = Status(-1)

    var statusListener: OnClickStatusListener? = null

    fun getItem(position: Int): Status {
        if (position == statusArray.size) {
            return dummy
        }
        return statusArray[position]
    }

    fun contains(id: Long): Boolean {
        return ids.contains(id)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getPositionById(id: Long): Int? {
        return statusArray.indexOfFirst { e -> e.id == id }
    }

    fun getItemById(id: Long): Status {
        return statusArray.first { e -> e.id == id }
    }

    fun getPosition(item: Status): Int {
        return statusArray.indexOf(item)
    }

    fun addTop(status: Status) {
        statusArray.add(0, status)
        ids.add(status.id)
        notifyItemInserted(0)
    }

    fun addTop(statuses: Collection<Status>) {
        statusArray.addAll(0, statuses)
        statuses.forEach { e -> ids.add(e.id) }
        notifyItemRangeInserted(0, statuses.size)
    }

    fun addBottom(statuses: Collection<Status>) {
        val oldSize = itemCount
        statusArray.addAll(statuses)
        ids.addAll(statuses.map { e -> e.id })
        notifyItemRangeInserted(oldSize, statuses.size)
    }

    fun update(status: Status) {
        getPositionById(status.id)?.let { pos ->
            statusArray[pos] = status
            notifyItemChanged(pos)
        }
    }

    fun removeById(id: Long) {
        val target = statusArray.find { it.id == id }
        target?.let {
            val pos = getPosition(target)
            statusArray.remove(target)
            ids.remove(id)
            notifyItemRemoved(pos)
        }
    }

    val isEmpty: Boolean
        get() {
            return statusArray.isEmpty()
        }

    override fun onBindViewHolder(holder: StatusViewHolder?, position: Int) {
        val item = getItem(position)
        if (holder is StatusHolder) {
            holder.setData(context, item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StatusViewHolder {
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
        return holder
    }

    override fun getItemCount(): Int {
        return if (isEmpty) 0 else statusArray.size + 1
    }

    override fun onViewRecycled(holder: StatusViewHolder?) {
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

    abstract class StatusViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        open fun updateRelativeTime(){}
    }

    class StatusHolder(v: View) : StatusViewHolder(v) {
        @BindView(R.id.reblogged_by_icon)
        lateinit var rebloggedByIcon: ImageView

        @BindView(R.id.reblogged_by_name)
        lateinit var rebloggedBy: TextView

        @BindView(R.id.reblogged_icon)
        lateinit var rebloggedMark: ImageView

        @BindView(R.id.displayName)
        lateinit var name: TextView

        @BindView(R.id.status)
        lateinit var content: TextView

        @BindView(R.id.icon)
        lateinit var icon: ImageView

        @BindView(R.id.createdAt)
        lateinit var createdAt: TextView

        @BindView(R.id.reply)
        lateinit var reply: ImageView

        @BindView(R.id.boost)
        lateinit var boost: ImageView

        @BindView(R.id.followers_only)
        lateinit var followersOnly: ImageView

        @BindView(R.id.direct)
        lateinit var direct: ImageView

        @BindView(R.id.favorite)
        lateinit var favorite: ImageView

        @BindView(R.id.more)
        lateinit var more: ImageView

        @BindView(R.id.preview)
        lateinit var preview: InlineImagePreview

        var createdAtString: String? = null

        val accrAppearance: TextAppearanceSpan

        init {
            ButterKnife.bind(this, v)
            accrAppearance = TextAppearanceSpan(v.context, R.style.TextAppearance_AppCompat_Caption)
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

        override fun updateRelativeTime() {
            createdAt.text = TextUtil.parseCreatedAt(createdAtString!!)
        }

        private fun setup(context: Context, status: Status) {
            val sb = SpannableStringBuilder()
            sb.append(status.account.dispNameWithEmoji)

            val start = sb.length
            sb.append(" @${status.account.acct}")
            sb.setSpan(accrAppearance, start, sb.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            name.text = sb

            content.text = status.spannedContent!!
            Glide.with(context)
                    .load(status.account.avatar)
                    .centerCrop()
                    .crossFade()
                    .bitmapTransform(CropCircleTransformation(context))
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
                preview.setMedia(status.mediaAttachments)
            } else {
                preview.visibility = View.GONE
            }

            createdAtString = status.createdAt
        }

        private fun enableReblogView(isEnabled: Boolean) {
            for (v in arrayOf(rebloggedBy, rebloggedByIcon, rebloggedMark)) {
                v.visibility = if (isEnabled) View.VISIBLE else View.GONE
            }
        }

        private fun setupRebloggedStatus(context: Context, status: Status) {
            Glide.with(context)
                    .load(status.account.avatar)
                    .fitCenter()
                    .crossFade()
                    .bitmapTransform(CropCircleTransformation(context))
                    .into(rebloggedByIcon)
            rebloggedBy.text = context.getString(R.string.boosted_by).format(status.account.dispNameWithEmoji)
        }
    }

    class FooterHolder(v: View) : StatusViewHolder(v)

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
