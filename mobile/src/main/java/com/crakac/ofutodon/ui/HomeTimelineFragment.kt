package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Status
import retrofit2.Call

class HomeTimelineFragment : StatusFragment() {
    val TAG: String = "HomeTimelineFragment"

    override fun getTitle(): String {
        return "ホーム"
    }

    override fun onRefreshRequest(): Call<List<Status>>? {
        return MastodonUtil.api?.getHomeTimeline(prevRange.q)
    }

    override fun onLoadMoreRequest(): Call<List<Status>>? {
        if (isLoadingNext || nextRange.maxId == null)
            return null
        return MastodonUtil.api?.getHomeTimeline(nextRange.q)
    }
}