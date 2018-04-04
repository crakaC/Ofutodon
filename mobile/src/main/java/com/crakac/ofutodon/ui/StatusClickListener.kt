package com.crakac.ofutodon.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.ui.adapter.StatusAdapter
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class StatusClickListener(context: Activity) : OnClickStatusListener {
    val contextRef = WeakReference(context)
    val context get() = contextRef.get()
    var adapter: StatusAdapter? = null

    override fun onItemClicked(status: Status) {
        val intent = Intent(context, ConversationActivity::class.java)
        ConversationActivity.setStatus(intent, status)
        context?.startActivity(intent)
    }

    override fun onIconClicked(icon: ImageView, status: Status) {
        val intent = Intent(context, UserActivity::class.java)
        UserActivity.setUserInfo(intent, status.reblog?.account ?: status.account)
        context?.startActivity(intent)
    }

    override fun onReplyClicked(icon: ImageView, status: Status) {
        val intent = Intent(context, TootActivity::class.java)
        FabTransform.addExtras(intent, ContextCompat.getColor(context!!, R.color.background_dark), R.drawable.ic_reply, icon.alpha)
        TootActivity.addReplyInfo(intent, status)
        val options = ActivityOptions.makeSceneTransitionAnimation(context!!, icon, context?.getString(R.string.transition_name_toot_dialog));
        context?.startActivity(intent, options.toBundle())
    }

    override fun onBoostClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        reblogSuccess(status.id, it)
                    }
                } else {
                    adapter?.update(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                adapter?.update(status)
            }

            fun reblogSuccess(oldStatusId: Long, newStatus: Status) {
                val oldStatus = adapter?.getItemById(oldStatusId) ?: return
                val isReblogAction = newStatus.reblog != null
                if (isReblogAction) {
                    if (oldStatus.reblog != null) {
                        oldStatus.reblog = newStatus.reblog
                    } else {
                        oldStatus.isReblogged = true
                    }
                    adapter?.update(oldStatus)
                } else {
                    oldStatus.isReblogged = false
                    if (oldStatus.reblog != null) {
                        oldStatus.reblog!!.isReblogged = false
                    }
                    adapter?.update(oldStatus)
                }
            }
        }

        if (status.isBoosted) {
            MastodonUtil.api?.unreblogStatus(status.originalId)?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.reblogStatus(status.originalId)?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context!!, R.color.boosted))
        }
    }

    override fun onFavoriteClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                context?.let{
                    if (response != null && response.isSuccessful) {
                        response.body()?.let {
                            favoriteSuccess(status.id, it)
                        }
                    } else {
                        adapter?.update(status)
                    }
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                context?.let{
                    adapter?.update(status)
                }
            }

            fun favoriteSuccess(oldStatusId: Long, newStatus: Status) {
                val oldStatus = adapter?.getItemById(oldStatusId) ?: return
                if (oldStatus.reblog != null) {
                    oldStatus.reblog = newStatus
                    adapter?.update(oldStatus)
                } else {
                    adapter?.update(newStatus)
                }
            }
        }
        if (status.isFaved) {
            MastodonUtil.api?.unfavouriteStatus(status.originalId)?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.favouriteStatus(status.originalId)?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context!!, R.color.favourited))
        }
    }

    override fun onMenuClicked(status: Status, menuId: Int) {
        MastodonUtil.api?.run {

        }
    }

    override fun onClickAttachment(status: Status, attachmentIndex: Int) {
        val intent = Intent(context, AttachmentsPreviewActivity::class.java)
        AttachmentsPreviewActivity.setup(intent, status, attachmentIndex)
        context?.startActivity(intent)
    }
}