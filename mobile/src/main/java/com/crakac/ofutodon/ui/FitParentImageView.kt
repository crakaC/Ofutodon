package com.crakac.ofutodon.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class FitParentImageView(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    val TAG: String = "FitParentImageView"
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, parentHeight)
    }
}