package com.crakac.ofutodon.ui.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.ui.UserActivity
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import com.crakac.ofutodon.ui.widget.StatusViewHolder
import com.crakac.ofutodon.util.GlideApp
import java.lang.ref.WeakReference

class NotificationAdapter(context: Activity) : RefreshableAdapter<Notification>(context) {

    var statusListener: OnClickStatusListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            NotificationType.Mention.value, NotificationType.Favourite.value, NotificationType.Reblog.value -> {
                StatusViewHolder(context!!, View.inflate(context, R.layout.status, null))
            }
            NotificationType.Follow.value -> {
                FollowedNotificationViewHolder(context!!, View.inflate(context, R.layout.followed_notification, null)).apply {
                    itemView.setOnClickListener { _ ->
                        val account = getItem(adapterPosition).account!!
                        val intent = Intent(context, UserActivity::class.java)
                        UserActivity.setUserInfo(intent, account)
                        context?.startActivity(intent)
                    }
                }
            }
            else -> {
                StatusAdapter.FooterViewHolder(View.inflate(context, R.layout.footer, null))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = getItem(position)
        val type = getItemViewType(position)
        when {
            type == NotificationType.Mention.value -> (holder as StatusViewHolder).setStatus(notification.status!!)
            holder is StatusViewHolder -> {
                holder.setNotification(notification)
                holder.resetListener(notification.status!!, statusListener)
            }
            holder is FollowedNotificationViewHolder -> holder.setNotification(notification)
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

    class FollowedNotificationViewHolder(context: Context, v: View) : RecyclerView.ViewHolder(v), Refreshable {
        val contextRef = WeakReference(context)
        val context get() = contextRef.get()
        val roundedCorners = RequestOptions().transform(RoundedCorners(8))
        val followedBy: TextView = v.findViewById(R.id.followed_by)
        val name: TextView = v.findViewById(R.id.displayName)
        val icon: ImageView = v.findViewById(R.id.icon)
        val userId: TextView = v.findViewById(R.id.user_id_with_domain)

        fun setNotification(notification: Notification) {
            val account = notification.account!!
            followedBy.text = context?.getString(R.string.followed_by)?.format(account.displayName)
            name.text = account.dispNameWithEmoji
            GlideApp.with(context!!).load(account.avatar).apply(roundedCorners).into(icon)
            userId.text = account.acct
        }
    }
}

