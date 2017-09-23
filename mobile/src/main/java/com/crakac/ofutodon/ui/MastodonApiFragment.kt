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
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.Link
import com.crakac.ofutodon.model.api.Range
import com.crakac.ofutodon.model.api.entity.Identifiable
import com.crakac.ofutodon.ui.widget.FastScrollLinearLayoutManager
import com.crakac.ofutodon.ui.widget.RefreshableAdapter
import com.crakac.ofutodon.ui.widget.RefreshableViewHolder
import com.crakac.ofutodon.ui.widget.SwipeRefreshListView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class MastodonApiFragment<AdapterClass : Identifiable, ResponseClass> : Fragment(),
        SwipeRefreshLayout.OnRefreshListener,
        SwipeRefreshListView.OnLoadMoreListener {
    open val TAG: String = "MastodonApiFragment"
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.swipeRefresh)
    lateinit var swipeRefresh: SwipeRefreshListView
    lateinit var unbinder: Unbinder
    lateinit var adapter: RefreshableAdapter<AdapterClass>
    lateinit var layoutManager: LinearLayoutManager

    private var nextRange: Range = Range()
    private var prevRange: Range = Range()
    val next: Map<String, String> get() = nextRange.q
    val prev: Map<String, String> get() = prevRange.q

    var isLoadingNext = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)
        unbinder = ButterKnife.bind(this, view)
        adapter = createAdapter(activity)

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
        swipeRefresh.isRefreshing = true
        onRefresh()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    override fun onStart() {
        super.onStart()
        refreshItem()
    }

    abstract fun createAdapter(context: Context): RefreshableAdapter<AdapterClass>

    open fun onRefreshRequest(): Call<ResponseClass>? = null

    override fun onRefresh() {
        onRefreshRequest()?.enqueue(onRefreshResponse)
    }

    val onRefreshResponse = object : Callback<ResponseClass> {
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
                insertQuietly(convertResponseToAdapterItem(it))
            }
            Link.parse(response.headers().get("link"))?.let {
                prevRange = it.prevRange()
                if (nextRange.maxId == null) {
                    nextRange = it.nextRange()
                }
            }
        }
    }

    open fun onLoadMoreRequest(): Call<ResponseClass>? = null

    override fun onLoadMore() {
        if (isLoadingNext || nextRange.maxId == null) return
        onLoadMoreRequest()?.run {
            enqueue(onLoadMoreResponse)
            isLoadingNext = true
        }
    }

    val onLoadMoreResponse = object : Callback<ResponseClass> {
        override fun onFailure(call: Call<ResponseClass>?, t: Throwable?) {
            isLoadingNext = false
        }

        override fun onResponse(call: Call<ResponseClass>?, response: Response<ResponseClass>?) {
            isLoadingNext = false
            if (response == null || !response.isSuccessful || !isAdded) {
                return
            }
            response.body()?.let {
                adapter.addBottom(convertResponseToAdapterItem(it))
            }
            Link.parse(response.headers().get("link"))?.let {
                nextRange = it.nextRange()
            }
        }
    }

    abstract fun convertResponseToAdapterItem(response: ResponseClass): List<AdapterClass>

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
            val holder = recyclerView.getChildViewHolder(child) as RefreshableViewHolder?
            holder?.refresh()
        }
    }
}