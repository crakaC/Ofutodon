package com.crakac.ofutodon.methods

import com.crakac.ofutodon.BuildConfig
import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.Range

abstract class MastodonMethodTestBase {
    val rangeLong = Range(Long.MAX_VALUE, 0, 10)
    val rangeInt = Range(Int.MAX_VALUE.toLong(), 0, 10)
    val api = Mastodon.initialize("don.crakac.com", BuildConfig.DEBUG_TOKEN)
    val noTokenApi = Mastodon.initialize("localhost")
}