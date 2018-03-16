package com.crakac.ofutodon.ui

import android.graphics.*
import android.text.style.ReplacementSpan
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget

class EmojiSpan(val view: View, val url: String) : ReplacementSpan() {
    val SCALE_RATIO = 1.2f
    val DESCENT_RATIO = 0.211f

    private val mPaint = Paint()
    private val rectSrc = Rect()
    private val rectDst = RectF()

    private var mBitmap: Bitmap? = null

    init {
        mPaint.isFilterBitmap = true
        Glide.with(view.context.applicationContext).load(url).asBitmap().into(object: SimpleTarget<Bitmap>(128, 128){
            override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                mBitmap = resource
                view.invalidate()
            }
        })
    }

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val size = (0.5f + SCALE_RATIO * paint.textSize).toInt()
        if (fm != null) {
            val descent = (0.5f + size * DESCENT_RATIO).toInt()
            val ascent = descent - size
            if (fm.ascent > ascent) fm.ascent = ascent
            if (fm.top > ascent) fm.top = ascent
            if (fm.descent < descent) fm.descent = descent
            if (fm.bottom < descent) fm.bottom = descent
        }
        return size
    }

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val bitmap = mBitmap ?: return
        val size = (0.5f + SCALE_RATIO * paint.textSize).toInt()
        val descent = (0.5f + size * DESCENT_RATIO).toInt()
        val transY = y - size + descent

        canvas.save()
        canvas.translate(x, transY.toFloat())
        rectSrc.set(0, 0, bitmap.width, bitmap.height)
        rectDst.set(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawBitmap(bitmap, rectSrc, rectDst, mPaint)
        canvas.restore()
    }
}