package com.crakac.ofutodon.ui

import android.content.Context
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
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.Link
import com.crakac.ofutodon.model.api.Range
import com.crakac.ofutodon.model.api.entity.Identifiable
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.ui.widget.FastScrollLinearLayoutManager
import com.crakac.ofutodon.ui.widget.SwipeRefreshListView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class MastodonApiFragment<AdapterClass : Identifiable, ResponseClass> : Fragment(),
        SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshListView.OnLoadMoreListener {
    open val TAG: String = "MastodonApiFragment"
    lateinit var recyclerView: RecyclerView
    lateinit var swipeRefresh: SwipeRefreshListView
    lateinit var adapter: RefreshableAdapter<AdapterClass>
    lateinit var layoutManager: LinearLayoutManager

    private var nextRange: Range = Range()
    private var prevRange: Range = Range()
    val next: Map<String, String> get() = nextRange.q
    val prev: Map<String, String> get() = prevRange.q
    open var isSwipeRefreshEnabled = true
    open var isLoadMoreEnabled = true
    var isLoadingNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_refreshable, container, false)

        adapter = createAdapter(requireContext(), isLoadMoreEnabled)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = swipeRefresh.recyclerView
        recyclerView.adapter = adapter
        layoutManager = FastScrollLinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val divider = DividerItemDecoration(requireContext(), layoutManager.orientation).apply {
            setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        }
        recyclerView.addItemDecoration(divider)
        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setOnLoadMoreListener(this)
        swipeRefresh.isEnabled = isSwipeRefreshEnabled
        onRefresh()
        return view
    }

    override fun onStart() {
        super.onStart()
        refreshItem()
    }

    abstract fun createAdapter(context: Context, enableRefresh: Boolean = true): RefreshableAdapter<AdapterClass>

    open fun onRefreshRequest(): Call<ResponseClass>? = null

    override fun onRefresh() {
        onRefreshRequest()?.enqueue(onRefreshResponse)
    }

    private val onRefreshResponse = object : Callback<ResponseClass> {
        override fun onFailure(call: Call<ResponseClass>?, t: Throwable?) {
            if (!isAdded) return
            swipeRefresh.isRefreshing = false
            refreshItem()
        }

        override fun onResponse(call: Call<ResponseClass>?, response: Response<ResponseClass>?) {
            if (!isAdded) return
            refreshItem()
            swipeRefresh.isRefreshing = false
            if (response == null || !response.isSuccessful) {
                return
            }
            response.body()?.let {
                onRefreshSuccess(it)
            }
            Link.parse(response.headers().get("link"))?.let {
                prevRange = it.prevRange()
                if (nextRange.maxId == null) {
                    nextRange = it.nextRange()
                }
            }
        }
    }

    abstract fun onRefreshSuccess(response: ResponseClass)

    open fun onLoadMoreRequest(): Call<ResponseClass>? = null

    override fun onLoadMore() {
        if (isLoadingNext || nextRange.maxId == null) return
        onLoadMoreRequest()?.run {
            enqueue(onLoadMoreResponse)
            isLoadingNext = true
        }
    }

    private val onLoadMoreResponse = object : Callback<ResponseClass> {
        override fun onFailure(call: Call<ResponseClass>?, t: Throwable?) {
            isLoadingNext = false
        }

        override fun onResponse(call: Call<ResponseClass>?, response: Response<ResponseClass>?) {
            isLoadingNext = false
            if (response == null || !response.isSuccessful || !isAdded) {
                return
            }
            response.body()?.let {
                onLoadMoreSuccess(it)
            }
            Link.parse(response.headers().get("link"))?.let {
                nextRange = it.nextRange()
            }
        }
    }
    abstract  fun onLoadMoreSuccess(response: ResponseClass)

    var firstVisibleItem: AdapterClass? = null
    var firstVisibleOffset = 0

    private fun savePosition() {
        if (adapter.isEmpty || recyclerView.getChildAt(0) == null) {
            return
        }
        firstVisibleItem = adapter.getItem(layoutManager.findFirstVisibleItemPosition())
        firstVisibleOffset = recyclerView.getChildAt(0).top
    }

    private fun restorePosition() {
        firstVisibleItem?.let {
            val pos = adapter.getPosition(it)
            layoutManager.scrollToPositionWithOffset(pos, firstVisibleOffset)
        }
    }

    fun insertQuietly(statuses: List<AdapterClass>) {
        savePosition()
        adapter.addTop(statuses)
        restorePosition()
    }

    fun scrollToTop() {
        if (!isAdded) return
        recyclerView.scrollToPosition(0)
    }

    fun refreshItem() {
        if (!isAdded) return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as RefreshableAdapter.Refreshable?
            holder?.refresh()
        }
    }
}