package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil

class LocalTimelineFragment : StatusFragment() {
    val TAG: String = "LocalTimelineFragment"

    override fun getTitle(): String {
        return "ローカル"
    }

    override fun onRefresh() {
        MastodonUtil.api?.getPublicTimeline(prevRange.q, isLocal = true)?.enqueue(onStatus)
    }

    override fun onLoadMore() {
        if (isLoadingNext || nextRange.maxId == null) return
        MastodonUtil.api?.getPublicTimeline(nextRange.q, isLocal = true)?.enqueue(onNextStatus)
        isLoadingNext = true
    }
}