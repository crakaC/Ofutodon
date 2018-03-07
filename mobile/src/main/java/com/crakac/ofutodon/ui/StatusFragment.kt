package com.crakac.ofutodon.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.transition.FabTransform
import com.crakac.ofutodon.ui.widget.RefreshableAdapter
import com.crakac.ofutodon.ui.widget.RefreshableViewHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Kosuke on 2017/04/26.
 */
abstract class StatusFragment : MastodonApiFragment<Status, List<Status>>(), StatusAdapter.OnClickStatusListener {

    abstract fun getTitle(): String

    override fun createAdapter(context: Context): RefreshableAdapter<Status> {
        val adapter = StatusAdapter(context)
        adapter.statusListener = this
        return adapter
    }

    override fun onRefreshSuccess(response: List<Status>) {
        insertQuietly(response)
    }

    override fun onLoadMoreSuccess(response: List<Status>) {
        adapter.addBottom(response)
    }

    override fun onItemClicked(status: Status) {
        val intent = Intent(activity, ConversationActivity::class.java)
        ConversationActivity.setStatus(intent, status)
        startActivity(intent)
    }

    override fun onIconClicked(icon: ImageView, status: Status) {
        val intent = Intent(activity, UserActivity::class.java)
        UserActivity.setUserInfo(intent, status.reblog?.account ?: status.account)
        startActivity(intent)
    }

    override fun onReplyClicked(icon: ImageView, status: Status) {
        val intent = Intent(activity, TootActivity::class.java)
        FabTransform.addExtras(intent, ContextCompat.getColor(requireContext(), R.color.background_dark), R.drawable.ic_reply, icon.alpha)
        TootActivity.addReplyInfo(intent, status)
        val options = ActivityOptions.makeSceneTransitionAnimation(activity, icon, getString(R.string.transition_name_toot_dialog));
        startActivity(intent, options.toBundle())
    }

    override fun onBoostClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                if (!isAdded) return

                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        reblogSuccess(status.id, it)
                    }
                } else {
                    adapter.update(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                if (!isAdded) return
                adapter.update(status)
            }

            fun reblogSuccess(oldStatusId: Long, newStatus: Status) {
                val oldStatus = adapter.getItemById(oldStatusId) ?: return
                val isReblogAction = newStatus.reblog != null
                if (isReblogAction) {
                    if (oldStatus.reblog != null) {
                        oldStatus.reblog = newStatus.reblog
                    } else {
                        oldStatus.isReblogged = true
                    }
                    adapter.update(oldStatus)
                } else {
                    oldStatus.isReblogged = false
                    if (oldStatus.reblog != null) {
                        oldStatus.reblog!!.isReblogged = false
                    }
                    adapter.update(oldStatus)
                }
            }
        }

        if (status.isBoosted) {
            MastodonUtil.api?.run {
                unreblogStatus(status.originalId)
            }?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.run {
                reblogStatus(status.originalId)
            }?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.boosted))
        }
    }

    override fun onFavoriteClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                if (!isAdded) return

                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        favoriteSuccess(status.id, it)
                    }
                } else {
                    adapter.update(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                if (!isAdded) return
                adapter.update(status)
            }

            fun favoriteSuccess(oldStatusId: Long, newStatus: Status) {
                val oldStatus = adapter.getItemById(oldStatusId) ?: return
                if (oldStatus.reblog != null) {
                    oldStatus.reblog = newStatus
                    adapter.update(oldStatus)
                } else {
                    adapter.update(newStatus)
                }
            }
        }
        if (status.isFaved) {
            MastodonUtil.api?.run {
                unfavouriteStatus(status.originalId)
            }?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.run {
                favouriteStatus(status.originalId)
            }?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.favourited))
        }
    }

    override fun onMenuClicked(status: Status, menuId: Int) {
        MastodonUtil.api?.run {

        }
    }

    fun updateRelativeTime() {
        if (!isAdded) return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as RefreshableViewHolder?
            holder?.refresh()
        }
    }

    override fun onClickAttachment(status: Status, attachmentIndex: Int) {
        val intent = Intent(activity, AttachmentsPreviewActivity::class.java)
        AttachmentsPreviewActivity.setup(intent, status, attachmentIndex)
        startActivity(intent)
    }

}