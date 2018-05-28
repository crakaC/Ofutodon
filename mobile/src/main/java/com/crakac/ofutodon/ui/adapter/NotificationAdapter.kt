package com.crakac.ofutodon.ui.adapter

import android.content.Intent
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.entity.Notification
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.ui.UserActivity
import com.crakac.ofutodon.ui.widget.InlineImagePreview
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import com.crakac.ofutodon.ui.widget.StatusViewHolder
import com.crakac.ofutodon.util.GlideApp
import com.crakac.ofutodon.util.HtmlUtil

class NotificationAdapter : RefreshableAdapter<Notification>(), StatusChangeListener {

    var statusListener: OnClickStatusListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NotificationType.Mention.value, NotificationType.Favourite.value, NotificationType.Reblog.value -> {
                StatusViewHolder(View.inflate(parent.context, R.layout.status, null)).apply {
                    itemView.setOnClickListener { statusListener?.onItemClicked(getItem(adapterPosition).status!!) }
                    icon.setOnClickListener { v ->
                        statusListener?.onIconClicked(v as ImageView, getItem(adapterPosition).status!!)
                    }
                    reply.setOnClickListener { _ ->
                        statusListener?.onReplyClicked(reply, getItem(adapterPosition).status!!)
                    }
                    boost.setOnClickListener { _ ->
                        statusListener?.onBoostClicked(boost, getItem(adapterPosition).status!!)
                    }
                    favorite.setOnClickListener { _ ->
                        statusListener?.onFavoriteClicked(favorite, getItem(adapterPosition).status!!)
                    }
                    more.setOnClickListener { _ ->
                        val popup = PopupMenu(context, more)
                        popup.inflate(R.menu.status_popup)
                        popup.setOnMenuItemClickListener { item ->
                            val menuItemId = item.itemId
                            statusListener?.onMenuClicked(getItem(adapterPosition).status!!, menuItemId)
                            return@setOnMenuItemClickListener true
                        }
                        popup.show()
                    }
                    preview.setOnPreviewClickListener(object : InlineImagePreview.OnClickPreviewListener {
                        override fun onClick(attachmentIndex: Int) {
                            statusListener?.onClickAttachment(getItem(adapterPosition).status!!, attachmentIndex)
                        }
                    })
                    readMore.setOnClickListener { _ ->
                        val status = getItem(adapterPosition).status!!
                        val st = if (status.reblog != null) status.reblog!! else status
                        st.hasExpanded = !st.hasExpanded
                        toggleReadMore(st)
                    }
                }
            }
            NotificationType.Follow.value -> {
                FollowedNotificationViewHolder(View.inflate(parent.context, R.layout.followed_notification, null)).apply {
                    itemView.setOnClickListener { _ ->
                        val account = getItem(adapterPosition).account!!
                        val intent = Intent(parent.context, UserActivity::class.java)
                        UserActivity.setUserInfo(intent, account)
                        parent.context.startActivity(intent)
                    }
                }
            }
            else -> {
                StatusAdapter.FooterViewHolder(View.inflate(parent.context, R.layout.footer, null))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = getItem(position)
        val type = getItemViewType(position)
        when(type) {
            NotificationType.Mention.value -> {
                (holder as StatusViewHolder).setStatus(notification.status!!)
            }
            NotificationType.Favourite.value, NotificationType.Reblog.value  -> {
                (holder as StatusViewHolder).setNotification(notification)
            }
            NotificationType.Follow.value ->
                (holder as FollowedNotificationViewHolder).setNotification(notification)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = getItem(position).getType()
        return when (type) {
            Notification.Type.Mention -> NotificationType.Mention.value
            Notification.Type.ReBlog -> NotificationType.Reblog.value
            Notification.Type.Favourite -> NotificationType.Favourite.value
            Notification.Type.Follow -> NotificationType.Follow.value
        }
    }

    enum class NotificationType(val value: Int) {
        Footer(0),
        Mention(1),
        Reblog(2),
        Favourite(3),
        Follow(4)
    }

    class FollowedNotificationViewHolder(v: View) : RecyclerView.ViewHolder(v), Refreshable {
        val context get() = itemView.context
        val roundedCorners = RequestOptions().transform(RoundedCorners(8))
        val followedBy: TextView = v.findViewById(R.id.followed_by)
        val name: TextView = v.findViewById(R.id.display_name)
        val icon: ImageView = v.findViewById(R.id.icon)
        val userId: TextView = v.findViewById(R.id.user_id_with_domain)

        fun setNotification(notification: Notification) {
            val account = notification.account!!
            val nameWithEmoij = HtmlUtil.emojify(followedBy, context.getString(R.string.followed_by).format(account.displayName), account.emojis)
            followedBy.text = nameWithEmoij
            name.text = nameWithEmoij
            GlideApp.with(context.applicationContext).load(account.avatar).apply(roundedCorners).into(icon)
            userId.text = account.unicodeAcct
        }
    }

    private fun findByStatus(status: Status): Notification? = items.find { item -> item.status != null && item.status!!.id == status.id }

    override fun onUpdate(status: Status){
        val notification = findByStatus(status) ?: return
        notification.status = status
        update(notification)
    }
}

