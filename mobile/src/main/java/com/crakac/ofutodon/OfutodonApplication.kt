package com.crakac.ofutodon

import android.app.Application
import com.crakac.ofutodon.db.AppDatabase
import com.crakac.ofutodon.util.PrefsUtil

/**
 * Created by Kosuke on 2017/04/27.
 */
class OfutodonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PrefsUtil.init(this)
        AppDatabase.getInstance(this)
    }
}