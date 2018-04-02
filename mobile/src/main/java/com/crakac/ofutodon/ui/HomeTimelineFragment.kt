package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class HomeTimelineFragment : TimelineFragment() {
    override fun getTitle() = "ホーム"

    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getHomeTimeline(prev)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getHomeTimeline(next)
    }
}