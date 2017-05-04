package com.crakac.ofutodon.util

import android.text.Html
import android.text.Spanned
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

}