package com.crakac.ofutodon.ui.widget

import android.support.v4.graphics.ColorUtils
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class LinkClickableSpan(val text: String, val url: String, val tag: String?): ClickableSpan(), OnHighlightListener {
    val TAG: String = "LinkClickableSpan"

    val backgroundAlpha = (0.75 * 255).toInt()
    private var isHighlight = false

    override fun onClick(view: View) {

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