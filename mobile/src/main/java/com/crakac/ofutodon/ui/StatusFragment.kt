package com.crakac.ofutodon.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
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
class StatusFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, MastodonStreaming.StreamingCallback {

    @BindView(R.id.listView)
    lateinit var listView: ListView
    @BindView(R.id.swipeRefresh)
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var unbinder: Unbinder
    lateinit var adapter: StatusAdapter

    var nextRange: Range? = null
    var prevRange: Range? = null

    var streaming: MastodonStreaming? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        unbinder = ButterKnife.bind(this, view)
        adapter = StatusAdapter(activity)

        val emptyView = view.findViewById<View>(R.id.empty)
        listView.emptyView = emptyView
        listView.isNestedScrollingEnabled = true
        listView.adapter = adapter
        swipeRefresh.setOnRefreshListener(this)
        streaming = MastodonStreaming()
        streaming?.callBack = this

        //streaming?.connect()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
        streaming?.callBack = null
        streaming?.close()
    }

    fun getTitle(): String = "テスト"


    fun addStatuses(statuses: List<Status>) {
        adapter.addTop(statuses)
    }

    fun addStatus(status: Status) {
        adapter.addTop(status)
    }

    override fun onRefresh() {
        val mastodon = MastodonUtil.api
        if (mastodon == null) {
            swipeRefresh.isRefreshing = false
            return
        }
        mastodon.getHomeTileline(pager = prevRange?.q ?: emptyMap()).enqueue(
                object : Callback<List<Status>> {
                    override fun onFailure(call: Call<List<Status>>?, t: Throwable?) {
                        swipeRefresh.isRefreshing = false
                        Log.w("hoge", t)
                    }

                    override fun onResponse(call: Call<List<Status>>?, response: Response<List<Status>>?) {
                        swipeRefresh.isRefreshing = false
                        if (response == null || !response.isSuccessful) {
                            return
                        }
                        response.body().forEach {
                            Log.d("StatusId", it.id.toString())
                        }
                        insertQuietly(response.body())
                        updateRange(response.headers().get("link"), response.body())
                    }
                }
        )
    }

    private fun updateRange(header: String?, statuses: List<Status>) {
        val link = Link.parse(header)
        if (link == null) {
            Log.d("updateRange", "cannot parse link from response header")
            return
        }

        if (statuses.isEmpty()) {
            Log.d("updateRange", "no need to update range")
            return
        }
        prevRange = Range(sinceId = link.sinceId, limit = 20)
        nextRange = Range(maxId = link.maxId, sinceId = statuses.last().id, limit = 20)
    }

    var firstVisibleStatus: Status? = null
    var firstVisibleOffset = -1

    private fun savePosition(){
        if(adapter.isEmpty || listView.getChildAt(0) == null){
            return
        }
        firstVisibleStatus = adapter.getItem(listView.firstVisiblePosition)
        firstVisibleOffset = listView.getChildAt(0).top
    }

    private fun restorePositioin(){
        firstVisibleStatus?.let{
            val pos = adapter.getPosition(it)
            listView.setSelectionFromTop(pos, firstVisibleOffset)
        }
    }

    fun insertQuietly(statuses: List<Status>){
        if(isResumed){
            savePosition()
        }
        adapter.addTop(statuses)
        restorePositioin()
    }

    override fun onStatus(status: Status?) {
        status?.let {
            addStatus(status)
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