package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class HomeTimelineFragment : StatusFragment() {
    override val TAG: String = "HomeTimelineFragment"

    override fun getTitle(): String {
        return "ホーム"
    }

    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getHomeTimeline(prev.q)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        if (isLoadingNext || next.maxId == null)
            return null
        return MastodonUtil.api?.getHomeTimeline(next.q)
    }
}