package com.crakac.ofutodon

import android.app.Application
import butterknife.ButterKnife
import com.crakac.ofutodon.util.PrefsUtil

/**
 * Created by Kosuke on 2017/04/27.
 */
class OfutodonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ButterKnife.setDebug(BuildConfig.DEBUG)
        PrefsUtil.init(this)
    }
}