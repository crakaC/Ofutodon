package com.crakac.ofutodon

import android.app.Application
import butterknife.ButterKnife

/**
 * Created by Kosuke on 2017/04/27.
 */
class OfutodonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ButterKnife.setDebug(BuildConfig.DEBUG)
    }
}