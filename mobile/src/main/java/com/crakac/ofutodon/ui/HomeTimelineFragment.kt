package com.crakac.ofutodon.ui

import com.crakac.ofutodon.api.MastodonUtil
import com.crakac.ofutodon.api.entity.Status
import retrofit2.Call

class HomeTimelineFragment : TimelineFragment() {
    override fun getTitle() = "ホーム"

    override fun onRefreshRequest(): Call<List<Status>>? = MastodonUtil.api?.getHomeTimeline(prev)

    override fun onLoadMoreRequest(): Call<List<Status>>? = MastodonUtil.api?.getHomeTimeline(next)
}