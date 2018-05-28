package com.crakac.ofutodon.ui

import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Status
import retrofit2.Call

class FederatedTimelineFragment: TimelineFragment() {
    override fun getTitle() = "連合"
    override fun onRefreshRequest(): Call<List<Status>> =
            Mastodon.api.getPublicTimeline(prev)

    override fun onLoadMoreRequest(): Call<List<Status>> =
            Mastodon.api.getPublicTimeline(next)
}