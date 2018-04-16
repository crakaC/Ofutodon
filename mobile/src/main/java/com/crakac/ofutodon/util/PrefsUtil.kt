package com.crakac.ofutodon.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Created by Kosuke on 2017/04/28.
 */
class PrefsUtil {
    val TAG: String = "PrefsUtil"
    companion object{
        private var prefs: SharedPreferences? = null
        fun init(context: Context){
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getLong(key: String, default: Long = 0L): Long{
            return prefs?.getLong(key, default) ?: 0L
        }
        fun getString(key: String, default: String? = null): String?{
            return prefs?.getString(key, default)
        }

        fun putInt(k: String, v: Int){
            prefs?.edit()?.putInt(k, v)?.apply()
        }

        fun putLong(k: String, v: Long){
            prefs?.edit()?.putLong(k, v)?.apply()
        }

        fun getInt(k: String, v: Int): Int{
            return prefs?.getInt(k, v) ?: v
        }

        fun putString(k: String, v: String){
            prefs?.edit()?.putString(k, v)?.apply()
        }

        fun remove(k: String){
            prefs?.edit()?.remove(k)?.apply()
        }

    }
}