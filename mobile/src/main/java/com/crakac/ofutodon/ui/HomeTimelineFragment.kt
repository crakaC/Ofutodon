package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil

class HomeTimelineFragment: StatusFragment() {
    val TAG: String = "HomeTimelineFragment"

    override fun getTitle(): String {
        return "ホーム"
    }

    override fun onRefresh() {
        MastodonUtil.api?.getHomeTimeline(prevRange.q)?.enqueue(onStatus)
    }

    override fun onLoadMore() {
        if (isLoadingNext || nextRange.maxId == null) return
        MastodonUtil.api?.getHomeTimeline(nextRange.q)?.enqueue(onNextStatus)
        isLoadingNext = true
    }
}