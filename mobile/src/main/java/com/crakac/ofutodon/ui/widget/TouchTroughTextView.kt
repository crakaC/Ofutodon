package com.crakac.ofutodon.ui.widget

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class TouchTroughTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    val TAG: String = "TouchTroughTextView"

    private var hasSpanTouched = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        hasSpanTouched = false
        Log.d(TAG, "before super.onTouchEvent")
        super.onTouchEvent(event)
        Log.d(TAG, "after super.onTouchEvent")
        return hasSpanTouched
    }

    fun handleTouchEvent(){
        hasSpanTouched = true
        Log.d(TAG, "handleTouchEvent")
    }
}