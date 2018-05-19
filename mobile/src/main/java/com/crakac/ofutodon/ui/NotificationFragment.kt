package com.crakac.ofutodon.ui

import com.crakac.ofutodon.model.api.MastodonUtil
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.ui.adapter.NotificationAdapter
import com.crakac.ofutodon.ui.adapter.RefreshableAdapter
import com.crakac.ofutodon.ui.adapter.StatusAdapter
import com.crakac.ofutodon.ui.adapter.StatusChangeListener
import retrofit2.Call

class NotificationFragment: MastodonApiFragment<Notification, List<Notification>>(){
    override fun getTitle() = "通知"

    override fun createAdapter(): RefreshableAdapter<Notification> {
        val adapter = NotificationAdapter(requireActivity())
        StatusAdapter.register(adapter)
        adapter.statusListener = StatusClickListener(requireActivity())
        return adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (adapter as NotificationAdapter).statusListener = null
        StatusAdapter.unregister(adapter as StatusChangeListener)
    }
    override fun onRefreshSuccess(response: List<Notification>) {
        insertQuietly(filterEmptyNotification(response))
    }

    override fun onLoadMoreSuccess(response: List<Notification>) {
        adapter.addBottom(filterEmptyNotification(response))
    }

    private fun filterEmptyNotification(notifications: List<Notification>) : List<Notification>{
        return notifications.filter {
            when (it.getType()) {
                Notification.Type.Favourite, Notification.Type.Mention, Notification.Type.ReBlog ->
                    return@filter it.status != null
                else ->
                    return@filter true
            }
        }
    }

    override fun onRefreshRequest(): Call<List<Notification>>? {
        return MastodonUtil.api?.getNotifications(prev)
    }

    override fun onLoadMoreRequest(): Call<List<Notification>>? {
        return MastodonUtil.api?.getNotifications(next)
    }
}