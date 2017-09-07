package com.crakac.ofutodon.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.Link
import com.crakac.ofutodon.model.api.MastodonStreaming
import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.Range
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.transition.FabTransform
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Kosuke on 2017/04/26.
 */
abstract class StatusFragment : Fragment(),
        SwipeRefreshLayout.OnRefreshListener,
        MastodonStreaming.StreamingCallback,
        SwipeRefreshListView.OnLoadMoreListener,
        StatusAdapter.OnClickStatusListener {

    lateinit var recyclerView: RecyclerView
    @BindView(R.id.swipeRefresh)
    lateinit var swipeRefresh: SwipeRefreshListView
    lateinit var unbinder: Unbinder
    lateinit var adapter: StatusAdapter
    lateinit var layoutManager: LinearLayoutManager

    var nextRange: Range = Range()
    var prevRange: Range = Range()

    var streaming: MastodonStreaming? = null

    var isLoadingNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        unbinder = ButterKnife.bind(this, view)
        adapter = StatusAdapter(activity)
        adapter.statusListener = this

        recyclerView = swipeRefresh.recyclerView
        recyclerView.adapter = adapter
        layoutManager = FastScrollLinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val divider = DividerItemDecoration(activity, layoutManager.orientation).apply {
            setDrawable(ContextCompat.getDrawable(activity, R.drawable.divider))
        }
        recyclerView.addItemDecoration(divider)

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setOnLoadMoreListener(this)

//        streaming = MastodonStreaming("friends.nico")
        swipeRefresh.isRefreshing = true
        onRefresh()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.statusListener = null
        unbinder.unbind()
    }

    override fun onStop() {
        super.onStop()
        streaming?.callBack = null
        streaming?.close()
    }

    override fun onStart() {
        super.onStart()
        connectStreamingIfNeeded()
        updateRelativeTime()
    }

    abstract fun onRefreshRequest(): Call<List<Status>>?

    override fun onRefresh() {
        onRefreshRequest()?.enqueue(onStatus)
    }

    val onStatus = object : Callback<List<Status>> {
        override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
            if (!isAdded) return
            swipeRefresh.isRefreshing = false
            updateRelativeTime()
        }

        override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
            if (!isAdded) return
            updateRelativeTime()
            swipeRefresh.isRefreshing = false
            if (response == null || !response.isSuccessful) {
                return
            }
            response.body()?.let {
                insertQuietly(it)
            }
            Link.parse(response.headers().get("link"))?.let {
                prevRange = it.prevRange()
                if (nextRange.maxId == null) {
                    nextRange = it.nextRange()
                }
            }
            connectStreamingIfNeeded()
        }
    }

    abstract fun onLoadMoreRequest(): Call<List<Status>>?

    override fun onLoadMore() {
        onLoadMoreRequest()?.run {
            enqueue(onNextStatus)
            isLoadingNext = true
        }
    }

    val onNextStatus = object : Callback<List<Status>> {
        override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
            isLoadingNext = false
        }

        override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
            isLoadingNext = false
            if (response == null || !response.isSuccessful || !isAdded) {
                return
            }
            response.body()?.let {
                adapter.addBottom(it)
            }
            Link.parse(response.headers().get("link"))?.let {
                nextRange = it.nextRange()
            }
        }
    }

    var firstVisibleStatus: Status? = null
    var firstVisibleOffset = 0

    private fun savePosition() {
        if (adapter.isEmpty || recyclerView.getChildAt(0) == null) {
            return
        }
        firstVisibleStatus = adapter.getItem(layoutManager.findFirstVisibleItemPosition())
        firstVisibleOffset = recyclerView.getChildAt(0).top
    }

    private fun restorePosition() {
        firstVisibleStatus?.let {
            val pos = adapter.getPosition(it)
            layoutManager.scrollToPositionWithOffset(pos, firstVisibleOffset)
        }
    }

    fun insertQuietly(statuses: List<Status>) {
        savePosition()
        adapter.addTop(statuses)
        restorePosition()
    }

    override fun onStatus(status: Status?) {
        status?.let {
            if (!adapter.contains(status.id)) {
                adapter.addTop(status)
            }
        }
    }

    override fun onNotification(notification: Notification?) {
    }

    override fun onDelete(id: Long?) {
        id?.let {
            adapter.removeById(id)
        }
    }

    override fun onItemClicked(status: Status) {
    }

    override fun onIconClicked(icon: ImageView, status: Status) {
        val intent = Intent(activity, UserActivity::class.java)
        UserActivity.setUserInfo(intent, status.reblog?.account ?: status.account)
        startActivity(intent)
    }

    override fun onReplyClicked(icon: ImageView, status: Status) {
        val intent = Intent(activity, TootActivity::class.java)
        FabTransform.addExtras(intent, ContextCompat.getColor(activity, R.color.background_dark), R.drawable.ic_reply, icon.alpha)
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
                        reblogSuccess(adapter.getItemById(status.id), it)
                    }
                } else {
                    adapter.update(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                if (!isAdded) return
                adapter.update(status)
            }

            fun reblogSuccess(oldStatus: Status, newStatus: Status) {
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
                unreblogStatus(status.apiId)
            }?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.run {
                reblogStatus(status.apiId)
            }?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context, R.color.boosted))
        }
    }

    override fun onFavoriteClicked(icon: ImageView, status: Status) {
        val onResponse = object : Callback<Status> {
            override fun onResponse(call: Call<Status>?, response: Response<Status>?) {
                if (!isAdded) return

                if (response != null && response.isSuccessful) {
                    response.body()?.let {
                        favoriteSuccess(adapter.getItemById(status.id), it)
                    }
                } else {
                    adapter.update(status)
                }
            }

            override fun onFailure(call: Call<Status>?, t: Throwable?) {
                if (!isAdded) return
                adapter.update(status)
            }

            fun favoriteSuccess(oldStatus: Status, newStatus: Status) {
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
                unfavouriteStatus(status.apiId)
            }?.enqueue(onResponse)
            icon.clearColorFilter()
        } else {
            MastodonUtil.api?.run {
                favouriteStatus(status.apiId)
            }?.enqueue(onResponse)
            icon.setColorFilter(ContextCompat.getColor(context, R.color.favourited))
        }
    }

    override fun onMenuClicked(status: Status, menuId: Int) {
        MastodonUtil.api?.run {

        }
    }

    fun connectStreamingIfNeeded() {
        if (streaming == null || streaming!!.isConnected || adapter.isEmpty)
            return

        streaming?.callBack = this
        streaming?.connect()
    }

    abstract fun getTitle(): String

    fun updateRelativeTime() {
        if (!isAdded) return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as StatusAdapter.StatusViewHolder?
            holder?.updateRelativeTime()
        }
    }

    fun scrollToTop() {
        if (!isAdded) return
        recyclerView.scrollToPosition(0)
    }

    override fun onClickAttachment(status: Status, attachmentIndex: Int) {
        val intent = Intent(activity, AttachmentsPreviewActivity::class.java)
        AttachmentsPreviewActivity.setup(intent, status, attachmentIndex)
        startActivity(intent)
    }

    private class Divider(context: Context, orientation: Int): DividerItemDecoration(context, orientation){

    }
}