package com.crakac.ofutodon.ui

import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Status
import retrofit2.Call

class LocalTimelineFragment : TimelineFragment() {
    override fun getTitle() = "ローカル"

    override fun onRefreshRequest(): Call<List<Status>> {
        return Mastodon.api.getPublicTimeline(prev, isLocal = true)
    }

    override fun onLoadMoreRequest(): Call<List<Status>> {
        return Mastodon.api.getPublicTimeline(next, isLocal = true)
    }
}