package com.crakac.ofutodon.ui

import com.crakac.ofutodon.api.Mastodon
import com.crakac.ofutodon.api.entity.Status
import retrofit2.Call

class HomeTimelineFragment : TimelineFragment() {
    override fun getTitle() = "ホーム"

    override fun onRefreshRequest(): Call<List<Status>> = Mastodon.api.getHomeTimeline(prev)

    override fun onLoadMoreRequest(): Call<List<Status>> = Mastodon.api.getHomeTimeline(next)
}