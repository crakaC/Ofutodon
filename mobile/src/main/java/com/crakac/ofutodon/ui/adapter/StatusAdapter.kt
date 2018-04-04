package com.crakac.ofutodon.ui.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.crakac.ofutodon.R
import com.crakac.ofutodon.model.api.entity.Status
import com.crakac.ofutodon.ui.widget.OnClickStatusListener
import com.crakac.ofutodon.ui.widget.StatusViewHolder


class StatusAdapter(context: Activity, val showBottomLoading: Boolean = true) : RefreshableAdapter<Status>(context) {
    val TAG: String = "StatusAdapter"
    val dummy = Status(-1)

    var statusListener: OnClickStatusListener? = null

    override fun getItem(position: Int): Status {
        if (isFooter(position)) {
            return dummy
        }
        return super.getItem(position)
    }

    override fun getItemCount(): Int {
        return when {
            isEmpty -> 0
            showBottomLoading -> super.getItemCount() + 1
            else -> super.getItemCount()
        }
    }

    private fun isFooter(position: Int): Boolean {
        return showBottomLoading && position >= itemCount - 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isFooter(position)) return
        if (holder is StatusViewHolder) {
            val status = getItem(position)
            holder.setStatus(status)
            holder.resetListener(status, statusListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == HolderType.Footer.rawValue) {
            return FooterViewHolder(View.inflate(context, R.layout.dummy_status, null))
        }
        return StatusViewHolder(context!!, View.inflate(context, R.layout.status, null))
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItemId(position) < 0) {
            HolderType.Footer.rawValue
        } else {
            HolderType.Status.rawValue
        }
    }

    private enum class HolderType(val rawValue: Int) {
        Status(3),
        Footer(4)
    }

    class FooterViewHolder(v: View) : RecyclerView.ViewHolder(v)
}