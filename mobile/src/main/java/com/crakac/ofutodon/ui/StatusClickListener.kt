package com.crakac.ofutodon.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Status
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.ui.adapter.StatusAdapter
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

open class StatusClickListener(context: Activity) : OnClickStatusListener {
    private val contextRef = WeakReference(context)
    private val context get() = contextRef.get()

    override fun onItemClicked(status: Status) {
        val intent = Intent(context, ConversationActivity::class.java)
        ConversationActivity.setStatus(intent, status.reblog ?: status)
        context?.startActivity(intent)
    }

    override fun onIconClicked(icon: ImageView, status: Status) {
        val intent = Intent(context, UserActivity::class.java)
        UserActivity.setUserInfo(intent, status.reblog?.account ?: status.account)
        context?.startActivity(intent)
    }

    override fun onReplyClicked(icon: ImageView, status: Status) {
        val st = status.reblog ?: status
        val intent = Intent(context, TootActivity::class.java)
        val iconId = if (st.inReplyToId > 0) R.drawable.ic_reply_all else R.drawable.ic_reply
        FabTransform.addExtras(intent, ContextCompat.getColor(context!!, R.color.background_mastodon), R.color.mastodon_grey, iconId, icon.alpha)
        TootActivity.addReplyInfo(intent, st)
        val options = ActivityOptions.makeSceneTransitionAnimation(context!!, icon, context?.getString(R.string.transition_name_toot_dialog));
        context?.startActivity(intent, options.toBundle())
    }

    override fun onBoostClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        reblogSuccess(it)
                    }
                } else {
                    updateStatus(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                updateStatus(status)
            }

            fun reblogSuccess(newStatus: Status) {
                val isReblogAction = newStatus.reblog != null
                if (isReblogAction) {
                    if (status.reblog != null) {
                        status.reblog = newStatus.reblog
                    } else {
                        status.isReblogged = true
                    }
                    updateStatus(status)
                } else {
                    status.isReblogged = false
                    if (status.reblog != null) {
                        status.reblog!!.isReblogged = false
                    }
                    updateStatus(status)
                }
            }
        }

        if (status.isBoosted) {
            Mastodon.api.unreblogStatus(status.originalId).enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            Mastodon.api.reblogStatus(status.originalId).enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context!!, R.color.boosted))
        }
    }

    override fun onFavoriteClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                context?.let {
                    if (response != null && response.isSuccessful) {
                        response.body()?.let {
                            favoriteSuccess(it)
                        }
                    } else {
                        updateStatus(status)
                    }
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                context?.let {
                    updateStatus(status)
                }
            }

            fun favoriteSuccess(newStatus: Status) {
                if (status.reblog != null) {
                    status.reblog = newStatus
                    updateStatus(status)
                } else {
                    updateStatus(newStatus)
                }
            }
        }
        if (status.isFaved) {
            Mastodon.api.unfavouriteStatus(status.originalId).enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            Mastodon.api.favouriteStatus(status.originalId).enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context!!, R.color.favourited))
        }
    }

    override fun onMenuClicked(status: Status, menuId: Int) {
        Mastodon.api.run {

        }
    }

    override fun onClickAttachment(status: Status, attachmentIndex: Int) {
        val intent = Intent(context, AttachmentsPreviewActivity::class.java)
        AttachmentsPreviewActivity.setup(intent, status, attachmentIndex)
        context?.startActivity(intent)
    }

    open fun updateStatus(status: Status) {
        StatusAdapter.update(status)
    }
}