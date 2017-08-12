package com.crakac.ofutodon.ui

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class HackyViewPager(context: Context, attrs: AttributeSet?): ViewPager(context, attrs) {
    val TAG: String = "HackyViewPager"

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        try{
            return super.onInterceptTouchEvent(ev)
        } catch(e: IllegalArgumentException){
            return false
        }
    }
}