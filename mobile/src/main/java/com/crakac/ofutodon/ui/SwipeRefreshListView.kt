package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.crakac.ofutodon.R

class SwipeRefreshListView : SwipeRefreshLayout {
    val TAG: String = "SwipeRefreshListView"

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    @BindView(R.id.recyclerView)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.empty)
    lateinit var emptyView: View

    init {
        val v = View.inflate(context, R.layout.swipe_refresh_list_view, this)
        ButterKnife.bind(v)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recyclerView.removeOnScrollListener(scrollListener)
    }

//    private var mLastItemVisibleListener: OnLastItemVisibleListener? = null
//    private var mIsBottomOfLastItemShown = false
//    private var mPreLastItemPosition = -1
//
//    fun setOnLastItemVisibleListener(listener: OnLastItemVisibleListener?) {
//        mLastItemVisibleListener = listener
//    }

    private var mLoadMoreListener: OnLoadMoreListener? = null
    fun setOnLoadMoreListener(listener: OnLoadMoreListener){
        mLoadMoreListener = listener
    }
    interface OnLoadMoreListener{
        fun onLoadMore()
    }

    val VISIBLE_THRESHOLD = 5
    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            // bail out if scrolling upward or already loading data
            // if (dy < 0 || dataLoading.isDataLoading()) return

            val visibleItemCount = recyclerView.childCount
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

            if (totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
                mLoadMoreListener?.onLoadMore()
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        }
    }

//    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
//        if (visibleItemCount == 0 || view == null) return
//        val lastItem = firstVisibleItem + visibleItemCount
//        if (lastItem == totalItemCount) {
//            if (mPreLastItemPosition != lastItem) {
//                mLastItemVisibleListener?.onLastItemVisible()
//                Log.d(TAG, "LastItem is Visible")
//            }
//            mIsBottomOfLastItemShown = view.getChildAt(visibleItemCount - 1).bottom <= view.height
//        } else {
//            mIsBottomOfLastItemShown = false
//        }
//        mPreLastItemPosition = lastItem
//    }
//
//    override fun onScrollStateChanged(view: AbsListView?, state: Int) {
//        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//            if (mIsBottomOfLastItemShown) {
//                mLastItemVisibleListener?.onBottomOfLastItemShown()
//                Log.d(TAG, "Reach to bottom")
//            }
//        }
//    }
//
//    interface OnLastItemVisibleListener {
//        fun onBottomOfLastItemShown(){}
//        fun onLastItemVisible(){}
//    }
}