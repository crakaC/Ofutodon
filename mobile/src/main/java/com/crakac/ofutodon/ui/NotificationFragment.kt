package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.ui.adapter.NotificationAdapter
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import retrofit2.Call

class NotificationFragment: MastodonApiFragment<Notification, List<Notification>>(){
    override fun getTitle() = "通知"

    override fun createAdapter(): RefreshableAdapter<Notification> {
        val adapter = NotificationAdapter(requireActivity())
        adapter.statusListener = StatusClickListener(requireActivity())
        return adapter
    }

    override fun onRefreshSuccess(response: List<Notification>) {
        insertQuietly(response)
    }

    override fun onLoadMoreSuccess(response: List<Notification>) {
        adapter.addBottom(response)
    }

    override fun onRefreshRequest(): Call<List<Notification>>? {
        return MastodonUtil.api?.getNotifications(prev)
    }

    override fun onLoadMoreRequest(): Call<List<Notification>>? {
        return MastodonUtil.api?.getNotifications(next)
    }
}