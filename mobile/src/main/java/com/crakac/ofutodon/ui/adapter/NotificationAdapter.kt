package com.crakac.ofutodon.ui.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Notification
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import com.crakac.ofutodon.ui.widget.StatusViewHolder

class NotificationAdapter(context: Activity) : RefreshableAdapter<Notification>(context) {

    var statusListener: OnClickStatusListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return StatusViewHolder(context!!, View.inflate(context, R.layout.status, null))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is StatusViewHolder) {
            holder.setNotification(item)
            holder.resetListener(item.status!!, statusListener)
        }
    }
}