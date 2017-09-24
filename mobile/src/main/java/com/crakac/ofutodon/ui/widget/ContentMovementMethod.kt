package com.crakac.ofutodon.ui.widget

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView


class ContentMovementMethod private constructor() : LinkMovementMethod() {
    val TAG: String = "ContentMovementMethod"

    companion object {
        val instance = ContentMovementMethod()
    }

    private fun getClickableSpanFromPosition(widget: TextView, buffer: Spannable, x: Int, y: Int): ClickableSpan? {
        var x = x
        var y = y
        x -= widget.totalPaddingLeft
        y -= widget.totalPaddingTop

        x += widget.scrollX
        y += widget.scrollY

        val layout = widget.layout
        val line = layout.getLineForVertical(y)

        if (0 <= line && line < layout.lineCount) {
            val lineLeft = layout.getLineLeft(line)
            val lineRight = layout.getLineRight(line)
            if (x in lineLeft..lineRight) {
                val off = layout.getOffsetForHorizontal(line, x.toFloat())
                val link = buffer.getSpans(off, off, ClickableSpan::class.java)
                if (link.isNotEmpty()) {
                    return link[0]
                }
            }
        }
        return null
    }

    private var lastActionDownClickableSpan: ClickableSpan? = null

    override fun onTouchEvent(widget: TextView, buffer: Spannable,
                              event: MotionEvent): Boolean {

        val action = event.action

        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_DOWN) {
            return Touch.onTouchEvent(widget, buffer, event)
        }


        val currentClickableSpan = getClickableSpanFromPosition(widget, buffer, event.x.toInt(), event.y.toInt())
        if (action == MotionEvent.ACTION_UP) {
            if (lastActionDownClickableSpan is OnHighlightListener) {
                (lastActionDownClickableSpan as OnHighlightListener).onHighlight(false)
                widget.invalidate()
            }
            if (lastActionDownClickableSpan != null && currentClickableSpan == lastActionDownClickableSpan) {
                lastActionDownClickableSpan?.onClick(widget)
            }
            lastActionDownClickableSpan = null
            return true
        } else if (action == MotionEvent.ACTION_DOWN) {
            if (currentClickableSpan != null) {
                if (currentClickableSpan is OnHighlightListener) {
                    (currentClickableSpan as OnHighlightListener).onHighlight(true)
                    widget.invalidate()
                }
                lastActionDownClickableSpan = currentClickableSpan
                return true
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (lastActionDownClickableSpan is OnHighlightListener) {
                (lastActionDownClickableSpan as OnHighlightListener).onHighlight(false)
                widget.invalidate()
            }
            lastActionDownClickableSpan = null
            return true
        }
        Touch.onTouchEvent(widget, buffer, event)
        return false
    }
}