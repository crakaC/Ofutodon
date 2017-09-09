package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics

class FastScrollLinearLayoutManager(context: Context): LinearLayoutManager(context) {
    val TAG: String = "FastScrollLinearLayoutManager"
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val scroller = object: LinearSmoothScroller(recyclerView.context){
            private var isScrolled = false

            override fun updateActionForInterimTarget(action: Action) {
                if(isScrolled){
                    action.jumpTo(targetPosition)
                } else {
                    super.updateActionForInterimTarget(action)
                    isScrolled = true
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 5f / displayMetrics.densityDpi
            }

        }
        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }
}