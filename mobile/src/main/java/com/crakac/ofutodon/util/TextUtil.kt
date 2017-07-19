package com.crakac.ofutodon.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Kosuke on 2017/07/19.
 */

class TextUtil private constructor(){
    companion object {
        fun parseCreatedAt(source: String): CharSequence{
            val format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            val time = sdf.parse(source).time + TimeZone.getDefault().rawOffset
            return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
        }

    }
}