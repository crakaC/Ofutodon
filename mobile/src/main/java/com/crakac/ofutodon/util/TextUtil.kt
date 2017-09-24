package com.crakac.ofutodon.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import android.text.style.URLSpan
import android.text.util.Linkify
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kosuke on 2017/07/19.
 */

class TextUtil private constructor() {
    companion object {
        private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        fun parseCreatedAt(source: String): CharSequence {
            val time = sdf.parse(source).time + TimeZone.getDefault().rawOffset
            return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }

        fun currentTimeString(): String {
            return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }

        fun shortenLinks(spanned: Spanned, linkMask: Int = Linkify.ALL): Spanned {
            val builder = SpannableStringBuilder(spanned)
            builder.getSpans(0, builder.length, URLSpan::class.java).forEach { span ->
                val start = builder.getSpanStart(span)
                val end = builder.getSpanEnd(span)
                val flags = builder.getSpanFlags(span)
                val linkText = builder.subSequence(start, end)
                if (linkText.length > 30) {
                    val shortLink = linkText.subSequence(0, 30)
                    builder.replace(start, end, shortLink)
                    builder.removeSpan(span)
                    builder.setSpan(span, start, start + shortLink.length, flags)
                }
            }
            return builder
        }
    }
}