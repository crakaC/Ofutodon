package com.crakac.ofutodon.ui

import android.util.Log
import android.widget.AbsListView

class LastItemListener: AbsListView.OnScrollListener {
    val TAG: String = "LastItemListener"
    var callback: OnLastItemVisibleListener? = null
    private var mIsBottomOfLastItemShown = false
    private var mPreLastItemPosition = -1

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (visibleItemCount == 0 || view == null) return
        val lastItem = firstVisibleItem + visibleItemCount
        view.lastVisiblePosition
        if (lastItem == totalItemCount) {
            if (mPreLastItemPosition != lastItem) {
                callback?.onLastItemVisible()
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
                callback?.onBottomOfLastItemShown()
                Log.d(TAG, "Reach to bottom")
            }
        }
    }

    interface OnLastItemVisibleListener {
        fun onBottomOfLastItemShown()
        fun onLastItemVisible()
    }
}