package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class LocalTimelineFragment : StatusFragment() {
    override val TAG: String = "LocalTimelineFragment"

    override fun getTitle(): String {
        return "ローカル"
    }

    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getPublicTimeline(prev.q, isLocal = true)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        if (isLoadingNext || next.maxId == null) return null
        return MastodonUtil.api?.getPublicTimeline(next.q, isLocal = true)
    }
}