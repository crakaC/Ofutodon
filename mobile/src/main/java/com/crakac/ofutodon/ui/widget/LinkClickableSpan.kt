package com.crakac.ofutodon.ui.widget

import android.support.v4.graphics.ColorUtils
import android.text.TextPaint
import android.text.style.URLSpan
import android.util.Log
import android.view.View

class LinkClickableSpan(val text: String, val url: String, val tag: String? = null): URLSpan(url), OnHighlightListener {
    private val TAG: String = "LinkClickableSpan"

    private val backgroundAlpha = (0.5 * 255).toInt()
    private var isHighlight = false

    override fun onClick(view: View) {
        Log.d(TAG, "Span is Clicked: $text")
        super.onClick(view)
    }

    override fun onHighlight(isHighlight: Boolean) {
        this.isHighlight = isHighlight
    }

    override fun updateDrawState(ds: TextPaint) {
        if(isHighlight){
            ds.bgColor = ColorUtils.setAlphaComponent(ds.linkColor, backgroundAlpha)
        }
        ds.color = ds.linkColor
        ds.isUnderlineText = false
    }
}