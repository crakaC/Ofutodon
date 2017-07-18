package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.ListView
import com.crakac.ofutodon.R

class SwipeRefreshListView : SwipeRefreshLayout, AbsListView.OnScrollListener {
    val TAG: String = "SwipeRefreshListView"

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var listView: ListView

    init {
        val v = View.inflate(context, R.layout.swipe_refresh_list_view, this)
        listView = v.findViewById<ListView>(R.id.listView)
        val emptyView = v.findViewById<View>(R.id.empty)
        listView.emptyView = emptyView
        listView.isNestedScrollingEnabled = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        listView.setOnScrollListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listView.setOnScrollListener(null)
    }

    private var mLastItemVisibleListener: OnLastItemVisibleListener? = null
    private var mIsBottomOfLastItemShown = false
    private var mPreLastItemPosition = -1

    fun setOnLastItemVisibleListener(listener: OnLastItemVisibleListener?) {
        mLastItemVisibleListener = listener
    }

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (visibleItemCount == 0 || view == null) return
        val lastItem = firstVisibleItem + visibleItemCount
        if (lastItem == totalItemCount) {
            if (mPreLastItemPosition != lastItem) {
                mLastItemVisibleListener?.onLastItemVisible()
                Log.d(TAG, "LastItem is Visible")
            }
            mIsBottomOfLastItemShown = view.getChildAt(visibleItemCount - 1).bottom <= view.height
        } else {
            mIsBottomOfLastItemShown = false
        }
        mPreLastItemPosition = lastItem
    }

    override fun onScrollStateChanged(view: AbsListView?, state: Int) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (mIsBottomOfLastItemShown) {
                mLastItemVisibleListener?.onBottomOfLastItemShown()
                Log.d(TAG, "Reach to bottom")
            }
        }
    }

    interface OnLastItemVisibleListener {
        fun onBottomOfLastItemShown(){}
        fun onLastItemVisible(){}
    }
}