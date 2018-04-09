package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class FederatedTimelineFragment: TimelineFragment() {
    override fun getTitle() = "連合"
    override fun onRefreshRequest(): Call<List<Status>>? =
            MastodonUtil.api?.getPublicTimeline(prev, isLocal = false)

    override fun onLoadMoreRequest(): Call<List<Status>>? =
            MastodonUtil.api?.getPublicTimeline(next, isLocal = false)
}