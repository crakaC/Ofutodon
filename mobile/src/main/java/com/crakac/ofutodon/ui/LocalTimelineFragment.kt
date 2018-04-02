package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class LocalTimelineFragment : TimelineFragment() {
    override fun getTitle() = "ローカル"

    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getPublicTimeline(prev, isLocal = true)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getPublicTimeline(next, isLocal = true)
    }
}