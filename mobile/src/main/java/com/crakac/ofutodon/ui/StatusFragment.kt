package com.crakac.ofutodon.ui

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Kosuke on 2017/04/26.
 */
class StatusFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, MastodonStreaming.StreamingCallback, SwipeRefreshListView.OnLoadMoreListener {

    lateinit var recyclerView: RecyclerView
    @BindView(R.id.swipeRefresh)
    lateinit var swipeRefresh: SwipeRefreshListView
    lateinit var unbinder: Unbinder
    lateinit var adapter: StatusAdapter
    lateinit var layoutManager: LinearLayoutManager

    var nextRange: Range = Range()
    var prevRange: Range = Range()

    var streaming: MastodonStreaming? = null

    private var isLoadingNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        unbinder = ButterKnife.bind(this, view)
        adapter = StatusAdapter(activity)

        recyclerView = swipeRefresh.recyclerView
        recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val divider = DividerItemDecoration(activity, layoutManager.orientation).apply {
            setDrawable(ContextCompat.getDrawable(activity, R.drawable.divider))
        }
        recyclerView.addItemDecoration(divider)

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setOnLoadMoreListener(this)

        streaming = MastodonStreaming()
        streaming?.callBack = this
//        streaming?.connect()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
        streaming?.callBack = null
        streaming?.close()
    }

    fun getTitle(): String = "テスト"

    override fun onRefresh() {
        MastodonUtil.api?.getHomeTimeline(prevRange.q)?.enqueue(onStatus)
    }

    override fun onLoadMore() {
        if(isLoadingNext) return
        MastodonUtil.api?.getHomeTimeline(nextRange.q)?.enqueue(onNextStatus)
        isLoadingNext = true
    }


    private val onStatus = object : Callback<List<Status>> {
        override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
            swipeRefresh.isRefreshing = false
        }

        override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
            swipeRefresh.isRefreshing = false
            if (response == null || !response.isSuccessful) {
                return
            }
            insertQuietly(response.body())
            Link.parse(response.headers().get("link"))?.let{
                prevRange = it.prevRange()
                if(nextRange.maxId == null){
                    nextRange = it.nextRange()
                }
            }
        }
    }

    private val onNextStatus = object : Callback<List<Status>> {
        override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
            isLoadingNext = false
        }

        override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
            isLoadingNext = false
            if (response == null || !response.isSuccessful) {
                return
            }
            adapter.addBottom(response.body())
            Link.parse(response.headers().get("link"))?.let {
                nextRange = it.nextRange()
            }
        }
    }

    var firstVisibleStatus: Status? = null
    var firstVisibleOffset = 0

    private fun savePosition(){
        if(adapter.isEmpty || recyclerView.getChildAt(0) == null){
            return
        }
        firstVisibleStatus = adapter.getItem(layoutManager.findFirstVisibleItemPosition())
        firstVisibleOffset = recyclerView.getChildAt(0).top
    }

    private fun restorePosition(){
        firstVisibleStatus?.let{
            val pos = adapter.getPosition(it)
            layoutManager.scrollToPositionWithOffset(pos, firstVisibleOffset)
        }
    }

    fun insertQuietly(statuses: List<Status>){
        savePosition()
        adapter.addTop(statuses)
        restorePosition()
    }

    override fun onStatus(status: Status?) {
        status?.let {
            adapter.addTop(status)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onNotification(notification: Notification?) {
    }

    override fun onDelete(id: Long?) {
        id?.let {
            adapter.removeById(id)
        }
    }
}