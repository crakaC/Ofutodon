package com.crakac.ofutodon.util

import android.net.Uri
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import com.emojione.Emojione

/**
 * Created by Kosuke on 2017/05/05.
 */
object HtmlUtil {
    fun parse(src: String): Spanned {
        val text = Emojione.shortnameToUnicode(src)
        return trimWhiteSpace(Html.fromHtml(text)) as Spanned
    }

    private fun trimWhiteSpace(source: CharSequence): CharSequence {
        var i = source.length
        do {
            --i
        } while (i >= 0 && Character.isWhitespace(source[i]))
        return source.subSequence(0, i + 1)
    }

    private fun shortenLinks(spanned: Spanned, linkMask: Int = Linkify.ALL): Spanned {
        val builder = SpannableStringBuilder(spanned)
        builder.getSpans(0, builder.length, URLSpan::class.java).forEach { span ->
            val start = builder.getSpanStart(span)
            val end = builder.getSpanEnd(span)
            val flags = builder.getSpanFlags(span)
            val linkText = builder.subSequence(start, end)
            val uri = Uri.parse(linkText.toString())
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