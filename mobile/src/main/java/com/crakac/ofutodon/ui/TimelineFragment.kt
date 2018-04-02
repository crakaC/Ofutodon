package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.entity.Status

abstract class TimelineFragment: StatusFragment<List<Status>>() {
    override fun onRefreshSuccess(response: List<Status>) {
        insertQuietly(response)
    }

    override fun onLoadMoreSuccess(response: List<Status>) {
        adapter.addBottom(response)
    }
}