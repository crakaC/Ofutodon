package com.crakac.ofutodon.util

import android.net.Uri
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.URLSpan
import android.text.util.Linkify
import com.crakac.ofutodon.ui.widget.LinkClickableSpan
import com.emojione.Emojione
import java.net.IDN

/**
 * Created by Kosuke on 2017/05/05.
 */
object HtmlUtil {
    val MAX_PATH_LENGTH = 20
    val MAX_URL_LENGTH = 40
    fun parse(src: String): Spanned {
        val text = Emojione.shortnameToUnicode(src)
        return shrinkLinks(trimWhiteSpace(Html.fromHtml(text)) as Spanned)
    }

    private fun trimWhiteSpace(source: CharSequence): CharSequence {
        var i = source.length
        do {
            --i
        } while (i >= 0 && Character.isWhitespace(source[i]))
        return source.subSequence(0, i + 1)
    }

    private fun shrinkLinks(spanned: Spanned, linkMask: Int = Linkify.WEB_URLS): Spanned {
        val builder = SpannableStringBuilder(spanned)
        for (span in builder.getSpans(0, builder.length, URLSpan::class.java)) {
            val start = builder.getSpanStart(span)
            val end = builder.getSpanEnd(span)
            val linkText = builder.subSequence(start, end)
            val uri = Uri.parse(linkText.toString())
            val text = if (linkText.startsWith('#') || linkText.startsWith('@') || uri.host == null) {
                linkText
            } else {
                val host = IDN.toUnicode(uri.host)
                val path = uri.path
                val rawUrl = host + path
                val rawLength = rawUrl.length
                when {
                    path.length > MAX_PATH_LENGTH -> host + path.subSequence(0, MAX_PATH_LENGTH - 1) + "…"
                    rawLength > MAX_URL_LENGTH -> rawUrl.substring(0, MAX_URL_LENGTH - 1) + "…"
                    else -> rawUrl
                }
            }
            builder.replace(start, end, text)
            builder.removeSpan(span)

            val clickableSpan = LinkClickableSpan(text.toString(), span.url)

            builder.setSpan(clickableSpan, start, start + text.length, builder.getSpanFlags(span))
        }
        return builder
    }
}